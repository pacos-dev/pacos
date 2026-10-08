---
name: pacos-testing
description: Choose the cheapest correct test level and preserve PacOS test conventions.
---

# PacOS Testing

Use this skill whenever production code changes.

## Test hierarchy

Prefer the cheapest test that proves the behavior:

1. pure unit test
2. Spring context test
3. MVC or REST test
4. Vaadin or UI test
5. broader integration test

Use a higher level only when the framework or runtime contract requires it.

## Existing infrastructure

Reuse current test helpers and patterns.

For Vaadin tests, inspect current-instance setup before adding new mocks.

For external HTTP integration, use existing WireMock and test configuration.

## Test naming

Use method names in the form whenCallMethodThenExpectedResult.

Avoid comments inside tests. Intent should be clear from the test name and assertions.

## Coverage focus

Runtime code should cover registration and cleanup when applicable.

Security changes should cover allowed and denied behavior.

Lifecycle changes should cover start, stop, repeated execution, and resource release when relevant.

Persistence changes should cover schema and repository or service behavior.

REST changes should cover status, mapping, contract, and authorization as appropriate.

## Side effects

Some integration tests may write generated artifacts. Inspect the current test before running a build and do not silently treat such files as source changes.

