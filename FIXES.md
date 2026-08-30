# Fix Log — baseline for further development

This file documents what was fixed in this snapshot of the codebase, so
anyone picking up the project afterward has a record of what changed and
why, instead of relying on chat history.

**Before building anything new on top of this: run `mvn clean install` from
the repo root once, for real, on a machine with internet access to Maven
Central.** Every fix below was verified by static review (syntax, imports,
method signatures, cross-file references all checked by hand) but has not
been confirmed by an actual compile — this environment had no access to
Maven Central to run one. If the build fails, it should be a quick fix, but
don't assume this is proven correct until you've seen it compile green.

## Fixed

1. **claims-service claim validation was silently broken.**
   `PolicyServiceClient.getPolicy()` / `getUserPolicies()` deserialized
   user-service's response straight into a `Map`/`List<Map>`, but
   user-service wraps every response in an `ApiResponse<T>` envelope
   (`{success, message, data, timestamp}`). `isPolicyActive()` was reading
   `id`/`status` off the wrong object and always returned `false`.
   Fixed: the client now unwraps `ApiResponse<T>` before reading fields.
   Also made claims-service's local `ApiResponse` tolerant of the extra
   `timestamp` field (`@JsonIgnoreProperties(ignoreUnknown = true)`) so
   deserializing it doesn't throw.

2. **Dashboard's "total premium paid" always showed 0.**
   pricing-service's `DashboardService` called
   `GET /api/policies/{id}/premium-history` on user-service, but that
   endpoint didn't exist. Restored `PolicyPremiumHistory` (entity +
   repository) in user-service, wired `PolicyService` to record a row
   whenever a policy's premium actually changes, and added the endpoint.

3. **`usageCap` had no real source.** pricing-service's `PolicyDTO` had a
   `usageCap` field that user-service never populated (no such concept
   existed anywhere in user-service), so usage-cap alerts always fell back
   to a hardcoded `100.0` regardless of the real policy. Added a real
   `usageCap` field to `PolicyTemplate` (admin-settable), and a pass-through
   `Policy.getUsageCap()` so it flows through to the JSON at the top level,
   matching what pricing-service already expected.

4. **Usage-cap threshold alerts spammed on every usage entry past 80%/100%.**
   Added `PolicyAlertState` (one row per policy, two booleans) so each
   threshold fires exactly once per policy.

5. **`PATCH /api/claims/{id}/status` returned a 500 on invalid input.**
   `ClaimStatus.valueOf(...)` threw a raw `IllegalArgumentException` on a
   bad/typo'd status string, which fell through to the generic exception
   handler. Now validates explicitly and throws `BusinessRuleException` for
   a clean `400` with the list of valid statuses.

6. **pricing-service and claims-service had no authentication at all.**
   Every endpoint on both services (including pricing-rule admin CRUD and
   coupon creation) was open to anyone who could reach ports 8082/8083.
   Fixed by adding `spring-boot-starter-security` + a JWT validator/filter
   to both services that validates the *same* token user-service already
   issues, using the same shared HMAC secret (`app.jwt.secret` - must be
   identical across all three services' `application.yml`, currently the
   same dev-only literal in all three). No new login flow, no network
   round-trip back to user-service to check a token - each service verifies
   the signature locally.
   - pricing-service: writes to `/api/pricing/rules/**` and
     `POST /api/coupons` require `ROLE_ADMIN`; everything else just needs
     any valid token.
   - claims-service: `POST /api/risk/rules` requires `ROLE_ADMIN` or
     `ROLE_UNDERWRITER`; everything else just needs any valid token.
   - user-service's `AdminController` (which proxies pricing-rule CRUD to
     pricing-service on behalf of an admin) now forwards the original
     caller's `Authorization` header through `PricingRuleServiceClient`
     instead of calling pricing-service anonymously - that anonymous call
     would now get rejected by pricing-service's own `ROLE_ADMIN` check.
   - Swagger UI's Authorize button now works on all three services (added
     the `bearerAuth` security scheme to pricing/claims' `OpenApiConfig`,
     matching what user-service already had).

   **This means every Postman/Swagger call to pricing-service or
   claims-service now needs `Authorization: Bearer <token>` from
   `POST /api/auth/login` on user-service** - update your saved requests
   accordingly.

7. **CORS was not configured on any service**, which silently blocks every
   request from a browser-based frontend running on a different origin/port
   (e.g. a React dev server on `localhost:5173`). Added a
   `CorsConfigurationSource` bean to all three services' `SecurityConfig`,
   allowing `http://localhost:5173` by default (override via
   `odmip.cors.allowed-origins` if the frontend runs elsewhere).

8. **Added `GET /api/auth/me`** to user-service. A JWT only carries username
   and roles, not the numeric userId - the new frontend needs its own
   userId for policy/claims/dashboard lookups, and there was previously no
   way to resolve "who am I" from a token. Requires authentication (carved
   out of the public `/api/auth/**` wildcard explicitly, since it needs a
   real principal).

9. **claims-service's Postgres password didn't match the other two
   services.** user-service and pricing-service both default to
   `postgres`/`postgres`; claims-service defaulted to `postgres`/`root`.
   Against one shared local Postgres instance (the normal setup - one
   Postgres server, three databases), claims-service would fail to
   authenticate. Standardized on `postgres`/`postgres` everywhere.

10. **Added a working `docker-compose.yml`.** The three Dockerfiles
    previously only ran a jar that had to already be built locally
    (`COPY target/*.jar`) - `docker compose up --build` alone couldn't work
    from a clean checkout. Rewrote all three as proper multi-stage builds
    (Maven build stage + JRE runtime stage), and added a root
    `docker-compose.yml` that starts Postgres (auto-creating the three
    databases via `docker/init-db.sql`) plus all three services, wired to
    reach each other and Postgres by container name instead of `localhost`.
    One command (`docker compose up --build`) now brings up the entire
    backend from a clean clone - no local Maven or Postgres install needed.
    The frontend still runs separately on the host (`npm run dev`) and
    reaches these services through the same published ports either way.
    Also removed a stale, broken `docker-compose.yml` that was sitting in
    `Person-A-user-service/od-mip/` - it referenced a
    `POSTGRES_MULTIPLE_DATABASES` env var that doesn't exist on the plain
    `postgres` image and build paths (`./pricing-service`, `./claims-service`)
    that don't exist relative to that folder. Use the one at the repo root.

11. **pricing-service's `PolicyServiceClient` was missing two methods it
    was already being called with** - `getPremiumHistory(Long)` (called by
    `DashboardService`) and `getUser(Long)` (called by
    `PremiumCalculatorService` and `UsageTrackingService` for email
    notifications). This was a genuine pre-existing bug in the original
    codebase, not something introduced by any of the fixes above -
    pricing-service could never have compiled as originally uploaded.
    It only surfaced when a real `docker compose up --build` actually ran
    `mvn clean package` for the first time; static review alone (no Maven
    Central access available while producing fixes 1-10) didn't catch it.
    Added both methods, matching the real
    `GET /api/policies/{id}/premium-history` and `GET /api/auth/users/{id}`
    endpoints on user-service. **This is a live reminder that "verified by
    static review" is not the same as "verified by a real build" - if you
    hit a compile error anywhere else, that's expected until someone runs
    the actual build once end-to-end.**

12. **claims-service's `ClaimsAnalyticsService` imported a `ClaimSpecification`
    class that never existed anywhere in the codebase.** Same root cause as
    #11: claims-service could never have compiled as originally uploaded.
    `ClaimRepository` already correctly extended `JpaSpecificationExecutor<Claim>`
    (that part was always intact), it just had no `Specification<Claim>`
    implementation to hand it. Added `ClaimSpecification` with the three
    static filter methods (`hasStatus`, `submittedAfter`, `submittedBefore`)
    the analytics service was already calling.

    After this fix, every internal import (`com.odmip.pricing.*`,
    `com.odmip.claims.*`, `com.odmip.user.*`, and each service's vendored
    `com.odmip.common.*`) was cross-checked against the files that actually
    exist in the repo, service by service. No further missing classes
    found as of this snapshot - but given two were missed by review alone,
    treat "compiles clean" as unconfirmed until `docker compose up --build`
    actually finishes without error.

13. **All three Dockerfiles copied the wrong jar filename, causing every
    container to fail at startup with "Unable to access jarfile app.jar".**
    All three `pom.xml` files set `<finalName>` (e.g.
    `<finalName>pricing-service</finalName>`), which makes Maven produce a
    version-less jar - `target/pricing-service.jar`, not
    `target/pricing-service-1.0.0-SNAPSHOT.jar`. The Dockerfiles' `COPY
    --from=build` used a wildcard pattern (`pricing-service-*.jar`) that
    required a hyphen immediately after the service name - which never
    matched the real, hyphen-less filename. The build itself succeeded
    (Maven produced a real jar), the wildcard just matched zero files, and
    `/app/app.jar` was silently never created - so every container built
    fine and then crashed immediately on `java -jar app.jar`. Fixed by
    pointing each Dockerfile at the exact real filename
    (`pricing-service.jar`, `claims-service.jar`, `user-service.jar`).

## Known gaps — deliberately NOT fixed here, need a decision first

- **Inconsistent HTTP status codes for the same exception.**
  `BusinessRuleException` returns `400` in pricing-service but `409` in
  user-service (and a mix of both in claims-service depending on message
  text). Not breaking anything today, but worth standardizing before a
  frontend has to handle these responses consistently.
- Three separate vendored copies of the `common` module (user-service,
  pricing-service, claims-service) have drifted from each other. Not
  reconciled here since doing so risks changing each service's
  error-response shape without a real build to verify against.
- pricing-service (Spring Boot 3.3.4) and claims-service (3.3.5) are on
  different Spring Boot patch versions than user-service/common (3.3.2).

## What was NOT removed
Every fix above is additive or corrects existing broken logic. Nothing was
deleted except a stale `pricingServiceV2/target` build-output folder, which
Maven regenerates and shouldn't be version-controlled anyway.

## New feature: email-OTP registration + role-aware login

**Note on a bug introduced while adding this, then fixed:** the first pass
at this feature added a *second* top-level `spring:` block to
user-service's `application.yml` (for `spring.mail.*`), when one already
existed (for `spring.datasource`/`spring.jpa`). YAML doesn't allow
duplicate top-level keys in a single document, so user-service crashed on
startup immediately (`YamlProcessor`/`OriginTrackedYamlLoader` parse
failure - postgres, pricing-service, and claims-service all started fine
since they weren't touched). Fixed by merging both into one `spring:`
block. Every service's `application.yml` was re-validated with a real YAML
parser afterward to confirm this was the only instance of the mistake.
Requested addition, not a bug fix. Changes the registration flow from
"register → immediately logged in" to "register → account created
unverified → 6-digit OTP emailed → verify → sign in."

- `User` entity gained `firstName`, `lastName`, `emailVerified`, `otpCode`,
  `otpExpiresAt`. Login is blocked (`AppUserDetailsService`) until
  `emailVerified` is true. The seeded `admin` account is created
  pre-verified so it logs in immediately as before.
- `RegisterRequest` now requires `firstName`/`lastName`, enforces
  `@gmail.com`-only emails, and enforces password complexity (upper +
  lower + digit + special char, 6+ chars) at the API level - not just in
  the frontend, so the rule can't be bypassed by calling the API directly.
- `POST /api/auth/register` no longer returns a JWT - it returns a message
  confirming an OTP was sent. New endpoints: `POST /api/auth/verify-otp`
  (returns a JWT on success) and `POST /api/auth/resend-otp`.
- **OTP delivery defaults to console-log, not real email** -
  `odmip.otp.mail-enabled: false` in `application.yml`. Check user-service's
  terminal output for a line like `OTP for jane: 482913` when testing
  locally. To send real emails, set `odmip.otp.mail-enabled: true` and
  supply real Gmail SMTP credentials via `SMTP_USERNAME`/`SMTP_PASSWORD`
  env vars (Gmail requires an "app password," not your normal password -
  generate one at https://myaccount.google.com/apppasswords).
- Frontend: `LoginPage` now asks Admin vs. User before showing the
  login form, and rejects the login (with a clear message) if the actual
  account's role doesn't match what was selected - this is a UX guard
  only, not a security boundary; the real enforcement is still each
  service's `SecurityConfig`. `RegisterPage` is a two-step flow (form →
  OTP) with a password-visibility toggle and a live-updating password
  strength checklist (5 rules, each turns green as satisfied).
