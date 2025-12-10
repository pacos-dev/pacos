package org.pacos.core.component.variable.service.provider;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.core.component.variable.dto.SystemVariableDTO;
import org.pacos.core.component.variable.dto.mapper.SystemVariableToVariableMapper;
import org.pacos.core.component.variable.generator.DynamicJavaScriptRunner;
import org.pacos.core.component.variable.proxy.SystemVariableProxy;
import org.springframework.stereotype.Component;
import org.vaadin.addons.variablefield.data.Scope;
import org.vaadin.addons.variablefield.data.ScopeName;
import org.vaadin.addons.variablefield.data.Variable;
import org.vaadin.addons.variablefield.provider.VariableProvider;

@Component
@AllArgsConstructor
public class SystemVariableProviderImpl implements VariableProvider {

    private final SystemVariableProxy systemVariableOutbound;

    @Override
    public List<Variable> loadVariables(Scope scope) {
        return systemVariableOutbound.loadSystemVariable();
    }

    @Override
    public Optional<Variable> loadVariable(Scope scope, String variableName) {
        Optional<SystemVariableDTO> variable = systemVariableOutbound.findVariable(variableName);
        if (variable.isEmpty()) {
            return Optional.empty();
        }
        SystemVariableDTO gv = variable.get();
        if (gv.getJs() == null) {
            return Optional.empty();
        }
        String result = DynamicJavaScriptRunner.runCode(variable.get().getJs());
        return Optional.of(SystemVariableToVariableMapper.map(gv, result));
    }

    @Override
    public Set<ScopeName> supportedScopes() {
        return Set.of(ScopeDefinition.SYSTEM.getScopeName());
    }
}
