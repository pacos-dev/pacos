package org.pacos.core.component.token.view;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.Anchor;
import org.pacos.base.component.setting.SettingPageLayout;
import org.pacos.base.utils.component.InfoBox;
import org.pacos.base.window.shortcut.ShortcutType;
import org.pacos.core.component.token.service.ApiTokenService;

public class ApiConfigTabLayout extends SettingPageLayout {

    private static final String DETAILS = "Create new token";
    private static final String TOKEN_LIST = "Token list";
    private static final String INFO = """
            The token is used by the API provided by the current system configuration.
            It's required as an authorization header for any request.
            The API documentation is refreshed each time the configuration of running plugins is changed.
            API swagger documentation can be found here:
            """;

    public ApiConfigTabLayout(ApiTokenService tokenService) {
        setSizeFull();
        configureInfoBox();

        ApiTokenGridView tokenListView = new ApiTokenGridView(tokenService);
        ApiTokenFormLayout tokenFormLayout = new ApiTokenFormLayout(tokenService, tokenListView);

        Details tokenForm = new Details(DETAILS, tokenFormLayout);
        tokenForm.setOpened(false);
        add(tokenForm);

        Details tokenList = new Details(TOKEN_LIST, tokenListView);
        tokenList.setOpened(true);
        tokenList.setSizeFull();
        add(tokenList);
    }

    public static String getSearchIndex() {
        return INFO+DETAILS+TOKEN_LIST+ApiTokenGridView.getSearchIndex()+ApiTokenFormLayout.getSearchIndex();
    }

    private void configureInfoBox() {
        Anchor anchor = new Anchor("/swagger-ui/index.html", "/swagger-ui/index.html");
        anchor.setRouterIgnore(true);
        anchor.setTarget("_blank");
        anchor.getStyle().set("color", "blue");
        InfoBox infoBox = new InfoBox(new Text(
                INFO),
                anchor);

        add(infoBox);
    }

    @Override
    public void onShortCutDetected(ShortcutType shortcutType) {
        //shortcut listener not implemented
    }
}
