# 0003: Deny requests by default

Status: Accepted
Date: 2026-10-22

## Context
The security configuration ended with a catch-all rule that allowed any
logged-in user to reach any URL under the API prefix. An endpoint that
was added later, or that someone forgot to list, was therefore open to
every authenticated user without anyone deciding that. Rules are also
evaluated in order and the first match wins, which earlier hid the
SUPER_ADMIN restrictions behind a general rule.

## Decision
End the rule list with `anyRequest().denyAll()` and list every
reachable endpoint explicitly, from specific to general:
- ERROR dispatches and CORS preflight (OPTIONS) are permitted.
- Public: the auth endpoints, Swagger/OpenAPI pages, and `/actuator/health`.
- SUPER_ADMIN only: admin create, edit and toggle-status.
- Any logged-in user: the admin, queries, logs and generatereport prefixes.
- Everything else is denied.
  The previous `/api/v1/queryapplication/**` catch-all is removed.
  This change keeps existing permissions the same; it only changes
  what happens to anything not listed.

## Alternatives considered
- Keep an `authenticated()` catch-all: simpler, but a forgotten
  endpoint fails open.
- Per-method `@PreAuthorize` instead of URL rules: useful for
  object-level checks later (e.g. "only your own logs"), but it does
  not give a safe default on its own.

## Consequences
- New endpoints return 403 until a rule is added; this is a deliberate
  small cost.
- A URL that matches no rule is denied (403) rather than returning 404.
- Everything under `/auth/` is public, so new endpoints there need
  review.
- Follow-up: automated access tests (401/403/200 per group) so the
  rule list cannot drift from the controllers.
- Follow-up: object-level checks for logs (who may see whose) are a
  separate decision.