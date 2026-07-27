# ADR-0002 - Store instrument metadata in JSONB

- Status: Accepted
- Date: 2026-06-02
- Deciders: ReconX team

## Context

`instruments` needs flexible attributes that vary by asset class (for example
sector, tenor, issuer, contract details). Forcing these into rigid relational
columns causes frequent migrations and sparse tables. We still need indexed
lookup for dashboard filters and investigations.

## Decision

Add `metadata JSONB` to `instruments` for variable attributes, keeping core
canonical fields (`symbol`, `asset_class`, `currency`, `isin`) as regular columns.
Use JSONB containment queries for flexible filtering and maintain schema-like
conventions in ingestion code.

## Consequences

Positive:
- Supports heterogeneous instrument attributes without weekly migrations.
- Enables rich filter use-cases with JSON operators.
- Keeps relational core stable while allowing controlled flexibility.

Negative:
- Weak DB-level schema enforcement for JSON structure.
- Query performance depends on careful indexing strategy.
- Requires ingestion validation to keep metadata shape consistent.
