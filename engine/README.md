# engine

The `engine` module is PacOS's relatively stable runtime artifact. It packages the infrastructure and resources needed to launch the application, including the Spring and Vaadin versions and the production-optimized frontend.

## Architectural boundary

The engine is intentionally separated from the PacOS system modules that are selected and updated at deployment or startup time.

- **Engine:** owns the stable runtime and infrastructure dependencies (including Spring and Vaadin) and changes less frequently than the system modules.
- **Starter:** decides which versions of PacOS system modules are used and prepares the classpath used to launch the engine.
- **System modules:** are resolved independently, so the platform can load the appropriate versions without rebuilding the engine for every system-module update.

Dependencies to locally deployed system modules may be declared during development but are excluded from the final engine artifact where needed. This is intentional: the runtime module set is supplied by the starter, rather than being permanently bundled into the engine.

Keep this boundary in mind when changing Maven dependencies or packaging. Do not add system modules back into the production engine artifact merely to simplify local development. Validate both development execution and the production packaging/classpath behavior.

## Key components

### Spring and Hibernate

Provide dependency injection, transaction management and ORM/database integration.

### Vaadin

Provides the UI runtime and production-precompiled frontend resources.

## Execution

In a deployed application, the `starter` module initializes the environment, selects system module versions and launches the engine with the resulting classpath.

For local development, the engine application can be launched directly from the IDE using `org.pacos.core.Application`; this path does not replace the starter's role in deployment-time module selection.

## Build

Build the engine artifact with:

```bash
mvn clean package
```

When changing build configuration, verify the packaged artifact as well as the local development path. The expected production result is a stable runtime JAR without the locally deployed system-module artifacts that the starter resolves separately.
