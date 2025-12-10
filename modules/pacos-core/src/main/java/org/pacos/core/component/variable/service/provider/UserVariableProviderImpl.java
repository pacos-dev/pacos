package org.pacos.core.component.variable.service.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.vaadin.flow.server.VaadinSession;
import lombok.AllArgsConstructor;
import org.pacos.base.session.UserDTO;
import org.pacos.base.session.UserSession;
import org.pacos.base.variable.ScopeDefinition;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.dto.mapper.UserVariableToVariableMapper;
import org.pacos.core.component.variable.proxy.UserVariableProxy;
import org.springframework.stereotype.Component;
import org.vaadin.addons.variablefield.data.Scope;
import org.vaadin.addons.variablefield.data.ScopeName;
import org.vaadin.addons.variablefield.data.Variable;
import org.vaadin.addons.variablefield.provider.VariableProvider;

@Component
@AllArgsConstructor
public class UserVariableProviderImpl implements VariableProvider {

    private final UserVariableProxy userVariableOutbound;

    @Override
    public List<Variable> loadVariables(Scope scope) {
        UserSession userSession = VaadinSession.getCurrent().getAttribute(UserSession.class);
        if (userSession == null) {
            return new ArrayList<>();
        }
        UserDTO userDTO = userSession.getUser();
        Optional<Integer> variableCollection = getChosenCollection(userDTO);
        return variableCollection.map(collection ->
                        userVariableOutbound.loadVariables(collection)
                                .stream()
                                .filter(UserVariableDTO::isEnabled)
                                .map(UserVariableToVariableMapper::map)
                                .toList())
                .orElseGet(ArrayList::new);
    }

    @Override
    public Optional<Variable> loadVariable(Scope scope, String variableName) {
        UserSession userSession = VaadinSession.getCurrent().getAttribute(UserSession.class);
        if (userSession == null) {
            return Optional.empty();
        }
        UserDTO userDTO = userSession.getUser();
        Optional<Integer> variableCollection = getChosenCollection(userDTO);
        return variableCollection.flatMap(collectionId ->
                userVariableOutbound.loadVariable(collectionId, variableName)
                        .map(UserVariableToVariableMapper::map));
    }


    public Optional<Variable> loadVariable(String variableName, UserDTO userDTO) {
        Optional<Integer> variableCollection = getChosenCollection(userDTO);
        return variableCollection.flatMap(collectionId ->
                userVariableOutbound.loadVariable(collectionId, variableName).filter(UserVariableDTO::isEnabled)
                        .map(UserVariableToVariableMapper::map));
    }

    private Optional<Integer> getChosenCollection(UserDTO userDTO) {
        Integer variableCollection = userDTO.getVariableCollectionId();
        if (variableCollection == null) {
            return Optional.empty();
        } else {
            return Optional.of(variableCollection);
        }
    }

    @Override
    public Set<ScopeName> supportedScopes() {
        return Set.of(ScopeDefinition.USER_NAME);
    }
}
