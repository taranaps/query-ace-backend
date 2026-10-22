# 0001: Single Spring Boot application (layered monolith)

Status: Accepted
Date: 2026-10-22

## Context
ACE is a query management system for support teams: searching and
managing queries and answers, with SUPER_ADMIN and ADMIN accounts,
activity logs, and bulk lookup. It is built and maintained by one
developer, with a separate frontend. The load is small and the domain
is one coherent area backed by one MySQL database. An earlier layout
had login in a separate backend; the current code has authentication
and the rest of the API together.

## Decision
Keep the backend as a single deployable Spring Boot application
(Java 21, Spring Boot 3.4), organised in layers: controller, service,
repository, security and config packages. Authentication (JWT) lives
inside the same application. The React frontend is a separate
application that talks to it over REST.

## Alternatives considered
- Microservices: independent scaling and deployment, but it adds
  network calls, distributed data, deployment and monitoring overhead
  that a small team and a small domain do not need.
- Separate login service: isolates authentication, but needs shared
  tokens or keys and two things to run for no current benefit.
- Modular monolith (packages grouped by feature with enforced
  boundaries): a reasonable next step if the codebase grows; not
  needed yet.

## Consequences
- One thing to build, run, test and deploy; local setup is simple.
- Database transactions stay simple, since everything shares one schema.
- The whole app scales and fails as a unit.
- Layered packages can let features become tangled over time. If that
  happens, regrouping by feature is the first step before any split
  into services.