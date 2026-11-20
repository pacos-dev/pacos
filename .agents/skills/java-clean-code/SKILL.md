---
name: java-clean-code
description: Apply Java import conventions, clean code and SOLID principles when creating, reviewing or refactoring Java code in this repository.
---

# Java clean code conventions

## Imports

- Always use regular Java imports for referenced classes and interfaces.
- Do not use fully qualified class names inline in Java declarations, expressions, method references, generic types or annotations.
- Example: replace `java.util.Arrays.equals(left, right)` with an import of `java.util.Arrays` and `Arrays.equals(left, right)`.
- Keep imports explicit; do not use wildcard imports.
- Keep imports grouped consistently: JDK, Jakarta/Javax, third-party libraries, then project packages.
- The exception is a fully qualified class name inside a JPQL/HQL query string when the query language requires it, such as `SELECT new org.example.dto.Result(...)`. Java imports
  do not apply inside query strings.
- Do not add imports for types used only inside JPQL/HQL string literals.

## Clean code

- Keep methods focused on one responsibility and make their names describe the observable operation.
- Extract cohesive policy, mapping, validation and infrastructure concerns into separate classes when this reduces coupling and makes the behavior independently testable.
- Avoid god classes, duplicated conditions, deeply nested branches and unnecessary mutable state.
- Prefer immutable value objects and defensive copies at boundaries.
- Keep domain decisions out of UI and transport infrastructure.
- Do not introduce abstractions that only wrap a single call without isolating a meaningful responsibility.

## SOLID

- Apply Single Responsibility by separating distinct reasons for change.
- Apply Open/Closed and Dependency Inversion where extension points or interchangeable policies are genuinely needed; avoid speculative interfaces.
- Keep interfaces small and consumers dependent only on the behavior they use.
- Preserve existing public contracts unless a change is explicitly required.
- Refactor incrementally and retain or add tests for behavior and edge cases.

## Verification

- Run the relevant Maven tests and formatting/static analysis after refactoring.
- Do not report a build or test as successful unless it was executed and its result was observed.

## UI event subscription lifecycle

- Event listeners registered on a Vaadin component must not outlive that component.
- Prefer `SystemEvent.subscribeOnAttached(component, eventType, notifier)` for component-scoped subscriptions. It registers the listener on attach and unregisters it on detach.
- Do not use `subscribe()` for component-owned listeners unless the component explicitly retains the exact notifier instance and reliably calls `unsubscribe()` during
  detach/removal.
- Review every listener registration when adding or refactoring UI components. Verify that closing/removing a tab releases its listeners and that reopening it does not accumulate
  duplicate subscriptions.
- Do not assume removing a component from a layout automatically unregisters listeners held by an external event publisher.
