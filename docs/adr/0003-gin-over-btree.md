# ADR-0003 - Use GIN index for JSONB metadata filters

- Status: Accepted
- Date: 2026-06-02
- Deciders: ReconX team

## Context

Recon analysts filter instruments by metadata keys and values (for example
`sector=Banking`, `tenor=10Y`). A btree index is ineffective for deep JSONB
containment predicates. With about 50k trades/day and frequent lookups, metadata
queries must stay responsive under load.

## Decision

Create a GIN index on `instruments.metadata` using `jsonb_path_ops` for
containment-heavy query patterns. Keep btree indexes on canonical scalar columns
and do not attempt to replace them with GIN.

## Consequences

Positive:
- Material improvement for JSONB containment predicates.
- Works naturally with `@>`-style filters used in investigation workflows.
- Preserves clear division: btree for scalar keys, GIN for JSONB.

Negative:
- Extra storage and write overhead for index maintenance.
- `jsonb_path_ops` optimizes a subset of JSONB operators.
- Requires periodic review if query shapes evolve.
