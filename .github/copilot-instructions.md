# Copilot instructions for devex-golden-path

This project uses **Java 21** and **Maven**, with **Spring Boot 3.x**. There is no other build tool, no Gradle, and no Lombok — write plain Java.

## Architecture

Single Maven module. Application code lives under `com.goodintechnology.devexgoldenpath`, with one feature package per domain concept — currently just `release`. Each feature package follows this layering:

- A plain Java domain model (e.g. `Release`) — no framework annotations on the model itself.
- A `*Repository` — a thin Spring `@Repository` component. Currently in-memory (`ConcurrentHashMap`); there is no database and no Spring Data JPA in this project yet.
- A `*Service` — owns all business logic. Controllers must not contain business logic; they only translate HTTP <-> service calls.
- A `*Controller` — `@RestController`, thin, delegates to the service, maps to `dto` types.
- A `dto` subpackage — request/response records, decoupled from the domain model. Controllers never accept or return domain objects directly.
- A `*NotFoundException` + a shared `@RestControllerAdvice` mapping it to a 404 JSON body.

When adding a new feature, follow this same package layout rather than inventing a new one.

## Coding conventions

- Use Java **records** for DTOs (see `release/dto`). Use plain classes with explicit getters for mutable domain models (see `Release`).
- Use constructor injection for Spring beans, not field injection (`@Autowired` on fields).
- Prefer `enum` over string constants for fixed sets of values (see `CheckType`, `CheckStatus`, `ReadinessStatus`).
- Validate request DTOs with `jakarta.validation` annotations (`@NotBlank`, `@NotNull`) rather than manual null checks in the controller.
- Reuse existing dependencies before adding new ones. Current dependencies: `spring-boot-starter-web`, `spring-boot-starter-validation`, `spring-boot-starter-test`. Don't add a new library (e.g. Lombok, a JSON library, a testing framework) without a clear reason it's not already covered.

## Testing expectations

Every new REST endpoint requires:
- A unit test on the service method it calls (see `ReleaseServiceTest`) — no Spring context needed for these.
- An integration test through the controller using `@SpringBootTest` + `MockMvc` (see `ReleaseControllerIntegrationTest`) covering the success path and at least one failure/edge case (e.g. not-found, validation failure).

Run `mvn test` before proposing any change as finished.

## Security expectations

- Never put credentials, tokens, or API keys in source code or commit them.
- Don't log request/response bodies that could contain secrets.
- All external input arrives through the `dto` layer and must be validated there, not deep inside the service.

## Fitting into this project

- Follow the existing package structure — don't introduce a different layering style (e.g. no "manager" or "handler" classes alongside the existing service layer).
- Prefer extending an existing enum (like adding a new `CheckType`) over adding a parallel mechanism for something the codebase already models.
- Keep the app framework-minimal: no new Spring Boot starters, no database, no message queue, unless the task explicitly requires one.
