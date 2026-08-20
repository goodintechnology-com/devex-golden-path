# devex-golden-path

A "Golden Path" platform-engineering project: the developer experience from "I need a new service" to "I have a compliant, buildable, deployable service."

This repo is also the **template**: use GitHub's "Use this template" button to start a new service with the same approved structure, then swap out the `release` package for your own domain logic. The questions a new service shouldn't have to ask twice:

- **Java version** — 21 (see `pom.xml`, `java.version`).
- **Structure** — a single Maven module, application code under `src/main/java/<groupId>/...`, tests mirroring it under `src/test/java/...`.
- **Build** — `mvn package`.
- **Test** — `mvn test`.
- **Containerize** — the `Dockerfile` in this repo (multi-stage: build with Maven, run on a JRE image).
- **CI** — `.github/workflows/ci.yml` runs on every push/PR to `main`.

This app itself is a **Deployment Readiness API** — teams submit release metadata and check results (unit tests, quality gate, security scan, SBOM, approval), and the service decides whether a release is ready to deploy, and if not, why.

## Running

```
mvn spring-boot:run
```

The API listens on `http://localhost:8080`.

## Testing

```
mvn test
```

## Containerizing

```
docker build -t devex-golden-path .
docker run --rm -p 8080:8080 devex-golden-path
```

## CI/CD

`.github/workflows/ci.yml` runs the full pipeline on every push/PR to `main`: Compile → Unit Test → Package → Container Build. On pushes to `main`, the built image is also published to `ghcr.io/rgoodin/devex-golden-path` (tagged with the commit SHA and `latest`) using the workflow's built-in `GITHUB_TOKEN` — no extra secrets needed. The first time a package is published this way it may land as private; make it public from the repo's Packages tab if you want it pullable without auth.

A later stage adds a SonarQube quality-gate step to this same workflow.

## Copilot instructions

`.github/copilot-instructions.md` gives GitHub Copilot repo-level context: architecture, coding conventions, testing and security expectations, and how generated code should fit into this project. It's picked up automatically by Copilot Chat in supported IDEs and on github.com.

## API

| Method | Path                       | Description                                   |
|--------|----------------------------|------------------------------------------------|
| POST   | `/releases`                | Create a release (`version`, `environment`)     |
| GET    | `/releases/{id}`           | Fetch a release and its current check results   |
| POST   | `/releases/{id}/checks`    | Submit/update a check result (`checkType`, `status`) |
| GET    | `/releases/{id}/readiness` | Compute readiness: `READY` or `BLOCKED` (+ reasons) |

Required checks: `UNIT_TESTS`, `QUALITY_GATE`, `SECURITY_SCAN`, `SBOM`, `APPROVAL`, each with status `PENDING`, `PASS`, or `FAIL`. A release is `READY` only when every required check is `PASS`.

Example:

```
curl -X POST localhost:8080/releases \
  -H 'Content-Type: application/json' \
  -d '{"version":"1.8.4","environment":"production"}'

curl -X POST localhost:8080/releases/{id}/checks \
  -H 'Content-Type: application/json' \
  -d '{"checkType":"APPROVAL","status":"PASS"}'

curl localhost:8080/releases/{id}/readiness
```
