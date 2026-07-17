# claims-service — Person C (Claims, Risk & Notifications)

## What's implemented (2-week scope)
- Claim Submission Module (`POST /api/claims`)
- Claim Validation Engine (rule-based sanity checks at submission time)
- Claim Status Tracking — explicit state machine
  (`SUBMITTED → VALIDATED → UNDER_REVIEW → APPROVED/REJECTED`, with `ON_HOLD`
  reachable from most states when fraud rules trigger)
- Fraud Detection Rules (multiple claims in a 30-day window, unusually high
  amount, missing description — each produces a `FraudFlag` row)
- Risk Scoring System (weighted heuristic → `LOW/MEDIUM/HIGH/CRITICAL` tier)
- Real-Time Alerts — `NotificationPublisher` wraps the AWS SNS SDK. SNS is
  **disabled by default** (`odmip.sns.enabled=false`) so it just logs events;
  flip the flag once the team has AWS topic ARNs and nothing else changes.
- Swagger/OpenAPI docs

## Run it
```bash
# from repo root
mvn -pl claims-service -am spring-boot:run
```
Runs on **http://localhost:8083**. Swagger: http://localhost:8083/swagger-ui.html

Fully standalone — no auth, no dependency on the other two services to run
and test the claims workflow end-to-end.

## Try it (curl)
```bash
# Submit a claim (policyId/userId are just references - no live check against
# user-service yet, see Week 3+ TODO)
curl -X POST http://localhost:8083/api/claims \
  -H "Content-Type: application/json" \
  -d '{"policyId":1,"userId":2,"claimedAmount":250.00,"description":"Lost baggage during connecting flight"}'
# -> note the "id" and "status" in the response. Small, well-described,
#    first-time claims usually auto-progress straight to VALIDATED.

# Submit a claim that WILL get auto-flagged (huge amount, no description)
curl -X POST http://localhost:8083/api/claims \
  -H "Content-Type: application/json" \
  -d '{"policyId":1,"userId":2,"claimedAmount":75000.00}'
# -> status will be ON_HOLD; check why:
curl http://localhost:8083/api/risk/claims/2/flags

# Move a claim forward through the state machine
curl -X PATCH http://localhost:8083/api/claims/1/status \
  -H "Content-Type: application/json" \
  -d '{"newStatus":"UNDER_REVIEW","note":"Assigned to underwriter"}'

curl -X PATCH http://localhost:8083/api/claims/1/status \
  -H "Content-Type: application/json" \
  -d '{"newStatus":"APPROVED","note":"Documents verified"}'

# Illegal transition example (should 409) - APPROVED claims can't move again
curl -X PATCH http://localhost:8083/api/claims/1/status \
  -H "Content-Type: application/json" \
  -d '{"newStatus":"REJECTED"}'
```

Watch the console log when you submit/transition claims — you'll see
`[SNS-disabled] Would publish ClaimStatusChangedEvent ...` lines, which is
exactly what would go out over SNS once it's turned on.

## Week 3+ TODO
- Call user-service's Policy API (same `PolicyServiceClient` pattern as
  pricing-service) to validate `policyId` actually exists and is `ACTIVE`
  before accepting a claim
- Flip `odmip.sns.enabled=true` + fill in real topic ARNs once AWS is provisioned
- Consider moving `FraudDetectionService` rules into DB-configurable rows
  (mirroring pricing-service's `PricingRule` pattern) instead of hardcoded Java
