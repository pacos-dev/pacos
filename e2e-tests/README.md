# PacOS end-to-end tests

This module boots the real Spring application context on a random HTTP port and uses an isolated temporary working directory and file-backed HSQLDB database.

## Scenarios

- Installer configuration and persistence through Spring-managed services.
- HTTP availability of the application.
- **Browser E2E (opt-in):** completes the Group installation wizard, signs in, opens App center, installs the skeleton plugin from the isolated Maven-layout repository, verifies that the plugin starts, launches its ToDo list application, uninstalls it through Plugin management, confirms that the application disappears from Applications, then logs out and checks the protected desktop route.
- Login/logout session lifecycle at service level.
- Plugin install → uninstall → reinstall consistency for catalog and persistence at service level.
- Plugin repository integration: the test repository serves Maven metadata, plugin catalog JSON, plugin version metadata, plugin details XML, icon and JAR using the same paths as the production repository. It does not contact the live PacOS repository. When `skeleton-3.1.jar` is present in `e2e-tests/src/test/resources/test-plugins/` or at `/mnt/data/skeleton-3.1.jar`, the mock serves that artifact; otherwise it generates a small self-contained plugin JAR for CI.

The browser scenario performs the UI installation instead of the service-level installer test, so the app starts from a clean, uninstalled database. The marketplace test uses the local mock repository and is independent of the live repository.

## Standard Maven builds

E2E tests are **skipped by default**, so ordinary commands such as `mvn test`, `mvn verify`, and the root build do not execute them. This avoids E2E side effects and long-running browser/application startup during standard local builds. The test sources are still compiled during the normal build.

## Run E2E tests without a browser

From the repository root:

```bash
mvn -pl e2e-tests -am -Pproduction test -Dpacos.e2e.skip=false
```

This runs the service/context tests and skips the browser-only scenario unless `pacos.e2e.browser=true` is set.

## Run the full browser E2E locally

Install Playwright Chromium once:

```bash
mvn -pl e2e-tests -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium" org.codehaus.mojo:exec-maven-plugin:3.5.0:java
```

Run the installer → login → logout browser flow in a visible browser with slowed actions:

```bash
mvn -pl e2e-tests -am -Pproduction test \
  -Dpacos.e2e.skip=false \
  -Dpacos.e2e.browser=true \
  -Dpacos.e2e.headed=true \
  -Dpacos.e2e.slowMo=300
```

To run Chromium headlessly, omit `-Dpacos.e2e.headed=true` or set it to `false`. To change the action delay, adjust `pacos.e2e.slowMo` (milliseconds).

Jenkins runs browser E2E only on `main`. PR builds compile the E2E tests but skip their execution. The module sets `pacos.e2e.skip=true` by default; CI explicitly overrides this property on `main`.

The test class is isolated because application startup uses JVM-wide system properties and file-backed persistence.
