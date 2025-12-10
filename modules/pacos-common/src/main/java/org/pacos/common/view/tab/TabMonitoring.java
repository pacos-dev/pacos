package org.pacos.common.view.tab;

import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.Tab;

/**
 * Monitor changes on Tab content. If change are detected then tab header is updated
 */
public class TabMonitoring extends Tab {

    protected Span label;

    protected boolean markedAsUpdated;

    public TabMonitoring(String labelText) {
        this.label = new Span(labelText);
        add(label);
    }

    public void updateLabel(String label) {
        this.label.setText(label);
    }

    public void markAsChanged() {
        label.addClassName("changes");
        addThemeName("warning");
    }

    public void markAsNoChanges() {
        label.removeClassName("changes");
        removeThemeName("warning");
    }

    public void markChanges(boolean changed) {
        if (changed) {
            markAsChanged();
        } else {
            markAsNoChanges();
        }
    }
}
