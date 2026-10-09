---
name: pacos-security-and-platform
description: Apply PacOS security, REST, database, variables, settings, events, resources, and automation contracts.
---

# PacOS Security and Platform Contracts

Use this skill for security, REST, persistence, variables, settings, resources, events, or automation.

## Security

Permission keys are stable identifiers.

Do not confuse UI visibility with authorization.

Use UserSession.hasPermission(...) for permission checks and hasActionPermission(...) when the platform action-denial notification semantics are intended.

Authorize at the action boundary, not only at the button or route.

For plugin-to-plugin PacOS APIs, use InternalApiAccess and treat token creation and revocation as part of the contract.

## REST and resources

Follow existing Spring MVC mappings.

Use the plugin resource handling model for plugin static resources and custom request handlers.

Keep API DTOs separate from domain entities where that is the established component pattern.

## Database

Preserve existing datasource, entity-manager, repository, transaction-manager, and migration boundaries.

Schema changes require matching Flyway migrations and tests.

Do not change persistence bean names or migration locations casually.

## Variables and settings

VariableProvider is discovered automatically by the current plugin runtime.

Use VariableManager for registration, update, removal, and scope operations.

SettingTab is a plugin extension point discovered from the plugin context.

Do not resurrect old manual registration patterns when current runtime discovery replaces them.

## Events and automation

Use SystemEvent patterns for events and subscriptions.

ExecutableBlock is the PacOS automation SPI. Its metadata and Camunda delegate name are compatibility-sensitive and the delegate name must be globally unique.

Do not mix automation discovery with the general plugin data loader.

## Compatibility

Treat permission keys, REST paths, variable names and scopes, database identifiers, migration locations, event payloads, and automation delegate names as compatibility surfaces.

