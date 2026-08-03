# user-service — Person A (Foundation & Policy Core)

## What's implemented (2-week scope)
- User Registration & Login (JWT, Spring Security, BCrypt)
- Role-Based Access Control (`ROLE_USER`, `ROLE_ADMIN`, `ROLE_UNDERWRITER`)
- Dynamic Policy Templates (CRUD, admin-only writes)
- On-Demand Policy creation from a template (`DRAFT` → `ACTIVE` → `EXPIRED`/`CANCELLED`)
- Policy Activation & Expiry Engine (`@Scheduled` job, flips overdue `ACTIVE` policies to `EXPIRED` every 5 min)
- Admin Panel (list users/policies, disable a user)
- Swagger/OpenAPI docs
- A `DataSeeder` that creates an `admin` user and 2 sample templates on boot

## Run it
```bash
# from repo root
mvn -pl user-service -am spring-boot:run
```
Runs on **http://localhost:8081**. Swagger: http://localhost:8081/swagger-ui.html
H2 console (if you want to peek at data): http://localhost:8081/h2-console (JDBC URL `jdbc:h2:mem:userdb`)

Default seeded admin: `admin` / `Admin@123`

## Try it (curl)
```bash
# Register a normal user
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"jane","email":"jane@example.com","password":"password1"}'

# Login as admin
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
# -> copy the "token" from the response

# List templates (any authenticated user)
curl http://localhost:8081/api/templates -H "Authorization: Bearer <TOKEN>"

# Create a policy for user id 2 from the seeded TRAVEL_1DAY template
curl -X POST http://localhost:8081/api/policies \
  -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"userId":2,"templateCode":"TRAVEL_1DAY"}'

# Activate it (use the id returned above)
curl -X POST http://localhost:8081/api/policies/1/activate -H "Authorization: Bearer <TOKEN>"
```

## Switching to Postgres later
Run with `-Dspring-boot.run.profiles=postgres` (or `SPRING_PROFILES_ACTIVE=postgres`)
after starting a local Postgres with a `odmip_users` database — see root `docker-compose.yml`.

## Week 3+ TODO (left as comments in code)
- Publish `PolicyExpiringEvent` from `PolicyExpiryScheduler` once SNS is wired (currently just logs)
- Wire pricing-rule management into `AdminController` once `pricing-service`'s `PricingRule` is stable
