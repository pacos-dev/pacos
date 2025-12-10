package org.pacos.core.component.variable.view.config;

import java.util.Set;

import org.pacos.base.window.manager.VariableManager;
import org.vaadin.addons.variablefield.modal.VariableModal;
import org.vaadin.addons.variablefield.provider.VariableProvider;

/**
 * Manager implementation based on VariableModal provided by variable-field
 */
public class VariableManagerImpl extends VariableModal implements VariableManager {

    public VariableManagerImpl(Set<VariableProvider> providers) {
        super(providers);
    }
}
