---
name: pacos-core-architecture
description: Select the correct PacOS module and component layer before implementing a core change.
---

# PacOS Core Architecture

Use this skill when adding, moving, or refactoring core functionality.

## Module selection

Use pacos-base for plugin-facing APIs and SPIs.

Use pacos-core for user-facing PacOS behavior, plugin management, platform services, security coordination, desktop and window functionality.

Use pacos-common for reusable implementations that are not core orchestration.

Use engine for initialization and runtime bootstrapping.

Use pacos-config for global configuration.

Use starter for application assembly and dependency packaging.

Do not introduce a base API only because another core class could call it. A new SPI needs a real extension boundary.

## Core component layers

The common feature structure is:

org.pacos.core.component.<feature>.<layer>

Typical layers are domain, dto, repository, service, proxy, and view.

Service means business or application orchestration.

Proxy means a facade across a subsystem or boundary.

Manager means genuine subsystem coordination, not a generic synonym for service.

Config, Listener, and Event should describe real configuration, listener, and event responsibilities.

## Spring scanning

Core scanning is explicit in SpringCoreConfig.

New components must land in packages covered by the existing scan or update configuration deliberately.

Do not broaden component scanning as a convenience for a single feature.

## Decision process

Before creating a class, identify:

1. owning module
2. owning layer
3. actual responsibility
4. closest existing pattern
5. object lifecycle

If any answer is unclear, inspect more source before coding.

