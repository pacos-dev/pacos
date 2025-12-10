package org.pacos.core.component.variable.service.provider;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vaadin.addons.variablefield.provider.VariableProvider;

/**
 * This class loads all available providers from the system and extensions and holds them for future processing by
 * a modal variable
 */
@Component
public class VariableProviderHandler {

    private static final Logger LOG = LoggerFactory.getLogger(VariableProviderHandler.class);
    private final List<VariableProvider> variableProviders;

    @Autowired
    public VariableProviderHandler(List<VariableProvider> variableProviders) {
        this.variableProviders = variableProviders;
        variableProviders.forEach(provider -> LOG.info("Variable provider loaded {} ", provider.getClass().getName()));
    }

    public List<VariableProvider> getProviders() {
        return variableProviders;
    }

}
