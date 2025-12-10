package org.pacos.core.component.variable.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pacos.base.session.UserDTO;
import org.pacos.base.variable.VariableProcessor;
import org.pacos.core.component.plugin.manager.PluginResource;
import org.pacos.core.component.variable.service.provider.UserGlobalVariableProviderImpl;
import org.pacos.core.component.variable.service.provider.UserVariableProviderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.vaadin.addons.variablefield.data.Scope;
import org.vaadin.addons.variablefield.data.Variable;
import org.vaadin.addons.variablefield.provider.VariableProvider;

/**
 * This class processes the given line with variables.
 * The variables are extracted and replaced with the configured value.
 */
@Component
public class ProcessVariable implements VariableProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessVariable.class);

    /**
     * Search variables between double mustaches quote. The order of the scopes define the order of processing
     */
    public String process(List<Scope> scopes, String line, UserDTO userDTO) {
        if (line == null) {
            return null;
        }
        Set<String> extractedVariables = extractVariables(line);
        for (String variable : extractedVariables) {
            Optional<Variable> optValue = findValue(scopes, variable, userDTO);
            if (optValue.isPresent()) {
                String value = optValue.get().getValue();
                if (value != null) {
                    line = line.replace("{{" + variable + "}}", value);
                } else {
                    LOGGER.debug("Variable {} is not defined {}", variable, scopes);
                }
            } else {
                LOGGER.debug("Variable {} not found in scope {}", variable, scopes);
            }
        }
        return line;
    }

    private static Set<String> extractVariables(String input) {
        Pattern pattern = Pattern.compile("\\{\\{([^}]+)}}");
        Matcher matcher = pattern.matcher(input);

        Set<String> variables = new HashSet<>();
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        return variables;
    }

    private Optional<Variable> findValue(List<Scope> scopes, String variable, UserDTO userDTO) {
        for (Scope scope : scopes) {
            for (VariableProvider provider : PluginResource.getAllVariableProvider()) {
                if (!provider.isScopeSupported(scope.getScopeName())) {
                    continue;
                }

                Optional<Variable> res;

                if(userDTO == null){
                    res = provider.loadVariable(scope, variable);
                }else if (provider instanceof UserVariableProviderImpl userVariableProvider) {
                    res = userVariableProvider.loadVariable(variable, userDTO);
                } else if (provider instanceof UserGlobalVariableProviderImpl userGlobalVariableProvider) {
                    res = userGlobalVariableProvider.loadVariable(userDTO, variable);
                } else {
                    res = provider.loadVariable(scope, variable);
                }

                if (res.isPresent()) {
                    return res;
                }
            }
        }
        return Optional.empty();
    }
}
