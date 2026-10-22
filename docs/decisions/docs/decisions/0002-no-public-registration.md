# 0002: No public registration endpoint

Status: Accepted
Date: 2026-10-22

## Context
The backend exposed `POST /auth/register`, open to anyone and taking
the user's role from the request body. Anyone could create an account
with the ADMIN or SUPER_ADMIN role. The requirements (BRD) never ask
for self-service sign-up; accounts are managed by administrators.
The endpoint was originally added only to create test users.

## Decision
Remove the endpoint, its service method and its request DTO.
Accounts are created only by:
- the startup seeder (the first SUPER_ADMIN), and
- `POST /admin/create`, restricted to SUPER_ADMIN.

## Alternatives considered
- Keep it, restricted to SUPER_ADMIN: duplicates `/admin/create`.
- Keep it public but force a default role: still allows anyone to
  create accounts, which the requirements do not call for.

## Consequences
- Smaller attack surface; no self-assigned roles.
- Test users are created via `/admin/create` (not by direct SQL,
  because passwords must be BCrypt-hashed).
- The first admin must come from the seeder.