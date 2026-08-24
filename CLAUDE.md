# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

`devex-golden-path` is a staged "Golden Path" platform-engineering project (see the GitHub issues for the full roadmap: base app → developer template → Copilot-aware repo instructions → CI/CD → SonarQube → Artifactory/Xray). The app itself is a Deployment Readiness API that gates release deployment on a fixed set of check results. This repo also doubles as the project template — it's a GitHub "Template repository", and new services are meant to be generated from it and then have the `release` package swapped for their own domain logic.

## Commands

- Build: `mvn compile`
- Run all tests: `mvn test`
- Run a single test class: `mvn test -Dtest=ReleaseServiceTest`
- Run a single test method: `mvn test -Dtest=ReleaseServiceTest#readinessIsReadyWhenAllRequiredChecksPass`
- Run the app locally: `mvn spring-boot:run` (listens on `http://localhost:8080`)
- Package a jar: `mvn package`
- Build the container image: `docker build -t devex-golden-path .`
- Run the container: `docker run --rm -p 8080:8080 devex-golden-path`

Requires Java 21 and Maven (no wrapper is checked in — both are installed system-wide in this environment).

CI/CD (`.github/workflows/ci.yml`) runs on every push/PR to `main`: Compile → Unit Test → SonarCloud Scan → SonarCloud Quality Gate Check → Xray Audit → Package → Publish to Artifactory → Container Build, and on pushes to `main` also pushes the image to `ghcr.io/${{ github.repository }}` (tags: commit SHA and `latest`) via the built-in `GITHUB_TOKEN`. Both the SonarCloud quality gate and the Xray audit fail the job (blocking everything downstream) on violation. Everything after Unit Test is gated on `github.repository == 'goodintechnology-com/devex-golden-path'`, because "Use this template" pushes this same workflow file (still carrying the template's own Maven/Sonar/Artifactory identity) to every new repo before `scripts/init-service.sh` has renamed anything — without the guard, that pre-init commit would publish analysis and artifacts under the template's own identity. SonarCloud project: org `goodintechnology-com`, project key `goodintechnology-com_devex-golden-path` — identifiers live in `pom.xml` `<properties>`, matching the SonarCloud GitHub App's auto-provisioned project (Automatic Analysis is disabled there since CI does the real scan). `SONAR_TOKEN` and `ARTIFACTORY_ACCESS_TOKEN` are organization-level GitHub Actions secrets, available to every repo in `goodintechnology-com` without per-repo setup.

Coverage comes from `jacoco-maven-plugin` (bound to the `test` phase), producing `target/site/jacoco/jacoco.xml`, which SonarCloud reads via the `sonar.coverage.jacoco.xmlReportPaths` property.

JFrog Platform trial at `https://trialpghcwm.jfrog.io` (`JF_URL` in CI env, token in the `ARTIFACTORY_ACCESS_TOKEN` repo secret). Repos: `golden-path-libs-{release,snapshot}-local` (deploy targets), `golden-path-libs-{release,snapshot}` (virtual, resolve), `golden-path-maven-remote` (proxies Maven Central). Xray policy `golden-path-security-policy` (CVE severity ≥ High → Fail Build) is bound via watch `golden-path-security-watch` to both local repos; CI passes `--watches=golden-path-security-watch` explicitly to `jf audit` since the watch is scoped to repo-based scanning, not applied to CLI audits by default. `cyclonedx-maven-plugin` is declared bare in `pom.xml` — Spring Boot 3.3+'s native SBOM support auto-binds it to the `package` phase, producing `target/classes/META-INF/sbom/application.cdx.json` (bundled into the jar, also live at `/actuator/sbom/application` since `spring-boot-starter-actuator` is present).

`.github/copilot-instructions.md` holds the repo-level conventions given to GitHub Copilot (architecture, coding/testing/security conventions). Keep it in sync with the architecture notes below if either changes.

## Architecture

Single Spring Boot module. All application code lives under `com.goodintechnology.devexgoldenpath.release`:

- `Release` — the domain model: id, version, environment, createdAt, and a `Map<CheckType, CheckStatus>` seeded with every `CheckType` at `PENDING`.
- `CheckType` — the fixed set of required checks: `UNIT_TESTS`, `QUALITY_GATE`, `SECURITY_SCAN`, `SBOM`, `APPROVAL`. This enum is the single source of truth for what's required — readiness computation just iterates it.
- `CheckStatus` — `PENDING` / `PASS` / `FAIL`.
- `ReleaseRepository` — an in-memory `ConcurrentHashMap<UUID, Release>`. There is no database; this is stage-1 scope only.
- `ReleaseService` — owns the one piece of real business logic: `computeReadiness` walks every check on a release and reports `READY` only if all are `PASS`; otherwise `BLOCKED` with one human-readable reason per non-passing check (`CHECK_LABELS` maps enum constants to display names).
- `ReleaseController` — the four REST endpoints (`POST /releases`, `GET /releases/{id}`, `POST /releases/{id}/checks`, `GET /releases/{id}/readiness`). DTOs in `release/dto` decouple the wire format from the domain model.
- `ReleaseExceptionHandler` — `@RestControllerAdvice` mapping `ReleaseNotFoundException` to a 404 JSON body.

To add a new required check type: add it to `CheckType`, add its display label to `ReleaseService.CHECK_LABELS`. `Release` automatically seeds new releases with it at `PENDING`, so no other changes are needed for it to affect readiness.

Tests mirror the two layers: `ReleaseServiceTest` unit-tests the readiness logic directly (no Spring context); `ReleaseControllerIntegrationTest` uses `@SpringBootTest` + `MockMvc` to exercise the full HTTP flow end-to-end (create → submit checks → readiness, including the BLOCKED→READY→BLOCKED transitions and the 404 case).
