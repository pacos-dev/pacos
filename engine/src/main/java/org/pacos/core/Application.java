package org.pacos.core;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.LoadDependenciesOnStartup;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.pacos.config.property.WorkingDir;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The entry point of the Spring Boot application.
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 * command line args to run -XX:HotswapAgent=fatjar -DworkingDir='/my/location/pacos'
 */
@SpringBootApplication(scanBasePackages = {"org.pacos.core.config", "org.pacos.plugin.*.config"})
@EnableVaadin(value = {"org.pacos.core", "org.pacos.base", "org.pacos.plugin.*"})
@EnableAsync
@Theme(value = "desktop", variant = Lumo.LIGHT)
@PWA(name = "Pac OS", shortName = "PacOS", offlineResources = {})
@NpmPackage(value = "line-awesome", version = "1.3.0")
@Push
@LoadDependenciesOnStartup({ConfigView.class})
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {

        getSpringApplication().run(args);
    }

    static SpringApplication getSpringApplication() {
        WorkingDir.initialize();
        return new SpringApplication(Application.class);
    }

}
