# OD-MIP Frontend

React + TypeScript + Redux Toolkit (RTK Query) frontend for the On-Demand
Micro Insurance Platform. Talks directly to the three backend services -
no gateway, no BFF.

## Setup

```bash
npm install
npm run dev
```

Opens at http://localhost:5173.

## Before running, the backend needs

1. All three services running (`mvn spring-boot:run` in `user-service`,
   `pricing-service`, `claims-service` - default ports 8081/8082/8083).
2. CORS enabled on all three - already added in the backend's `FIXES.md`
   item 7. If you're on an older backend copy without that fix, requests
   from this frontend will fail silently in the browser console with a
   CORS error, not a clear error message.
3. `GET /api/auth/me` on user-service (`FIXES.md` item 8) - the frontend
   uses this right after login to resolve its own numeric userId.

## Config

`.env` sets the three API base URLs (defaults match the ports above):

```
VITE_USER_API_URL=http://localhost:8081
VITE_PRICING_API_URL=http://localhost:8082
VITE_CLAIMS_API_URL=http://localhost:8083
```

## Demo login

The backend seeds an admin account: `admin` / `Admin@123` (pre-filled on
the login screen). Log in as admin to see the Admin Panel nav item and
pricing-rule/fraud-rule management; register a second, regular account to
demo the ordinary user flow (buy a policy, get a quote, file a claim).

## What's wired up

Every page calls real endpoints - nothing is mocked. See `src/api/*.ts`
for the exact RTK Query definitions per service.

- **Dashboard** - pricing-service's `/api/dashboard/{userId}` + policy overview
- **Policies** - create from template, activate/cancel, premium history
- **Pricing & Coupons** - live quote engine, coupon list/creation, usage logging
- **Claims** - submit, browse, risk score + fraud flags, status transitions
- **Admin** (ROLE_ADMIN only) - users, all policies, pricing rules, fraud rules

## Build

```bash
npm run build
```

Verified clean (`tsc -b` + `vite build`, zero errors/warnings) as of this
snapshot.
