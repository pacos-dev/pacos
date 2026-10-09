# PacOS AI Agent Skills

Repository-local guidance for AI coding agents working on PacOS core.

The current source tree and its tests are the technical source of truth. These skills explain where to look, which conventions to preserve, and what to validate.

## Selection

- Core feature: engineering-rules, architecture, naming-and-structure, testing, change-impact, commit-review
- Plugin runtime: architecture, plugin-runtime, lifecycle-and-ui, testing, change-impact, commit-review
- UI: lifecycle-and-ui, security-and-platform, testing, change-impact, commit-review
- REST or persistence: security-and-platform, testing, change-impact, commit-review
- Naming refactor: naming-and-structure, change-impact, commit-review

## General rules

Prefer existing patterns over new abstractions.

Before changing a contract, inspect implementations, consumers, tests, configuration, and affected documentation.

Do not infer an extension point from a class name. Verify it in the current source.

ExecutableBlock is a PacOS automation SPI and is not part of the PluginDataLoader discovery path.

Changes involving plugin loading, classloaders, Spring contexts, Vaadin sessions, listeners, executors, or static state require explicit lifecycle and leak review.

## Validation

Before proposing a commit:

1. Inspect changed files and complete surrounding context.
2. Re-check syntax, imports, and current API signatures.
3. Re-check semantic compatibility.
4. Re-check the complete diff for accidental scope.
5. Re-check tests and documentation impact.

Repeat the validation sequence 2–3 times for every suggested commit.

Never claim a build or CI run unless it was actually executed.

