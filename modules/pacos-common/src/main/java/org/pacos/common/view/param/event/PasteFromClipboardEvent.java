package org.pacos.common.view.param.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.vaadin.flow.component.UI;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.notification.NotificationUtils;
import org.pacos.common.view.param.GridParam;
import org.pacos.common.view.param.Param;
import org.pacos.common.view.param.ParamParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PasteFromClipboardEvent {

    private static final Logger LOG = LoggerFactory.getLogger(PasteFromClipboardEvent.class);

    private PasteFromClipboardEvent() {
    }

    public static void fireEvent(GridParam gridParam) {
        CompletableFuture<String> stringCompletableFuture = UISystem.getCurrent().getClipboardManager().readClipboard();
        UI ui = UI.getCurrent();
        CompletableFuture.runAsync(() -> readClipboardAndModifyVariables(gridParam, stringCompletableFuture, ui));

    }

    static void readClipboardAndModifyVariables(GridParam gridParam, CompletableFuture<String> stringCompletableFuture, UI ui) {
        try {
            String value = stringCompletableFuture.get(3, TimeUnit.SECONDS);
            Set<Param> pastedParam = ParamParser.mapFromString(value);
            if (pastedParam.isEmpty()) {
                return;
            }
            List<Param> joinParams = joinVariables(gridParam.getItems(), pastedParam);
            ui.access(() -> {
                gridParam.modifyItems(joinParams);
                NotificationUtils.info("Pasted " + pastedParam.size() + " variable(s)");
            });
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOG.error("Can't load variables from clipboard", e);
            Thread.currentThread().interrupt();
        }
    }

    static List<Param> joinVariables(List<Param> existingParams, Set<Param> pastedParams) {
        List<Param> params = new ArrayList<>(existingParams);
        for (Param param : pastedParams) {
            Optional<Param> v = params.stream()
                    .filter(variable -> variable.getName().equals(param.getName())).findFirst();
            if (v.isPresent()) {
                v.get().setValue(param.getValue());
                v.get().setEnabled(true);
            } else {
                params.add(param);
            }
        }
        return params;
    }
}
