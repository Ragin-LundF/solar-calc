---
name: module-architecture
description: >
  Solar Calc module map: what each Gradle module owns, the actual inter-module
  dependency graph, layer rules, REST/domain responsibilities, caching
  strategy, and code-generation policy. Load for changes touching module
  boundaries, cross-module behavior, new classes whose owning module is unclear,
  or Gradle module structure.
---

# Module Architecture

Use this file for module boundaries, package layout, adapters, the REST input layer, and cross-module changes.

## Goal

The Solar Calculator (SC) is a multi-module Gradle build with a layered architecture, so developers can switch between modules without relearning structure. Technical domains are separated into their own Gradle subprojects with explicit, one-directional dependencies.

## Modules

The build is defined in `settings.gradle`.

| Module                      | Responsibility                                                                                                                                                                                                       |
|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `solarcalc-server`          | Spring Boot application. Holds `Application.kt` (entry point), Spring configuration/wiring, and security config. Produces the `bootJar`. Contains no business logic. Only module that may use Spring Boot starters.  |
| `solarcalc-dtos`            | REST Models as DTO.Used by `solarcalc-domain-services` and `solarcalc-rest-api`.                                                                                                                                     |
| `solarcalc-rest-api`        | REST `@RestController` implementations, request/response DTOs, `GlobalExceptionHandler`, and Spring MVC config. Controllers call services from `solarcalc-domain-services`; they never access repositories directly. |
| `solarcalc-domain-services` | Business logic, orchestration, caching. Holds `@Service`/`@Component` beans by subdomain, Konvert mappers, cache config, and domain-specific interfaces. This is the module core. No web or servlet dependencies.    |
| `solarcalc-domain-models`   | JPA entities, Spring Data repositories, embeddables, and domain enums. QueryDSL Q-types generated via KSP.                                                                                                           |

### Shared infrastructure modules

These have no domain knowledge and are depended on widely:

| Module                  | Responsibility |
|-------------------------|---|
| `solarcalc-kotlin-extensions` | `kotlinEquals` / `kotlinHashCode` / `kotlinToString` helpers, primarily for Hibernate entities. No dependencies. |
| `solarcalc-logging`           | Method-logging annotations (`@LogMethod`, `@LogDuration`, `@LogMethodWithParams`) with parameter skip/obfuscation. |

### Frontend modules

| Directory          | Responsibility                                       |
|--------------------|------------------------------------------------------|
| `solarcalc-webapp` | Angular frontend with https://zardui.com components. |


## Dependency direction

Actual inter-module dependencies (arrow = "depends on"):

- `solarcalc-server` → `solarcalc-rest-api` (uses `solarcalc-dtos`) → solarcalc-domain-services` (uses also `solarcalc-dtos`) → `solarcalc-domain-models`

Rules:

- REST-API interfaces, RestControllers and API Models as DTO lives only in `solarcalc-rest-api`. Do not spread API related things into other modules.
- Business logic lives only in `solarcalc-domain-services` (and adapters for their integration). Do not spread domain rules into `solarcalc-server`.
- Domain services must not depend on `solarcalc-server` or on the REST controller interfaces as a runtime contract owner — they *implement* the REST interfaces.
- No module duplicates domain rules that belong in `solarcalc-domain-services`.

## Domain layer

- `*DomainController` façades are the primary entry points; they delegate to stateless `@Service` / `@Component` beans organised by subdomain.
- Konvert generates the entity ↔ DTO mappers via KSP.
- Repositories and entities live in `solarcalc-domain-models`; all repository queries are tenant-scoped via a `tenantUid` parameter to enforce data isolation.
- Entities use `id: Long? = null` and `var` properties for Hibernate's lifecycle; use `solarcalc-kotlin-extensions` for `equals`/`hashCode`/`toString`.

## REST API layer

- Contains the `@RestController` implementations and request/response DTOs (`*Request`, `*Response`, `*Dto` classes).
- Controllers call services from `solarcalc-domain-services` and repository/entity types from `solarcalc-domain-models` only via the service layer — never bypass the service to query repositories directly.
- Exception handling lives in `GlobalExceptionHandler`; do not duplicate error mapping in individual controllers.

## Frontend

- The Angular SPA lives in `solarcalc-webapp/`. It is a standalone Angular 22 app (no NgModules).
- API calls go through `core/api/ApiService`. Feature components delegate HTTP work to `core/api/` services.
- See `instructions/angular-guidelines.md` for Angular-specific rules.

### Caching

Manual caching (Caffeine) is used instead of `@Cacheable`, because Spring's proxy-based AOP cannot intercept internal (same-bean) calls. Access caches through the `CacheManager.getOrPut` / `clearCache` extensions in `CacheExtensions.kt`. Cache names are constants registered explicitly in `CacheManagerConfig.Companion` and must be registered there before use.

## Generation policy

- QueryDSL Q-types (`solarcalc-domain-models`) and Konvert mappers (`solarcalc-domain-services`, adapters) are generated via KSP at build time. Do not edit generated sources.
