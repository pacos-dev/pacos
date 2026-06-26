package org.pacos.core.component.ai.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.pacos.base.ai.AiToolbox;
import org.pacos.base.event.UISystem;
import org.pacos.base.session.UserSession;
import org.pacos.base.window.config.WindowConfig;
import org.pacos.core.component.plugin.manager.PluginResource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class SystemNavigationTool implements AiToolbox {

    @Tool(description = "Return current active window")
    public Map<String, String> getUiContext() {
        UISystem uiSystem = UISystem.getCurrent();
        System.out.println(uiSystem);
        Map<String, String> stateMap = new HashMap<>();
        uiSystem.getWindowManager().getWindowDisplayedOnFront().ifPresentOrElse(w -> {
            stateMap.put("windowOnFront", w.getConfig().title());
            stateMap.put("windowClass", w.getClassName());
            stateMap.put("windowId", w.getId().orElse(null));
        }, () -> {
            stateMap.put("noActiveWindow", "none");
        });
        return stateMap;
    }

    @Tool(description = "LIST ALL RUNNING APPLICATIONS. This is the ONLY source for valid titles to use in 'openApplication'. Call this first!")
    public Set<String> getApplicationToken() {
        return PluginResource.getAppWindowConfigForUser(UserSession.getCurrent()).stream().map(WindowConfig::title).collect(Collectors.toSet());
    }

    @Tool(description = "Launch a PacOS window. IMPORTANT: You must provide a valid 'SystemToken' which is ONLY available through 'getApplicationToken'.")
    public String openApplication(
            @ToolParam(description = "The EXACT title of the window to open. Copy-paste from the application name list.") String applicationName,
            @ToolParam(description = "Set to true if you verified this title in the last 10 seconds.") boolean verified) {
        Optional<WindowConfig> app = PluginResource.getAppWindowConfigForUser(UserSession.getCurrent()).stream().filter(e -> e.title().equalsIgnoreCase(applicationName))
                .findFirst();
        if (app.isPresent()) {
            UISystem.getCurrent().getWindowManager().showWindow(app.get().getClass());
        } else {
            throw new RuntimeException(
                    "CRITICAL ERROR: Application '" + applicationName + "' is NOT in the registry. " +
                            "You MUST call 'getApplicationToken' to synchronize your state. " +
                            "Valid titles currently are: [" + getApplicationToken() + "]. If any match, then application is probably not installed." +
                            "Please retry with an EXACT match from this list, or return information that application is not installed."
            );
        }
        return "Opened " + applicationName;
    }
}