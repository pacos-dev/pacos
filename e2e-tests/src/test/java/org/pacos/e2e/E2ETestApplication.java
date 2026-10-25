package org.pacos.e2e;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Test bootstrap for the application context.
 *
 * The production engine artifact is repackaged as an executable Spring Boot
 * archive, so its Application class is not exposed as a regular dependency
 * class. This bootstrap uses the same component and Vaadin scan roots while
 * leaving the production packaging unchanged.
 */
@SpringBootApplication(scanBasePackages = {"org.pacos.core.config", "org.pacos.plugin.*.config"})
@EnableVaadin(value = {"org.pacos.core", "org.pacos.base", "org.pacos.plugin.*"})
@EnableAsync
@Theme("desktop")
class E2ETestApplication {
}
