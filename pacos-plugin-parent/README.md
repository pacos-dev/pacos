# PacOS Plugin Parent

The `org.pacos:pacos-plugin-parent` Maven parent centralizes the Java/Spring/Vaadin baseline, PacOS BOM alignment, common dependencies supplied by the host runtime, test libraries and build-plugin defaults for PacOS plugins.

## Use it in a plugin

Set it as the plugin project's Maven parent:

```xml
<parent>
    <groupId>org.pacos</groupId>
    <artifactId>pacos-plugin-parent</artifactId>
    <version>3.4.1-SNAPSHOT</version>
    <relativePath/>
</parent>
```

Use the parent version published for the PacOS release that will host the plugin. For local development against this source checkout, build/install the matching PacOS reactor first. For a separate repository, the parent and its matching `pacos-bom` must already be published to the configured PacOS Maven repository.

The parent inherits Spring Boot's baseline and imports the PacOS BOM. Plugin POMs should not import a second PacOS BOM or Vaadin BOM, or repeat versions for dependencies already managed by the platform.

## Dependency rules

- Common PacOS APIs, Spring/Vaadin libraries and common runtime utilities are inherited as `provided`. They are available for compilation and tests but should not be bundled again into the plugin's shaded JAR.
- Keep only plugin-owned/runtime-specific dependencies in the plugin's own `<dependencies>`. Do not use `provided` for an extra library the plugin needs to package itself.
- Keep library versions in the PacOS BOM when the library is part of the shared platform baseline. Use an explicit version only for a truly plugin-specific library not managed by the BOM.
- Test libraries are inherited with `test` scope.
- Plugin-specific build executions (for example Shade, Vaadin frontend preparation, or deployment of plugin metadata and icons) remain in the plugin POM when needed; the parent centralizes their version and common configuration, not every plugin's execution details.

## Build and compatibility

Build a plugin with:

```bash
mvn clean package
```

Check the distributable shaded JAR and confirm it contains plugin code/resources and plugin-owned runtime libraries, not duplicate PacOS/Spring/Vaadin platform classes. Test installation against the PacOS version named by the parent baseline.

The parent version deliberately follows the PacOS release line. When preparing a release, publish the aligned PacOS BOM and plugin parent together before migrating separate plugin repositories to that parent version.
