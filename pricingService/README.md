# pricing-service — Person B (Pricing, Usage & Insights)

## What's implemented (2-week scope)
- Time-Based Premium Calculation (scales premium with coverage duration)
- Dynamic Pricing Engine (risk / location / usage multipliers, seeded rule set)
- Discount & Coupon System (create coupons, validate + redeem against a quote)
- Real-Time Usage Tracking (`POST /api/usage`)
- Policy Utilization Analytics (`GET /api/usage/policy/{id}/total`)
- User Dashboard aggregation endpoint (combines policy status + usage)
- `PolicyServiceClient` — calls Person A's user-service Policy API, fails soft if it's not running yet
- Swagger/OpenAPI docs

## Run it
```bash
# from repo root
mvn -pl pricing-service -am spring-boot:run
```
Runs on **http://localhost:8082**. Swagger: http://localhost:8082/swagger-ui.html

This works standalone with no auth and no dependency on user-service actually
running — the dashboard endpoint will just return empty policy data if
user-service isn't up, everything else works fully offline.

## Try it (curl)
```bash
# Get a premium quote
curl -X POST http://localhost:8082/api/pricing/quote \
  -H "Content-Type: application/json" \
  -d '{"basePremium":4.99,"riskCategory":"MEDIUM","location":"URBAN","usageLevel":"MODERATE","durationHours":24}'

# Create a coupon
curl -X POST http://localhost:8082/api/coupons \
  -H "Content-Type: application/json" \
  -d '{"code":"WELCOME10","discountPercent":10,"validFrom":"2026-01-01T00:00:00","validUntil":"2027-01-01T00:00:00","maxRedemptions":100}'

# Quote again, this time with the coupon applied
curl -X POST http://localhost:8082/api/pricing/quote \
  -H "Content-Type: application/json" \
  -d '{"basePremium":4.99,"riskCategory":"MEDIUM","durationHours":24,"couponCode":"WELCOME10"}'

# Record usage against a policy
curl -X POST http://localhost:8082/api/usage \
  -H "Content-Type: application/json" \
  -d '{"policyId":1,"userId":2,"usageType":"TRIP","quantity":12.5}'

# Total usage for that policy
curl http://localhost:8082/api/usage/policy/1/total
```

## Cross-service dependency
If you want the `/api/dashboard/{userId}` endpoint to return real policy data
(not just empty entries), have `user-service` running on port 8081 at the
same time — see root README for the shared contract (`PolicyDTO`).

## Week 3+ TODO
- Replace the manual `policyIds` query param on `DashboardController` with a
  single call to `GET /api/policies/user/{id}` once that response shape is
  finalized with Person A
- Persist calculated premiums back onto the Policy record in user-service
  (needs a write endpoint from Person A, or an event-driven update)
