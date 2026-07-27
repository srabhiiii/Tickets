# ADR-0001 - Partition the `trades` table by `trade_date`

- Status: Accepted
- Date: 2026-06-02
- Deciders: ReconX team

## Context

`trades` is our highest-volume table: about 50k inserts/day and 5-year retention,
which projects to about 91M rows. Most recon and dashboard queries are filtered by
date range (usually one day or month). A single unpartitioned table makes retention
operations expensive and slows range reads.

## Decision

Partition `trades` by RANGE on `trade_date` with one partition per calendar month.
The primary key includes `trade_date` to satisfy PostgreSQL partitioning rules.
Child partitions are named `trades_yYYYYmMM` and pre-created by a monthly job.
A default partition catches out-of-range inserts and raises alerts.

## Consequences

Positive:
- Partition pruning reduces scanned data for date filters.
- Retention and archival can use detach/drop partition operations.
- Per-partition indexes are smaller and cheaper to maintain.

Negative:
- Composite PK `(id, trade_date)` increases ORM mapping complexity.
- Cross-partition uniqueness for business keys needs explicit design.
- Partition lifecycle needs operational automation and monitoring.
