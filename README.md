# On-Demand Micro Insurance Platform (od-mip)

A modular-monolith-leaning-microservices platform built by a 3-person team,
split into 3 independently runnable Spring Boot services:

| Service | Owner | Port | Responsibility |
|---|---|---|---|
| `user-service` | **Person A** | 8081 | Auth (JWT), RBAC, Policy CRUD, Templates, Expiry Engine, Admin Panel |
| `pricing-service` | **Person B** | 8082 | Premium calc, dynamic pricing, coupons, usage tracking, analytics, dashboard |
| `claims-service` | **Person C** | 8083 | Claim submission/validation/status tracking, fraud detection, risk scoring, SNS alerts |
| `common` | shared | — | DTOs, exceptions, event contracts used by all 3 services |

Each service is a fully independent Spring Boot application with its own
`main()`, own port, own `application.yml`, and its own embedded H2 database
by default — **no one needs Postgres, Docker, or another teammate's service
running to start coding on day 1.**

## Repo layout

```
od-mip/
├── pom.xml                 (parent/reactor POM - shared dependency versions only)
├── docker-compose.yml       (spins up all 3 services + Postgres together, for integration testing)
├── common/                  (shared DTOs / exceptions / event contracts)
├── user-service/            (Person A)
├── pricing-service/         (Person B)
└── claims-service/          (Person C)
```

## Running a single service (what each person does day-to-day)

From the repo root:

```bash
# Person A
mvn -pl user-service -am spring-boot:run

# Person B
mvn -pl pricing-service -am spring-boot:run

# Person C
mvn -pl claims-service -am spring-boot:run
```

`-am` ("also make") builds the `common` module first since every service
depends on it. Each service uses an in-memory H2 database by default, so
`mvn spring-boot:run` is genuinely all you need — no external DB setup.

Swagger UI for each service:
- User/Policy: http://localhost:8081/swagger-ui.html
- Pricing/Usage: http://localhost:8082/swagger-ui.html
- Claims/Risk: http://localhost:8083/swagger-ui.html

## Running everything together (integration check)

Once all 3 people have pushed and you want to test cross-service calls
(e.g. pricing-service or claims-service calling user-service's Policy API):

```bash
mvn clean package -DskipTests     # builds jars for all 4 modules
docker compose up --build         # brings up Postgres + all 3 services, wired via service names
```

## Git workflow — how 3 people push to Person C's repo

Person C created the shared GitHub repo. Everyone pushes to it like this:

1. **Person C** (repo owner) adds Person A and Person B as collaborators
   (GitHub → repo → Settings → Collaborators), or the team agrees on a
   fork+PR workflow if preferred.

2. **Everyone clones the repo once:**
   ```bash
   git clone https://github.com/<person-c-username>/od-mip.git
   cd od-mip
   ```

3. **Each person works on their own branch**, named after their service:
   ```bash
   git checkout -b feature/user-service      # Person A
   git checkout -b feature/pricing-service    # Person B
   git checkout -b feature/claims-service     # Person C
   ```

4. **Drop in the code** (each person copies the folder Claude generated for
   them — `user-service/`, `pricing-service/`, or `claims-service/` — into
   their local clone of the repo, replacing the empty placeholder if one
   exists). Person A also adds the root-level `pom.xml`, `common/`,
   `docker-compose.yml`, `.gitignore`, and `README.md` first (see step 5),
   since everyone else's build depends on `common`.

5. **Order matters on the very first push:**
   - Person A pushes `common/` + root `pom.xml` + this `README.md` +
     `.gitignore` + `user-service/` to `main` (or via PR) **first**, since
     `pricing-service` and `claims-service` both declare a Maven dependency
     on `common`.
   - Person B and Person C then pull `main`, branch off it, add their own
     service folder, and open a PR (or push directly if the team is
     comfortable skipping PRs for a 3-person project).

6. **Day-to-day after that:**
   ```bash
   git pull origin main            # get latest common/ changes before you start
   # ... make changes inside your own service folder ...
   git add <your-service>/
   git commit -m "feat(user-service): add policy expiry scheduler"
   git push origin feature/user-service
   # open a PR into main, or push straight to main if the team agrees
   ```

7. **Touching `common/`?** Since all 3 services depend on it, changes there
   affect everyone. Whoever changes a shared DTO/event/exception should
   message the other two before pushing, and everyone should `git pull`
   and re-run `mvn -am` after such a change lands.

8. **Merge conflicts** will mostly happen in `common/` or this README —
   your actual service code lives in separate folders, so day-to-day work
   rarely conflicts between people.

## Cross-service integration points (agreed contracts)

- **`PolicyDTO`** (`common/.../dto/PolicyDTO.java`) — the shape pricing-service
  and claims-service should use when reading policy data, instead of
  depending on user-service's internal `Policy` entity.
- **`PolicyExpiringEvent`, `ClaimStatusChangedEvent`, `FraudFlaggedEvent`**
  (`common/.../event/*.java`) — SNS message contracts. Topics aren't wired to
  real AWS yet (see `odmip.sns.enabled=false` in claims-service); events are
  logged instead until the team provisions SNS topics.
- **`PolicyServiceClient`** in `pricing-service` — a `WebClient` wrapper that
  calls user-service's `GET /api/policies/{id}`. It fails soft (returns
  empty) if user-service isn't running, so Person B can develop without
  Person A's service up.

## What's in this 2-week cut vs. later

This drop covers the REST/JWT/JPA/Swagger core of every feature on the
team's board so all 3 people have something runnable and demoable in the
first sprint. Deliberately deferred to week 3+ (flagged with `TODO` comments
in the code where relevant):
- GraphQL layer (on top of the existing REST controllers)
- Real Kafka/RabbitMQ/SNS wiring (SQS/SNS client is included in
  claims-service but disabled by default — flip `odmip.sns.enabled=true` once
  AWS resources exist)
- MongoDB for analytics/read-heavy views
- Kubernetes manifests (docker-compose is enough for local integration testing)
- CI/CD (Jenkins) pipeline definitions
