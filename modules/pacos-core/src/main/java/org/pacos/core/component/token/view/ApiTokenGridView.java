package org.pacos.core.component.token.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.pacos.base.event.UISystem;
import org.pacos.base.utils.component.ButtonUtils;
import org.pacos.base.utils.component.VerticalLayoutUtils;
import org.pacos.base.window.config.impl.ConfirmationWindowConfig;
import org.pacos.core.component.token.ApiPermissions;
import org.pacos.core.component.token.dto.ApiTokenDTO;
import org.pacos.core.component.token.service.ApiTokenService;

public class ApiTokenGridView extends Grid<ApiTokenDTO> {

    private static final String HEADER_NAME = "Name";
    private static final String HEADER_TOKEN = "Token";
    private static final String HEADER_STATUS = "Status";
    private static final String HEADER_EXPIRED = "Expired";
    private static final String BTN_REVOKE= "Revoke";
    private static final String BTN_DELETE= "Delete";

    private final ApiTokenService tokenService;

    public ApiTokenGridView(ApiTokenService tokenService) {
        this.tokenService = tokenService;
        addColumn(ApiTokenDTO::name).setHeader(HEADER_NAME);
        addColumn(ApiTokenDTO::getHashToken).setHeader(HEADER_TOKEN);
        addColumn(ApiTokenDTO::status).setHeader(HEADER_STATUS);
        addColumn(ApiTokenDTO::getExpiredValue).setHeader(HEADER_EXPIRED);
        addColumn(new ComponentRenderer<>(ButtonUtils::new, this::buildRevokeBtn)).setWidth("20px").setHeader(BTN_REVOKE);
        addColumn(new ComponentRenderer<>(ButtonUtils::new, this::buildDeleteBtn)).setWidth("20px").setHeader(BTN_DELETE);

        refresh();
        setWidthFull();
    }
    static String getSearchIndex() {
        return HEADER_NAME+HEADER_STATUS+HEADER_TOKEN+HEADER_EXPIRED+BTN_REVOKE+BTN_DELETE;
    }
    void buildDeleteBtn(ButtonUtils btn, ApiTokenDTO apiTokenDTO) {
        btn.setIcon(VaadinIcon.TRASH.create());
        btn.setClassName("btn btn-error");
        btn.withEnabledForPermission(ApiPermissions.REMOVE_TOKEN);
        btn.addClickListener(event -> {
            final ConfirmationWindowConfig config = new ConfirmationWindowConfig(() -> removeTokenEvent(apiTokenDTO));
            config.setContent(VerticalLayoutUtils.defaults(
                    new Span("Are you sure? This token will be lost.")
            ));
            UISystem.getCurrent().getWindowManager().showModalWindow(config);
        });
    }

    void buildRevokeBtn(ButtonUtils btn, ApiTokenDTO apiTokenDTO) {
        btn.setIcon(VaadinIcon.ARROW_CIRCLE_LEFT_O.create());
        btn.addClickListener(event -> {
            final ConfirmationWindowConfig config = new ConfirmationWindowConfig(() -> revokeTokenEvent(apiTokenDTO));
            config.setContent(VerticalLayoutUtils.defaults(
                    new Span("Are you sure? This operation can't be undone.")
            ));
            UISystem.getCurrent().getWindowManager().showModalWindow(config);

        });
        btn.setEnabled(apiTokenDTO.status().isActive());
        btn.withEnabledForPermission(ApiPermissions.REVOKE_TOKEN);
    }

    boolean removeTokenEvent(ApiTokenDTO apiTokenDTO) {
        tokenService.removeToken(apiTokenDTO.name());
        refresh();
        return true;
    }

    boolean revokeTokenEvent(ApiTokenDTO apiTokenDTO) {
        tokenService.revokeToken(apiTokenDTO.name());
        refresh();
        return true;
    }

    public void refresh() {
        setItems(tokenService.findAll());
    }
}
