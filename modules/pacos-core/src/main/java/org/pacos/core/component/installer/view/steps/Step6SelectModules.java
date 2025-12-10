package org.pacos.core.component.installer.view.steps;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import org.pacos.core.component.installer.view.InstallerView;
import org.pacos.core.component.installer.view.steps.helper.BoxContent;
import org.pacos.core.component.plugin.proxy.PluginProxy;
import org.pacos.core.component.plugin.view.plugin.PluginListView;

@SuppressWarnings("squid:S110")
public class Step6SelectModules extends Box {

    private final PluginProxy pluginProxy;

    public Step6SelectModules(InstallerView installerView) {
        super(installerView, "Pac OS - install extensions");
        this.pluginProxy = installerView.getInstallerService().getPluginProxy();
        setProgressBarValue(0.7);
        BoxContent content = new BoxContent();
        content.setHeight(500, Unit.PIXELS);
        add(content);
        setWidth("900px");
        Span line = content.addTextLine("Install selected plugins. " +
                "It will also be possible to change the configuration at any time.");
        Span infoLine = new Span();
        infoLine.getStyle().set("padding-left", "120px");
        line.add(infoLine);

        PluginListView pluginListView = new PluginListView(pluginProxy);
        pluginListView.listLoaded(installerView.getSettings().getCouplerPluginList());

        pluginListView.setHeight(450, Unit.PIXELS);
        content.add(pluginListView);

        addBackBtn();
        Button nextBtn = addNextBtn();
        nextBtn.setText("Complete setup");
        nextBtn.setEnabled(true);

    }

    @Override
    protected void nextBtnEvent() {
        if (pluginProxy.getPluginInstallService().isInstallationInProgress()) {
            Notification.show("Wait for the selected plugin to complete installation");
        } else {
            installerView.setView(new Step7Installation(installerView));
        }
    }

    @Override
    protected void backBtnEvent() {
        if (pluginProxy.getPluginInstallService().isInstallationInProgress()) {
            Notification.show("Wait for the selected plugin to complete installation");
        } else {
            installerView.setView(new Step4AdditionalSettings(installerView));
        }
    }
}
