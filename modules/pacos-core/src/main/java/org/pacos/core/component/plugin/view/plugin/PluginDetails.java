package org.pacos.core.component.plugin.view.plugin;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.TabSheet;
import org.pacos.base.component.NoContent;
import org.pacos.base.component.Spinner;
import org.pacos.base.component.Style;
import org.pacos.base.utils.component.DivUtils;
import org.pacos.base.utils.component.LiUtils;
import org.pacos.base.utils.component.SpanUtils;
import org.pacos.base.utils.component.TabSheetUtils;
import org.pacos.base.utils.component.UlUtils;
import org.pacos.config.repository.info.PluginRelease;
import org.pacos.core.component.plugin.dto.PluginDTO;
import org.pacos.core.component.plugin.event.LoadPluginDetailEvent;

public class PluginDetails extends Div {
    private final UI ui;
    private PluginDTO currentDisplayed;
    private Button passedBtn;
    private Button btn;

    public PluginDetails() {
        this.ui = UI.getCurrent();
        this.addClassName("m-detail");
    }

    public void showForPlugin(PluginDTO plugin) {
        if (currentDisplayed != null && currentDisplayed == plugin) {
            return;
        }
        this.currentDisplayed = plugin;
        removeAll();
        add(new Spinner().center());
        if (plugin.getPluginInfoDTO() == null) {
            loadDetailsInBackground(plugin);
        } else {
            displayDetails(plugin);
        }
    }

    public void displayDetails(PluginDTO plugin) {
        if (currentDisplayed != plugin) {
            return;
        }
        ui.access(() -> prepareViewContent(plugin));
    }

    public void setBtnAndSynchronize(Button passedBtn) {
        this.passedBtn = passedBtn;
        cloneBtnSettings();
    }

    private void cloneBtnSettings() {
        if(this.btn!=null && passedBtn!=null) {
            this.btn.setText(passedBtn.getText());
            this.btn.setEnabled(passedBtn.isEnabled());
            this.btn.removeThemeNames(btn.getThemeNames().toArray(new String[0]));
            this.btn.addThemeNames(passedBtn.getThemeNames().toArray(new String[0]));
        }
    }

    private void loadDetailsInBackground(PluginDTO plugin) {
        LoadPluginDetailEvent.fireEvent(plugin, this);
    }

    void prepareViewContent(PluginDTO plugin) {
        removeAll();
        if (plugin.getPluginInfoDTO() == null) {
            add(new NoContent("Can't found details about this plugin", ""));
            return;
        }

        this.btn = new Button(passedBtn.getText());
        btn.addClickListener(e -> passedBtn.click());
        cloneBtnSettings();

        Div topicContent = new DivUtils().withStyle("flex", "1 1 30px");
        //Title
        topicContent.add(DivUtils.ofClass("m-bold").withText(plugin.getName())
                .withStyle("font-size", "18px"));
        //Button with version
        topicContent.add(new DivUtils().withStyle(Style.MARGIN_TOP.value(), "10px")
                .withComponents(btn, SpanUtils.ofClass("plugin")
                        .withStyle(Style.MARGIN_LEFT.value(), "20px")
                        .withText(plugin.getPluginInfoDTO().version())));

        Span overviewContent = prepareOverview(plugin.getPluginInfoDTO().description());
        SpanUtils releaseNote = SpanUtils.ofClass("m-over");

        for (PluginRelease release : plugin.getPluginInfoDTO().releases()) {
            releaseNote.withComponents(new DivUtils().withComponents(new Div(release.version())));
            UlUtils ulList = new UlUtils();
            ulList.getStyle().set(Style.MARGIN_TOP.value(), "0");
            if(release.changes()!=null) {
                for (String info : release.changes()) {
                    ulList.withComponents(new LiUtils().withComponents(new Text(info)));
                }
            }
            releaseNote.add(ulList);
        }

        TabSheet tabs = new TabSheetUtils()
                .withTab("Overview", overviewContent)
                .withTab("Release note", releaseNote);
        tabs.getStyle().set("margin-top", "10px");
        add(topicContent, tabs);
    }

    private Span prepareOverview(String description) {
        Span overview = SpanUtils.ofClass("m-over");
        if (description == null) {
            return overview;
        }
        for (String s : description.split("\\n")) {

            String line = s.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("#")) {
                overview.add(DivUtils.ofClass("m-bold").withText(line.substring(1)));
            } else if (line.startsWith("-")) {
                overview.add(DivUtils.ofClass("m-point").withText(line.substring(1)));
            } else {
                Paragraph p = new Paragraph();
                p.setText(line);
                overview.add(p);
            }
        }
        return overview;
    }

}
