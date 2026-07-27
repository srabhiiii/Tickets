# Day 1 Board Template (TICKET-ADV016)

Use this template in Jira or GitHub Projects.

## Epics

- RECONX-E1 - Day 1: Architecture and Setup (ADV001-ADV004)
- RECONX-E2 - Day 1: Schema and Analytics (ADV006-ADV011)
- RECONX-E3 - Day 1: Liquibase and Tooling (ADV012-ADV017)

## Columns

Backlog -> To Do -> In Progress -> In Review -> Done

## Card Fields

- Exercise ID (example: TICKET-ADV007)
- Estimate (1, 2, 3, 5, 8)
- Owner
- Linked PR
- Acceptance criteria

## GitHub Projects Field Schema

```yaml
fields:
  - name: Exercise ID
    type: text
  - name: Estimate
    type: single_select
    options: [1, 2, 3, 5, 8]
  - name: Owner
    type: assignee
  - name: Linked PR
    type: text
  - name: Status
    type: single_select
    options: [Backlog, To Do, In Progress, In Review, Done]
```

## Required Cards

Create one card each for:

- TICKET-ADV001
- TICKET-ADV002
- TICKET-ADV003
- TICKET-ADV004
- TICKET-ADV006
- TICKET-ADV007
- TICKET-ADV008
- TICKET-ADV009
- TICKET-ADV010
- TICKET-ADV011
- TICKET-ADV012
- TICKET-ADV013
- TICKET-ADV014
- TICKET-ADV015
- TICKET-ADV016
- TICKET-ADV017
