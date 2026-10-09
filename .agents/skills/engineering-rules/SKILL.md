---
name: pacos-engineering-rules
description: Baseline rules for safe, minimal PacOS core changes.
---

# PacOS Engineering Rules

Use this skill for every non-trivial PacOS core change.

## Source of truth

The current source tree and its tests are authoritative. Documentation is useful context but may lag behind implementation.

Read the target type, direct callers, implementations, tests, and relevant configuration before designing a change.

## Change shape

Prefer the smallest change that solves the requirement.

Preserve module boundaries, Spring conventions, package structure, bean scopes, executors, scheduling, and exception behavior unless the requirement changes them.

Use constructor injection. Keep dependencies explicit.

Use project logging conventions. Do not introduce System.out for application logging.

Keep public APIs stable unless a contract change is required. Search all implementations and consumers before changing a contract.

## Module roles

starter is application and dependency packaging.

pacos-config is global configuration.

engine is initialization and runtime bootstrapping.

pacos-core owns core business logic, plugin management, platform services, desktop and window behavior.

pacos-common contains reusable implementations.

pacos-base contains plugin APIs, SPIs, and integration foundations.

Do not move code across these boundaries without an explicit reason and impact analysis.

## Repository hygiene

Do not commit generated frontend output, IDE metadata, local runtime state, or test artifacts unless the repository explicitly expects them.

## Done criteria

Implementation, tests, compatibility, lifecycle, and documentation impact have all been considered.

