package org.pacos.base.api;

import java.util.Map;
import java.util.WeakHashMap;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;

/**
 * Provides a reusable API for highlighting literal text inside a Vaadin component.
 *
 * <p>Matching is case-insensitive and is performed independently for each text node
 * in the component's rendered DOM, including accessible open Shadow DOM roots.
 * Match indexes are zero-based and follow DOM traversal order. Overlapping matches
 * are not produced.
 *
 * <p>The browser implementation is provided by the PacOS theme. Components may be
 * highlighted before attachment; the requested highlight is applied when they attach.
 * Active highlighting is cleared on detach and restored on the next attach.
 */
public final class TextHighlighter {

    private static final String HIGHLIGHT_SCRIPT =
            "window.pacos.textHighlighter.highlight($0, $1, $2)";
    private static final String CLEAR_SCRIPT =
            "window.pacos.textHighlighter.clear($0)";
    private static final Map<Component, HighlightState> STATES = new WeakHashMap<>();

    private TextHighlighter() {
    }

    /**
     * Highlights every case-insensitive literal occurrence of the query.
     *
     * @param target component whose rendered text should be highlighted
     * @param query literal search text; null or blank values clear existing highlighting
     */
    public static void highlight(Component target, String query) {
        highlight(target, query, -1);
    }

    /**
     * Highlights every case-insensitive literal occurrence and marks one match as active.
     *
     * @param target component whose rendered text should be highlighted
     * @param query literal search text; null or blank values clear existing highlighting
     * @param activeMatchIndex zero-based match index to distinguish, or a negative/out-of-range
     *                         value to leave all matches inactive
     */
    public static void highlight(Component target, String query, int activeMatchIndex) {
        if (target == null) {
            return;
        }
        if (query == null || query.isBlank()) {
            clear(target);
            return;
        }

        HighlightState state = stateFor(target);
        state.query = query;
        state.activeMatchIndex = activeMatchIndex;
        target.getUI().ifPresent(ui -> executeHighlight(ui, target, state));
    }

    /**
     * Removes all highlighting from the component and restores its ordinary text nodes.
     *
     * @param target component whose highlighting should be removed
     */
    public static void clear(Component target) {
        if (target == null) {
            return;
        }

        HighlightState state = stateFor(target);
        state.query = null;
        state.activeMatchIndex = -1;
        target.getUI().ifPresent(ui -> executeClear(ui, target));
    }

    private static HighlightState stateFor(Component target) {
        synchronized (STATES) {
            return STATES.computeIfAbsent(target, component -> {
                HighlightState state = new HighlightState();
                component.addAttachListener(event -> onAttach(event, state));
                component.addDetachListener(TextHighlighter::onDetach);
                return state;
            });
        }
    }

    private static void onAttach(AttachEvent event, HighlightState state) {
        Component component = event.getSource();
        if (state.query == null) {
            executeClear(event.getUI(), component);
        } else {
            executeHighlight(event.getUI(), component, state);
        }
    }

    private static void onDetach(DetachEvent event) {
        executeClear(event.getUI(), event.getSource());
    }

    private static void executeHighlight(UI ui, Component target, HighlightState state) {
        ui.getPage().executeJs(HIGHLIGHT_SCRIPT, target.getElement(), state.query, state.activeMatchIndex);
    }

    private static void executeClear(UI ui, Component target) {
        ui.getPage().executeJs(CLEAR_SCRIPT, target.getElement());
    }

    private static final class HighlightState {
        private volatile String query;
        private volatile int activeMatchIndex = -1;
    }
}
