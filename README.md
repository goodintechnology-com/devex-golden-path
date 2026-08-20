# devex-golden-path

A "Golden Path" platform-engineering project: the developer experience from "I need a new service" to "I have a compliant, buildable, deployable service."

This first stage is a **Deployment Readiness API** — teams submit release metadata and check results (unit tests, quality gate, security scan, SBOM, approval), and the service decides whether a release is ready to deploy, and if not, why.

## Running

```
mvn spring-boot:run
```

The API listens on `http://localhost:8080`.

## Testing

```
mvn test
```

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
