package org.pacos.core.config;

import org.pacos.core.component.registry.proxy.RegistryProxy;
import org.pacos.core.component.registry.service.RegistryName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ComponentScan(basePackages = {
        "org.pacos.core.component.*.service",
        "org.pacos.core.component.*.proxy",
        "org.pacos.core.component.*.repository",
        "org.pacos.core.component.*.view.config",
        "org.pacos.core.component.*.view.tab",
        "org.pacos.core.component.plugin.manager",
        "org.pacos.core.system.proxy",
        "org.pacos.core.system.service",
        "org.pacos.core.system.listener",
        "org.pacos.core.system.window.config"
})
@EnableScheduling
public class SpringCoreConfig {

    private final RegistryProxy registryProxy;

    @Autowired
    public SpringCoreConfig(RegistryProxy registryProxy) {
        this.registryProxy = registryProxy;
    }

    @Bean(name = "pluginContextExecutor", defaultCandidate = false)
    public ThreadPoolTaskExecutor pluginContextExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("PluginContextThread--");
        executor.initialize();
        return executor;
    }

    @Bean(name = "pluginDownloadExecutor", defaultCandidate = false)
    public ThreadPoolTaskExecutor pluginDownloadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("PluginDownloadThread--");
        executor.initialize();
        return executor;
    }

    @Bean(name = "VaadinTaskExecutor")
    public ThreadPoolTaskExecutor vaadinTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("VaadinTaskExecutor--");
        executor.initialize();
        return executor;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        registryProxy.saveRegistry(RegistryName.RESTART_REQUIRED, false);
    }
}
