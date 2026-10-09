---
name: pacos-plugin-runtime
description: Work safely with PacOS plugin loading, Spring contexts, classloaders, discovery, startup and shutdown.
---

# PacOS Plugin Runtime

Use this skill for PluginManager, plugin loading, plugin discovery, startup, shutdown, or classloader-related work.

## Runtime model

PacOS loads a plugin JAR with a dedicated plugin classloader and creates a dedicated AnnotationConfigApplicationContext with the PacOS core context as parent.

Plugin configuration is discovered from org.pacos.plugin.<pluginName>.config.

Keep plugin implementation types inside the plugin boundary.

## Discovery

PluginDataLoader currently discovers WindowConfig, SettingTab, VariableProvider, PluginListener, Vaadin RequestHandler, and REST mappings.

When plugin resources require handling and no explicit request handler exists, the runtime may create the default PluginRequestHandler.

ExecutableBlock is separate. Automation discovery is a PacOS base SPI and must not be folded into PluginDataLoader assumptions.

## Lifecycle

PluginManager initializes plugins on application readiness and starts and stops plugin contexts asynchronously using the configured executor.

When stopping a plugin, verify release of:

- plugin Spring context
- plugin classloader
- variable providers
- REST handlers and mappings
- extension registrations
- listeners and callbacks
- executor or scheduler tasks
- UI references
- static references

Never retain a plugin implementation object in a long-lived core singleton without an explicit removal path.

## Contract changes

For discovery or lifecycle changes inspect registration, runtime use, stop and unload, tests, and resource cleanup.

Plugin unloadability is a correctness property.

