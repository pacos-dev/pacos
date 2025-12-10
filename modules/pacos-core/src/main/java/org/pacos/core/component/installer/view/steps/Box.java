package org.pacos.core.component.installer.view.steps;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.progressbar.ProgressBar;
import org.pacos.base.utils.component.SpanUtils;
import org.pacos.core.component.installer.view.InstallerView;

public abstract class Box extends Div {

    private final ProgressBar progressBar;
    protected final InstallerView installerView;

    Box(InstallerView installerView, String title) {
        this.installerView = installerView;
        setClassName("box");

        Div pageTitle = new Div();
        pageTitle.setClassName("page-title");
        Image image = new Image("/img/icon.png", "icon");
        image.setWidth(40, Unit.PIXELS);

        pageTitle.add(image);
        pageTitle.add(SpanUtils.ofClass("title").withText(title));

        add(pageTitle);

        this.progressBar = new ProgressBar();
        progressBar.setValue(0.0);
        add(progressBar);

    }

    Button addNextBtn() {
        Button btn = new Button("Next");
        btn.setClassName("next-btn");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.addClickListener(e -> nextBtnEvent());
        add(btn);
        return btn;
    }

    Button addBackBtn() {
        Button btn = new Button("Back");
        btn.setClassName("back-btn");
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        btn.addClickListener(e -> backBtnEvent());
        add(btn);
        return btn;
    }

    protected abstract void nextBtnEvent();

    protected abstract void backBtnEvent();

    public void setProgressBarValue(double value) {
        this.progressBar.setValue(value);
    }

}
