---
name: pacos-commit-review
description: Mandatory final validation gate for PacOS commit proposals.
---

# PacOS Commit Review

Use this skill immediately before suggesting a commit.

## Pass 1: structure

Check changed files, scope, package placement, imports, names, API changes, generated artifacts, and unrelated edits.

## Pass 2: syntax and semantics

Re-read complete changed files.

Verify Java syntax, Spring annotations, bean names, generic types, framework API usage, null assumptions, test APIs, and current repository contracts.

## Pass 3: integration and diff

Review the final diff again.

Re-check requirement alignment, tests, lifecycle, security, persistence, REST, automation, and documentation impact.

## Required discipline

Repeat these three checks 2–3 times for every commit you suggest.

Do not claim a Maven, frontend, test, or CI run unless it was actually executed.

Prefer one coherent commit per logical change.

The commit message must describe the actual repository change.

