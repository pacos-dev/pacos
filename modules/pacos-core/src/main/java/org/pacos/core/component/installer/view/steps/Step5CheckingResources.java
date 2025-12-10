package org.pacos.core.component.installer.view.steps;

import com.vaadin.flow.component.HtmlComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.pacos.config.repository.info.Plugin;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.installer.view.steps.helper.BoxContent;
import org.pacos.core.component.plugin.loader.PluginDetailsLoader;
import org.pacos.core.component.plugin.loader.PluginInfoLoader;
import org.slf4j.LoggerFactory;

import java.util.List;

@SuppressWarnings("squid:S110")
public class Step5CheckingResources extends Box {


    private final Span loadIndicator;
    private final Button nextBtn;
    private final UI ui;

    public Step5CheckingResources(InstallerView installerView) {
        super(installerView, "Pac OS - Additional plugins");
        this.ui = UI.getCurrent();
        setProgressBarValue(0.5);

        BoxContent content = new BoxContent();
        add(content);
        this.loadIndicator = content.addTextLine("Please wait, installer will check available extension");
        this.loadIndicator.add(new HtmlComponent("my-spinner"));
        PluginInfoLoader.refreshPluginList(this::refreshPluginListViewOnUI);

        addBackBtn();
        this.nextBtn = addNextBtn();
        this.nextBtn.setText("Skip & Install");
    }

    @Override
    protected void nextBtnEvent() {
        installerView.setView(new Step7Installation(installerView));
    }

    @Override
    protected void backBtnEvent() {
        PluginInfoLoader.killProcess();
        PluginDetailsLoader.killProcess();
        installerView.setView(new Step4AdditionalSettings(installerView));
    }


    private void refreshPluginListViewOnUI(List<Plugin> plugins) {
        try {
            ui.access(() -> {
                refreshPluginListView(plugins);
                ui.push();
            });

        } catch (RuntimeException e) {
            LoggerFactory.getLogger(Step5CheckingResources.class).error("Failed to refresh plugin list", e);
        }
    }

    void refreshPluginListView(List<Plugin> plugins) {
        installerView.getSettings().setCouplerPluginList(plugins);
        loadIndicator.removeAll();
        if (installerView.getSettings().getCouplerPluginList() == null) {
            displayErrorCantLoad();
        } else {
            installerView.setView(new Step6SelectModules(installerView));
        }
    }

    void displayErrorCantLoad() {
        loadIndicator.removeAll();

        loadIndicator.add(new Paragraph("A problem occurred while trying to communicate with the remote server. " +
                "Check official page for more information or try again later."));

        loadIndicator.add(new Paragraph("Updates and installations of new plugins will be possible also " +
                "after installation."));

        Button refreshBtn = new Button(VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_SMALL);
        refreshBtn.addClickListener(e -> add(new Step5CheckingResources(installerView)));
        loadIndicator.add(refreshBtn);

        this.nextBtn.setText("Skip & Install");
    }

}
