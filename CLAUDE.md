# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

- Java 21 (toolchain) / Spring Boot 3.4.7 / Gradle wrapper.
- Build jar: `./gradlew bootJar` (Linux/macOS) or `gradlew.bat bootJar` (Windows).
- Run app: `./gradlew bootRun`. Default profile is `local` (set in `src/main/resources/application.yml`), which expects MariaDB on `localhost:3306` (db `hansoom`, user `root`/`1234`), Redis on `localhost:6379`, and Elasticsearch on `localhost:9200`.
- Bring up local infra: `docker-compose up -d` (starts `hansoom-mariadb`, `hansoom-redis`, `hansoom-elasticsearch`).
- Run tests: `./gradlew test`. Run a single test: `./gradlew test --tests "com.beyond.HanSoom.HanSoomApplicationTests"` (only one smoke test currently exists under `src/test/java`).
- Docker image: `docker build -t hansoom-be .` (multi-stage Dockerfile builds the bootJar inside the image).
- Kubernetes manifests live in `k8s/` (`depl_svc.yml`, `redis.yml`, `elasticsearch.yml`, `ingress.yml`, `https.yml`); CI deploys via `.github/workflows/deploy_hansoom.yml`.

## Concurrency Load Test

The `test/concurrency` Spring module is dedicated infrastructure for benchmarking reservation locking strategies, not production code. It exposes `POST /api/test/reservations/{strategy}` where `{strategy}` is one of `nolock`, `pessimistic`, `optimistic`, `reentrant`, plus `/reset?stockCount=N` and `/status/{stockId}`.

Driver: `scripts/concurrency-test/load-test.js` (k6). Run with `k6 run -e STRATEGY=pessimistic scripts/concurrency-test/load-test.js`. The script resets stock, fires 100 req/s for 10s, then asserts no over-reservation. When adding a new strategy, wire it into the `switch` in `ConcurrencyTestController.reserve` and register a new `*ReservationService` bean implementing `ConcurrencyTestService`.

## Architecture

### Package layout

Root package is `com.beyond.HanSoom` (case sensitive in code; on Windows the directory shows as `hansoom`). The application is `HanSoomApplication` with `@EnableScheduling`. Each feature is a top-level package containing the same five layers — do not flatten:

```
<feature>/controller/    REST endpoints
<feature>/service/       business logic
<feature>/repository/    Spring Data JPA (+ Elasticsearch repos in hotel/)
<feature>/domain/        JPA entities (and Elasticsearch documents)
<feature>/dto/           request/response DTOs (use DTOs across API boundaries; see "JPA 무한참조" in README)
```

Feature modules: `user`, `hotel`, `room`, `roomImage`, `reservation`, `pay`, `review`, `reviewImage`, `reply`, `wishlist`, `notification`, `chat`, plus `test/concurrency` (load-test only) and `common` (shared infra).

### Cross-cutting infrastructure (`common/`)

- **`common/auth/`** — JWT (access + refresh) with `JwtTokenProvider`, `JwtTokenFilter`, and Spring Security authentication/authorization handlers. Refresh tokens live in HTTP-only cookies; CSRF defense is `OriginRefererCsrfFilter` driven by `OriginRefererProperties` (whitelist in `security.csrf.allowed-origins`). When touching auth, both filters must stay aligned with `SecurityConfig`.
- **`common/config/`** — beans for AWS S3, RabbitMQ, two Redis templates (`RedisChatConfig`, `RedisReservationConfig` — chat and reservation use separate connection factories with distinct serializers; don't merge them), STOMP/WebSocket (`StompWebSocketConfig`, `StompHandler`, `StompEventListener`, `WebSocketSessionRegistry`), and `TomcatConfig` (custom `setMaxParts` to raise the multipart file-count limit — see README troubleshooting).
- **`common/service/`** — `ReservationInventoryService` and `ReservationCacheService` implement Redis-backed stock control using a Lua script for atomic check-and-decrement (this is the production concurrency mechanism; `test/concurrency` exists to benchmark it against JPA locks). `QueueReservationService` implements the bargain-room wait queue. `LinkTicketService` handles the OAuth account-linking flow (409 + ticket dance described in the README). `S3Uploader` is the central S3 client; `InitialDataLoader` seeds dev data; `CommonExceptionHandler` is the global `@RestControllerAdvice`.

### Real-time messaging

Two distinct mechanisms — keep them straight:

1. **Chat** (`chat/`) — clients send via STOMP/WebSocket; `ChatPublishService` writes to a Redis Stream (key from `chat.stream-key`, consumer group `chat.group`). `ChatStreamListenerService` consumes the stream, persists to DB, and broadcasts. Redis Streams were chosen over Pub/Sub specifically to prevent message loss under load — don't regress to Pub/Sub. `ChatRateLimiter` enforces per-user send caps (10/min, 5-min ban). `ChatScheduler` runs periodic cleanup.
2. **Notifications** (`notification/`) — SSE to the client, fanned out across replicas via Redis Pub/Sub. Used for reservation/cancel/host-notice events.

### Search

Hotel search runs on Elasticsearch (`HotelDocument`, `HotelSearchQueryBuilder`) to offload complex queries from MariaDB, plus `search_as_you_type` fields for autocomplete. `HotelSpecification` covers the remaining JPA-side filters. `GeocoderService` calls Kakao GEOCODING via `WebClient` (not `RestTemplate` — the README documents why this matters).

### Profiles & secrets

- `application.yml` only sets `spring.profiles.active: local`.
- `application-local.yml` carries development values (currently includes credentials — treat the file as untrusted and never echo its contents into commit messages, PRs, or logs).
- `application-prod.yml` reads everything sensitive from env vars (`DB_HOST`, `DB_PW`, `S3_ACCESSKEY`, `S3_SECRETKEY`, `KAKAO_RESTAPI_KEY`, `SECRETKEY_AT`, `SECRETKEY_RT`, `GOOGLE_CLIENT_ID`, `CLIENT_SECRET`, `KAKAO_CLIENT_ID`, `CONSUMER_NAME`). Kubernetes `Secret` `hansoom-secrets` supplies them via `secretKeyRef` in `k8s/depl_svc.yml`.

## Working conventions

- `instructions.md` (in repo root) requires presenting a step-by-step plan and waiting for user approval before executing commands. Follow it for non-trivial changes.
- Always cross entity-to-API boundaries with DTOs; bidirectional JPA relations exist (`Hotel`↔`Room`↔`RoomImage`) and serializing entities directly causes infinite recursion.
- Reservation stock mutations must go through the Lua-script path in `ReservationInventoryService` to preserve atomicity — don't add a raw `GET`/`SET` shortcut even for "simple" cases.
