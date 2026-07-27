# Day 1 Branch Protection Checklist (TICKET-ADV001)

Apply these settings in GitHub repository Settings -> Branches.

## Rule: main

- Require a pull request before merging: enabled
- Required approvals: 2
- Dismiss stale pull request approvals: enabled
- Require review from Code Owners: enabled
- Require status checks to pass before merging: enabled
- Require branches to be up to date before merging: enabled
- Required checks: build, test, lint
- Require conversation resolution before merging: enabled
- Require linear history: enabled
- Include administrators: enabled
- Allow force pushes: disabled
- Allow deletions: disabled

## Rule: develop

- Require a pull request before merging: enabled
- Required approvals: 1
- Require status checks to pass before merging: enabled
- Required checks: build, test
- Include administrators: disabled

## Verification

1. Push a throwaway commit directly to main; GitHub must reject it.
2. Open a PR into main; required checks/reviews should appear automatically.
