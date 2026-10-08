---
name: pacos-change-impact
description: Review cross-cutting impact before and after a PacOS change.
---

# PacOS Change Impact Review

Use this skill before finalizing a core change.

## Impact checklist

Check whether the change affects:

- public API or SPI
- plugin discovery and lifecycle
- UI session and window lifecycle
- security and permissions
- persistence and migrations
- events and listener cleanup
- variables and scopes
- REST mappings and resources
- automation blocks
- tests
- developer or user documentation

Only mark an area irrelevant after checking the surrounding implementation.

## Diff discipline

The final diff should contain only files required for the requirement.

Look for accidental formatting churn, generated files, IDE files, unrelated refactors, version changes, dependency changes, and unrelated documentation.

## Compatibility

Search existing consumers before changing names, paths, keys, metadata, reflection targets, Spring scan paths, or routes.

A successful compile is not proof that reflection or string identifiers remain compatible.

## Completion

Implementation is not complete until tests, lifecycle cleanup, compatibility, and documentation impact have been considered.

