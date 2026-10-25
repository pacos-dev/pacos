package org.pacos.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.parallel.Isolated;
import org.pacos.base.session.UserDTO;
import org.pacos.config.property.WorkingDir;
import org.pacos.config.repository.RepositoryClient;
import org.pacos.config.repository.data.AppArtifact;
import org.pacos.config.repository.data.AppRepository;
import org.pacos.config.property.ApplicationProperties;
import org.pacos.config.property.PropertyName;
import org.pacos.core.component.dock.service.DockService;
import org.pacos.core.component.installer.service.InstallerService;
import org.pacos.core.component.installer.service.ProgressUpdateListener;
import org.pacos.core.component.installer.settings.InstallationMode;
import org.pacos.core.component.installer.settings.InstallerSettings;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.manager.PluginManager;
import org.pacos.core.component.plugin.manager.PluginState;
import org.pacos.core.component.plugin.manager.type.PluginStatusEnum;
import org.pacos.core.component.plugin.service.PluginInstallService;
import org.pacos.core.component.plugin.service.PluginService;
import org.pacos.core.config.session.UserSessionService;
import org.pacos.core.component.user.service.UserForm;
import org.pacos.core.system.view.login.LoginForm;
import org.pacos.base.session.UserSession;
import com.vaadin.flow.server.VaadinSession;
import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.pacos.core.component.user.proxy.UserProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@Isolated("The application bootstrap uses JVM-wide system properties and a file-backed database.")
@SpringBootTest(
        classes = E2ETestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.profiles.active=test",
                "vaadin.launch-browser=false"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InstallerContextE2ETest {

    private static final MockPluginRepository pluginRepository = new MockPluginRepository();
    private static final Path tempDir = createTemporaryWorkingDirectory();
    private static final String originalPluginRepositoryUrl = System.getProperty(PropertyName.PLUGIN_LIST_REPO_URL.getPropertyName());
    private static String originalWorkingDirArgument;
    private static String originalWorkingDir;

    static {
        originalWorkingDirArgument = System.getProperty("-DworkingDir");
        originalWorkingDir = System.getProperty("workingDir");
        System.setProperty(PropertyName.PLUGIN_LIST_REPO_URL.getPropertyName(), pluginRepository.url());
        ApplicationProperties.reloadProperties();
        System.setProperty("-DworkingDir", tempDir.toString());
        System.setProperty("workingDir", tempDir.toString());
        WorkingDir.initialize();
        try {
            Files.createDirectories(tempDir.resolve("db"));
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private InstallerService installerService;

    @Autowired
    private RegistryProxy registryProxy;

    @Autowired
    private UserProxyService userProxyService;

    @Autowired
    private DockService dockService;

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private PluginInstallService pluginInstallService;

    @Autowired
    private PluginService pluginService;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private PluginState pluginState;

    @DynamicPropertySource
    static void registerIsolatedDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:hsqldb:" + tempDir.resolve("db/pacos.db") + ";shutdown=true");
    }

    @AfterAll
    static void restoreWorkingDirectoryProperties() {
        pluginRepository.close();
        restoreSystemProperty(PropertyName.PLUGIN_LIST_REPO_URL.getPropertyName(), originalPluginRepositoryUrl);
        ApplicationProperties.reloadProperties();
        restoreSystemProperty("-DworkingDir", originalWorkingDirArgument);
        restoreSystemProperty("workingDir", originalWorkingDir);
    }

    @Test
    @Order(1)
    @DisabledIfSystemProperty(named = "pacos.e2e.browser", matches = "true")
    void whenInstallerServiceConfiguresSingleModeThenInstallationIsPersistedInApplicationContext() {
        InstallerSettings settings = new InstallerSettings();
        settings.setInstallationMode(InstallationMode.SINGLE);
        settings.getAdditionalSettings().setRegistrationEnabled(false);
        settings.getAdditionalSettings().setGuestAccountEnabled(true);
        settings.getAdditionalSettings().setSendingErrorLogAllowed(false);
        settings.getAdditionalSettings().setAutoUpdateEnabled(false);

        List<Double> progressUpdates = new ArrayList<>();
        ProgressUpdateListener listener = progressUpdates::add;

        assertTrue(installerService.configure(settings, listener));

        assertTrue(registryProxy.isInstalled());
        assertTrue(registryProxy.isSingleMode());
        assertTrue(registryProxy.isGuestMode());
        assertFalse(registryProxy.isRegistrationPanelEnabled());
        assertEquals("false", registryProxy.readRegistryOrDefault(RegistryName.LOG_PERMISSION, "missing"));
        assertEquals("false", registryProxy.readRegistryOrDefault(RegistryName.AUTO_UPDATE_ENABLED, "missing"));
        assertEquals(List.of(0.65, 0.70, 0.95, 1.0), progressUpdates);

        UserDTO admin = userProxyService.loadUserByLogin("admin").orElseThrow();
        assertTrue(userProxyService.loadUserByLogin("guest").isPresent());
        assertFalse(dockService.findByUserIdOrderByOrderNum(admin.getId()).isEmpty());
    }

    @Test
    @Order(2)
    void whenHttpClientOpensApplicationThenEndpointRespondsSuccessfully()
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertTrue(response.statusCode() < 400);
        assertFalse(response.body().isBlank());
    }

    @Test
    @Order(3)
    @EnabledIfSystemProperty(named = "pacos.e2e.browser", matches = "true")
    void whenUserInstallsApplicationLogsInAndLogsOutThenBrowserFlowCompletes() {
        try (Playwright playwright = Playwright.create()) {
            boolean headed = Boolean.getBoolean("pacos.e2e.headed");
            long slowMo = Long.getLong("pacos.e2e.slowMo", headed ? 250L : 0L);
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(!headed).setSlowMo(slowMo));
            try {
                Page page = browser.newPage();
                var response = page.navigate("http://127.0.0.1:" + port + "/install");

                assertNotNull(response);
                assertTrue(response.status() < 400);

                // Complete the installer through the actual Vaadin UI.
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Next")).click();
                page.getByText("Group", new Page.GetByTextOptions().setExact(true)).click();
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Next")).click();

                // Group mode requires an explicit administrator account.
                page.getByLabel("Login", new Page.GetByLabelOptions().setExact(true)).fill("admin-e2e");
                page.getByLabel("Password", new Page.GetByLabelOptions().setExact(true)).fill("E2e-admin-password-2026!");
                page.getByLabel("Repeat password", new Page.GetByLabelOptions().setExact(true)).fill("E2e-admin-password-2026!");
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Next")).click();
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Next")).click();

                // The plugin catalog may load asynchronously and either advance to module selection
                // or remain on the step with a "Skip & Install" action if the remote catalog is unavailable.
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Skip & Install"))
                        .or(page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                                new Page.GetByRoleOptions().setName("Complete setup")))
                        .waitFor();
                if (page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Skip & Install")).isVisible()) {
                    page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                            new Page.GetByRoleOptions().setName("Skip & Install")).click();
                }
                if (page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Complete setup")).isVisible()) {
                    page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                            new Page.GetByRoleOptions().setName("Complete setup")).click();
                }

                // The installer creates the administrator account configured in Group mode.
                var username = page.locator("#login-username");
                var password = page.locator("#login-password");
                username.waitFor();
                username.getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX).waitFor();
                password.getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX).waitFor();

                // Vaadin fields expose their editable input inside Shadow DOM. Wait for the
                // component to be attached and hydrated before interacting with the inner input.
                username.getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX).click();
                username.getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX).fill("admin-e2e");
                username.getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX).press("Tab");
                assertEquals("admin-e2e",
                        username.getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX).inputValue(),
                        "Username must be populated before submitting the login form");
                password.getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX).fill("E2e-admin-password-2026!");
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Log in")).click();

                // The desktop menu is rendered only after successful authentication.
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Applications")).waitFor();

                // Open the real App center and install the plugin from the isolated Maven repository.
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Applications")).click();
                page.locator("vaadin-text-field.search-filter input").fill("App center");
                page.getByText("App center", new Page.GetByTextOptions().setExact(true)).click();
                page.getByText("Skeleton app", new Page.GetByTextOptions().setExact(true)).waitFor();
                page.getByText("Skeleton app", new Page.GetByTextOptions().setExact(true)).click();
                var skeletonRow = page.locator(".m-info")
                        .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText("Skeleton app"));
                skeletonRow.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Install").setExact(true)).click();
                skeletonRow.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new com.microsoft.playwright.Locator.GetByRoleOptions().setName("Installed").setExact(true))
                        .waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(30_000));

                PluginDTO installedSkeleton = new PluginDTO();
                installedSkeleton.setGroupId("org.pacos.plugin");
                installedSkeleton.setArtifactName("skeleton");
                installedSkeleton.setVersion("3.1");
                long installDeadline = System.nanoTime() + java.time.Duration.ofSeconds(30).toNanos();
                while (pluginState.getState(installedSkeleton) != PluginStatusEnum.ON
                        && pluginState.getState(installedSkeleton) != PluginStatusEnum.ERROR
                        && System.nanoTime() < installDeadline) {
                    page.waitForTimeout(100);
                }
                assertEquals(PluginStatusEnum.ON, pluginState.getState(installedSkeleton),
                        "The plugin must be started, not merely downloaded");

                // The skeleton plugin contributes the ToDo list application to Applications.
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Applications")).click();
                page.locator("vaadin-text-field.search-filter input").fill("ToDo list");
                page.getByText("ToDo list", new Page.GetByTextOptions().setExact(true)).waitFor();
                page.getByText("ToDo list", new Page.GetByTextOptions().setExact(true)).click();
                page.locator(".header-label").filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText("ToDo list")).waitFor();

                // Open Settings from Applications, then navigate to Plugin management.
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Applications")).click();
                page.locator("vaadin-text-field.search-filter input").fill("Settings");
                page.locator("span.title").filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText("Settings")).waitFor();
                page.locator("span.title").filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText("Settings")).click();
                var pluginManagementItem = page.locator("vaadin-grid-cell-content")
                        .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText("Plugin management"));
                pluginManagementItem.last().waitFor();
                pluginManagementItem.last().click();
                var pluginRow = page.getByRole(com.microsoft.playwright.options.AriaRole.ROW)
                        .filter(new com.microsoft.playwright.Locator.FilterOptions().setHasText("Skeleton app"));
                pluginRow.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON).last().click();
                page.getByText("Are you sure?", new Page.GetByTextOptions().setExact(true)).waitFor();
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Ok").setExact(true)).click();

                long uninstallDeadline = System.nanoTime() + java.time.Duration.ofSeconds(30).toNanos();
                while (pluginState.getPlugins().stream().anyMatch(p -> p.getArtifactName().equals("skeleton"))
                        && System.nanoTime() < uninstallDeadline) {
                    page.waitForTimeout(100);
                }
                assertFalse(pluginState.getPlugins().stream().anyMatch(p -> p.getArtifactName().equals("skeleton")),
                        "The uninstalled plugin must be removed from runtime Applications");

                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Applications")).click();
                page.locator("vaadin-text-field.search-filter input").fill("ToDo list");
                assertFalse(page.getByText("ToDo list",
                        new Page.GetByTextOptions().setExact(true)).isVisible(),
                        "The plugin-provided application must disappear from Applications after uninstall");

                // Open the real logout confirmation window and confirm logout.
                page.locator("#logout-button").click();
                page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
                        new Page.GetByRoleOptions().setName("Log out")).click();

                // Closing the Vaadin session can close the current browser page as well.
                if (!page.isClosed()) {
                    try {
                        page.waitForTimeout(1_000);
                    } catch (PlaywrightException exception) {
                        if (!page.isClosed()) {
                            throw exception;
                        }
                    }
                }
                if (page.isClosed()) {
                    page = browser.newPage();
                }

                // A logged-out user must not be able to remain on the protected desktop route.
                var protectedResponse = page.navigate("http://127.0.0.1:" + port + "/desktop");
                assertNotNull(protectedResponse);
                page.locator("#login-username").getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX).waitFor();
                assertTrue(page.locator("#login-username").getByRole(com.microsoft.playwright.options.AriaRole.TEXTBOX).isVisible(),
                        "Unauthenticated desktop access should show the login form");
            } finally {
                browser.close();
            }
        }
    }


    @Test
    @Order(4)
    void whenUserLogsInAndOutThenSessionIsCreatedAndDestroyed() {
        String login = "e2e-login-user";
        String password = "E2e-test-password-2026!";
        if (userProxyService.loadUserByLogin(login).isEmpty()) {
            userProxyService.createAccount(new UserForm(login, password, password));
        }

        VaadinSession previousSession = VaadinSession.getCurrent();
        VaadinSession session = mock(VaadinSession.class);
        Map<Class<?>, Object> attributes = new HashMap<>();
        when(session.getAttribute(UserSession.class)).thenAnswer(invocation ->
                attributes.get(invocation.getArgument(0)));
        doAnswer(invocation -> {
            Class<?> key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            if (value == null) {
                attributes.remove(key);
            } else {
                attributes.put(key, value);
            }
            return null;
        }).when(session).setAttribute(org.mockito.ArgumentMatchers.eq(UserSession.class),
                org.mockito.ArgumentMatchers.nullable(UserSession.class));
        VaadinSession.setCurrent(session);
        try {
            assertTrue(userSessionService.initializeSession(new LoginForm(login, password)));
            assertNotNull(session.getAttribute(UserSession.class));
            assertEquals(login, ((UserSession) session.getAttribute(UserSession.class)).getUser().getUserName());

            UserSessionService.logOut();

            assertEquals(null, session.getAttribute(UserSession.class));
            org.mockito.Mockito.verify(session).close();
        } finally {
            VaadinSession.setCurrent(previousSession);
        }
    }

    @Test
    @Order(5)
    void whenPluginIsInstalledUninstalledAndInstalledAgainThenCatalogAndPersistenceStayConsistent() {
        PluginDTO plugin = new PluginDTO();
        plugin.setGroupId("org.pacos.e2e");
        plugin.setArtifactName("lifecycle-test-plugin");
        plugin.setVersion("1.0.0");
        plugin.setName("Lifecycle E2E Test Plugin");
        plugin.setAuthor("PacOS E2E");
        plugin.setRepoUrl("https://example.invalid/repository");

        pluginInstallService.savePlugin(plugin);

        assertEquals(1, pluginService.findByArtifactNameAndGroupId(
                plugin.getArtifactName(), plugin.getGroupId()).size());
        assertTrue(pluginState.getPlugins().contains(plugin));

        pluginManager.removePlugin(plugin);
        pluginService.removePlugin(plugin);

        assertTrue(pluginService.findByArtifactNameAndGroupId(
                plugin.getArtifactName(), plugin.getGroupId()).isEmpty());
        assertFalse(pluginState.getPlugins().contains(plugin));

        pluginInstallService.savePlugin(plugin);

        assertEquals(1, pluginService.findByArtifactNameAndGroupId(
                plugin.getArtifactName(), plugin.getGroupId()).size());
        assertTrue(pluginState.getPlugins().contains(plugin));
    }

    @Test
    @Order(6)
    @DisabledIfSystemProperty(named = "pacos.e2e.browser", matches = "true")
    void whenPluginIsDownloadedFromMockRepositoryThenItStartsAndCanBeUninstalled() throws InterruptedException {
        var catalog = RepositoryClient.loadPluginList(AppRepository.pluginRepo());
        assertEquals(1, catalog.size());
        assertEquals("skeleton", catalog.get(0).artifactName());
        assertEquals("3.1", catalog.get(0).version());

        PluginDTO plugin = new PluginDTO(catalog.get(0));
        plugin.setRepoUrl(pluginRepository.url());
        pluginInstallService.downloadAndInstallPluginFromRemote(plugin, AppRepository.pluginRepo());

        long deadline = System.nanoTime() + java.time.Duration.ofSeconds(30).toNanos();
        PluginStatusEnum status;
        do {
            status = pluginState.getState(plugin);
            if (status == PluginStatusEnum.ON || status == PluginStatusEnum.ERROR) {
                break;
            }
            Thread.sleep(100);
        } while (System.nanoTime() < deadline);

        assertEquals(PluginStatusEnum.ON, status, "The downloaded plugin should be loaded by the real plugin lifecycle");
        assertTrue(Files.isRegularFile(WorkingDir.getLibPath().resolve(
                new AppArtifact("org.pacos.plugin", "skeleton", "3.1").getJarPath())),
                "The JAR should be downloaded into the isolated PacOS library directory");
        assertEquals(1, pluginService.findByArtifactNameAndGroupId("skeleton", "org.pacos.plugin").size());

        pluginManager.removePlugin(plugin);
        pluginService.removePlugin(plugin);

        assertFalse(pluginState.getPlugins().contains(plugin), "Uninstalled plugin should disappear from runtime Applications");
        assertTrue(pluginService.findByArtifactNameAndGroupId("skeleton", "org.pacos.plugin").isEmpty(),
                "Uninstalled plugin should be removed from the persistent catalog");
    }

    private static Path createTemporaryWorkingDirectory() {
        try {
            return Files.createTempDirectory("pacos-e2e-");
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static void restoreSystemProperty(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }
}
