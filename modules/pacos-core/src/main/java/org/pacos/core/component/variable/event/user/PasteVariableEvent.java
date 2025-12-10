package org.pacos.core.component.variable.event.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.vaadin.flow.component.UI;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.common.view.param.Param;
import org.pacos.common.view.param.ParamParser;
import org.pacos.core.component.variable.dto.UserVariableDTO;
import org.pacos.core.component.variable.system.user.UserVariableSystem;
import org.pacos.core.component.variable.view.user.UserVariableForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasteVariableEvent {

    private static final Logger LOG = LoggerFactory.getLogger(PasteVariableEvent.class);

    private PasteVariableEvent() {
    }

    public static void fireEvent(UserVariableSystem system, UserVariableForm userVariableForm) {
        CompletableFuture<String> stringCompletableFuture = system.getUiSystem().getClipboardManager().readClipboard();
        UI ui = UI.getCurrent();
        CompletableFuture.runAsync(() -> readClipboardAndModifyVariables(userVariableForm, stringCompletableFuture, ui));

    }

    static void readClipboardAndModifyVariables(UserVariableForm userVariableForm, CompletableFuture<String> stringCompletableFuture, UI ui) {
        try {
            String value = stringCompletableFuture.get(3, TimeUnit.SECONDS);
            Set<Param> pastedParam = ParamParser.mapFromString(value);
            if (pastedParam.isEmpty()) {
                return;
            }
            List<UserVariableDTO> variables = joinVariables(userVariableForm.getVariables(), pastedParam);
            ui.access(() -> {
                userVariableForm.modifyVariables(variables);
                NotificationUtils.info("Pasted " + pastedParam.size() + " variable(s)");
            });
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Can't load variables from clipboard", e);
            Thread.currentThread().interrupt();
        }
    }

    static List<UserVariableDTO> joinVariables(List<UserVariableDTO> userVariables, Set<Param> pastedParam) {
        List<UserVariableDTO> variables = new ArrayList<>(userVariables);
        for (Param param : pastedParam) {
            Optional<UserVariableDTO> v = variables.stream()
                    .filter(variable -> variable.getName().equals(param.getName())).findFirst();
            if (v.isPresent()) {
                v.get().setCurrentValue(param.getValue());
                v.get().setEnabled(true);
            } else {
                UserVariableDTO dto = new UserVariableDTO();
                dto.setName(param.getName());
                dto.setCurrentValue(param.getValue());
                dto.setInitialValue(param.getValue());
                variables.add(dto);
            }
        }
        return variables;
    }
}
