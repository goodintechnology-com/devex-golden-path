# Developer Golden Path

A reusable, Copilot-aware Java service template with CI/CD, automated quality gates, dependency security scanning, artifact publishing, SBOM generation, and container publishing built in.

The goal is simple: a developer should be able to create a new service using an approved engineering path without having to assemble the build, security, quality, and delivery tooling themselves.

## Start Here — Using This Template

After creating a new repository using **Use this template** in GitHub:

### 1. Clone your new repository

```bash
git clone <your-repository-url>
cd <your-repository-name>
```

### 2. Initialize the service

Run the initialization script using the name of your new repository:

```bash
./scripts/init-service.sh <service-name>
```

For example:

```bash
./scripts/init-service.sh deployment-approval-service
```

The initialization script configures the service-specific identity in:

- Maven artifact and project name
- Spring Boot application name
- SonarQube project key
- GitHub Copilot repository instructions

Platform-wide configuration, such as shared security policies and artifact repositories, remains unchanged.

Review the generated changes before committing:

```bash
git diff
```

### 3. Configure SonarQube Cloud

The `goodintechnology.com` GitHub App already imports every new repository in the org into SonarQube Cloud automatically, using the project key `goodintechnology-com_<service-name>` — the same key `init-service.sh` wrote into `pom.xml`.

Because the Golden Path performs SonarQube analysis from CI instead, open the new project in SonarQube Cloud and disable **Automatic Analysis**. `SONAR_TOKEN` and `ARTIFACTORY_ACCESS_TOKEN` are already available to every repository as organization-level GitHub Actions secrets — no per-repository setup needed.

### 4. Review the service documentation

Review and update:

- `README.md`
- `CLAUDE.md`

These files contain descriptive documentation that should reflect the purpose and domain of the new service. They are deliberately not rewritten automatically by the initialization script.

### 5. Commit and push

```bash
git add .
git commit -m "Initialize service from Golden Path"
git push
```

The Golden Path CI pipeline will automatically compile, test, analyze, security scan, package, publish artifacts, generate an SBOM, build the container image, and publish the image.

> **Note:** the very first CI run for a brand-new SonarQube Cloud project can fail at the
> **SonarCloud Quality Gate Check** step with "Quality Gate not set for the project." SonarQube
> Cloud can't evaluate New Code conditions until a second analysis gives it something to compare
> against. Re-running the failed job (no code change needed) resolves it — every run after that
> analyzes normally.

> **Note:** "Use this template" pushes the template's own initial commit to your new repository
> immediately, before you've run `init-service.sh`, which triggers a CI run of its own. That run
> still carries the template's identity, so it only compiles and tests — it deliberately skips
> SonarCloud, Xray, Artifactory, and container publishing so it can't push analysis or artifacts
> under the template's own project. This is expected; ignore it and continue with the steps above.

---

## What the Golden Path Provides

The template establishes an opinionated engineering baseline so each service does not need to independently configure its delivery toolchain.

### Application Platform

- Java 21
- Spring Boot 3.x
- Maven
- Docker

### Developer Experience

- GitHub repository template
- Repository-level GitHub Copilot instructions
- Standard Maven project structure
- Repeatable service initialization
- Centralized reusable CI workflow

### CI/CD

GitHub Actions provides the automated delivery pipeline.

On pushes and pull requests to `main`, the pipeline performs:

```text
Compile
  ↓
Unit Test
  ↓
SonarQube Scan
  ↓
SonarQube Quality Gate
  ↓
JFrog Xray Audit
  ↓
Package
  ↓
Publish to Artifactory
  ↓
Publish Build Info
  ↓
Build Container Image
  ↓
Publish Container Image
```

A failed quality or security gate stops downstream delivery.

## SonarQube Quality Gates

SonarQube Cloud provides static analysis and quality enforcement.

Analysis is performed by the CI pipeline rather than SonarQube Automatic Analysis. This allows the Quality Gate to become an explicit delivery control.

If the Quality Gate fails, the pipeline stops before artifacts and container images are published.

## JFrog Artifactory and Xray

Packaged artifacts are published to JFrog Artifactory.

JFrog Xray audits application dependencies against the shared Golden Path security policy:

```text
golden-path-security-watch
```

The security watch is platform policy and intentionally remains the same across generated services.

Build Info links published artifacts to the corresponding CI execution.

## Software Bill of Materials

The Maven build generates a CycloneDX SBOM.

The SBOM is:

- packaged with the application
- published alongside the application artifact
- available through the Spring Boot actuator SBOM endpoint

This provides a machine-readable inventory of the components contained in the application.

## Container Publishing

The pipeline builds a Docker image for the service.

On pushes to `main`, the image is published to GitHub Container Registry using:

```text
ghcr.io/<owner>/<repository>
```

Images are tagged with the Git commit SHA and `latest`.

## Reusable CI

The implementation of the delivery pipeline is maintained centrally by the Golden Path rather than copied independently into every generated service.

A consuming service contains a small workflow that invokes:

```text
.github/workflows/reusable-ci.yml
```

The consuming repository explicitly grants the permissions and secrets required by the shared workflow.

The reusable workflow is referenced using an immutable full commit SHA rather than a mutable branch such as `main`.

This allows the platform implementation to be maintained centrally while service teams deliberately adopt vetted workflow versions.

## Copilot-Aware Development

The template includes:

```text
.github/copilot-instructions.md
```

These repository-level instructions provide AI coding tools with project-specific engineering context, including:

- Java and Maven conventions
- project structure
- testing expectations
- API conventions
- build practices

The intent is for AI-assisted development to follow the same paved road as human development rather than requiring developers to repeatedly explain repository conventions.

## Design Philosophy

A Golden Path is not intended to prevent developers from making engineering decisions.

It is intended to remove decisions that every team should not have to make independently.

Service teams own their application logic and domain.

The platform owns reusable engineering capabilities such as:

- build conventions
- quality gates
- security controls
- artifact management
- software supply-chain controls
- container publishing
- developer tooling defaults

The result should be a path that is easier to follow than to avoid.

## Current Limitations

Repository creation from a GitHub template copies service-specific values from the source template.

The included `scripts/init-service.sh` provides a lightweight initialization step that replaces those values after repository creation.

A future platform implementation could move this personalization into the provisioning process so repository creation and service initialization become a single operation.

For now, the initialization script keeps that behavior explicit, understandable, and easy to demonstrate.