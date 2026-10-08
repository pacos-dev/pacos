---
name: pacos-naming-and-structure
description: Apply PacOS package and class naming conventions and avoid suffix-driven design errors.
---

# PacOS Naming and Structure

Use this skill when adding, renaming, or reorganizing PacOS classes.

## Packages

For feature components prefer org.pacos.core.component.<feature>.<layer>.

For cross-cutting system services use the established org.pacos.core.system.<layer> structure.

Do not create a new top-level package pattern when an existing one fits.

## Class names

Use names that describe responsibility:

- FooService for business or application services
- FooRepository for persistence access
- FooProxy for boundary or subsystem facades
- FooDTO for transport or data contracts
- FooMapper for explicit mapping
- FooConfig for configuration
- FooManager for subsystem coordination
- FooListener for event listeners
- FooEvent for event payloads

Do not call a simple service a Manager or a facade a Service just because the name is shorter.

## Refactors

For renames search declarations, imports, bean references, component scans, routes, reflection strings, tests, and docs.

Compile success alone does not prove reflection-based or string-based consumers remain valid.

