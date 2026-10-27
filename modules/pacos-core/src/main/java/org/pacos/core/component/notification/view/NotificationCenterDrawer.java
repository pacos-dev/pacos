package org.pacos.core.component.notification.view;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.IntConsumer;

import org.pacos.base.session.UserSession;
import org.pacos.core.component.notification.domain.NotificationCategory;
import org.pacos.core.component.notification.domain.NotificationItemType;
import org.pacos.core.component.notification.domain.NotificationOperationStatus;
import org.pacos.core.component.notification.domain.NotificationPriority;
import org.pacos.core.component.notification.dto.NotificationCenterItemDTO;
import org.pacos.core.component.notification.service.NotificationService;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.server.VaadinSession;

public class NotificationCenterDrawer extends Div {

    private static final int LIST_LIMIT = 200;
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final NotificationService notificationService;
    private final Grid<NotificationCenterItemDTO> grid = new Grid<>();
    private final Div details = new Div();
    private final ComboBox<String> sourceFilter = new ComboBox<>("Source");
    private final ComboBox<NotificationCategory> categoryFilter = new ComboBox<>("Category");
    private final ComboBox<NotificationPriority> priorityFilter = new ComboBox<>("Priority");
    private final Checkbox unreadOnly = new Checkbox("Unread only");
    private IntConsumer unreadCountListener = ignored -> { };
    private NotificationCenterItemDTO selectedItem;
    private boolean refreshing;

    public NotificationCenterDrawer(NotificationService notificationService) {
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService");
        setWidth("min(1040px, 96vw)");
        setHeight("100vh");
        getStyle().set("position", "fixed")
                .set("top", "0")
                .set("right", "0")
                .set("z-index", "10000")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("background", "var(--lumo-base-color)")
                .set("color", "var(--lumo-body-text-color)")
                .set("box-shadow", "var(--lumo-box-shadow-xl)")
                .set("border-left", "1px solid var(--lumo-contrast-10pct)")
                .set("overflow", "hidden")
                .set("transform", "translateX(100%)")
                .set("transition", "transform 180ms ease")
                .set("pointer-events", "none");
        getElement().setAttribute("aria-label", "Notification Center");
        getElement().setAttribute("aria-hidden", "true");
        getElement().setAttribute("inert", "");

        Button closeButton = new Button("Close", VaadinIcon.CLOSE.create(), event -> close());
        closeButton.getElement().setAttribute("aria-label", "Close Notification Center");

        H2 title = new H2("Notification Center");
        Span subtitle = new Span("Messages and system operations");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)");
        VerticalLayout heading = new VerticalLayout(title, subtitle);
        heading.setPadding(false);
        heading.setSpacing(false);
        HorizontalLayout header = new HorizontalLayout(heading, closeButton);
        header.setWidthFull();
        header.setAlignItems(HorizontalLayout.Alignment.CENTER);
        header.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        header.getStyle().set("padding", "var(--lumo-space-m)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        sourceFilter.setPlaceholder("All sources");
        sourceFilter.setClearButtonVisible(true);
        categoryFilter.setPlaceholder("All categories");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setItems(NotificationCategory.values());
        categoryFilter.setItemLabelGenerator(Enum::name);
        priorityFilter.setPlaceholder("All priorities");
        priorityFilter.setClearButtonVisible(true);
        priorityFilter.setItems(NotificationPriority.values());
        priorityFilter.setItemLabelGenerator(Enum::name);

        HorizontalLayout filters = new HorizontalLayout(sourceFilter, categoryFilter, priorityFilter, unreadOnly);
        filters.setWidthFull();
        filters.setAlignItems(HorizontalLayout.Alignment.END);
        filters.setPadding(true);
        filters.setSpacing(true);
        filters.getStyle().set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        grid.addColumn(NotificationCenterItemDTO::title).setHeader("Notification").setFlexGrow(1).setAutoWidth(true);
        grid.addColumn(item -> item.priority().name()).setHeader("Priority").setAutoWidth(true);
        grid.addColumn(item -> TIME_FORMAT.format(item.createdAt())).setHeader("Time").setAutoWidth(true);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();
        grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));

        details.setWidth("58%");
        details.setHeightFull();
        details.getStyle().set("padding", "var(--lumo-space-l)")
                .set("overflow", "auto")
                .set("border-left", "1px solid var(--lumo-contrast-10pct)");
        Div listPanel = new Div(grid);
        listPanel.setWidth("42%");
        listPanel.setHeightFull();
        listPanel.getStyle().set("overflow", "hidden");
        HorizontalLayout content = new HorizontalLayout(listPanel, details);
        content.setWidthFull();
        content.setHeight("100%");
        content.setPadding(false);
        content.setSpacing(false);
        content.setFlexGrow(1, details);
        content.getStyle().set("min-height", "0");

        add(header, filters, content);
        getStyle().set("gap", "0");
        addClassName("notification-center-drawer");

        sourceFilter.addValueChangeListener(event -> refresh());
        categoryFilter.addValueChangeListener(event -> refresh());
        priorityFilter.addValueChangeListener(event -> refresh());
        unreadOnly.addValueChangeListener(event -> refresh());
        renderEmptyDetails();
        refreshUnreadCount();
    }

    public void setUnreadCountListener(IntConsumer listener) {
        unreadCountListener = listener == null ? ignored -> { } : listener;
        refreshUnreadCount();
    }

    public void open() {
        refresh();
        getElement().removeAttribute("inert");
        getElement().setAttribute("aria-hidden", "false");
        getStyle().set("pointer-events", "auto");
        getStyle().set("transform", "translateX(0)");
    }

    public void close() {
        getStyle().set("pointer-events", "none");
        getStyle().set("transform", "translateX(100%)");
        getElement().setAttribute("aria-hidden", "true");
        getElement().setAttribute("inert", "");
    }

    public void refreshUnreadCount() {
        UserSession session = currentUserSession();
        if (session != null) {
            unreadCountListener.accept(notificationService.getUnreadCount(session.getUserId()));
        }
    }

    private void refresh() {
        if (refreshing) {
            return;
        }
        UserSession session = currentUserSession();
        if (session == null) {
            grid.setItems(List.of());
            renderEmptyDetails();
            return;
        }
        refreshing = true;
        try {
            List<NotificationCenterItemDTO> allItems = notificationService.findRecentItems(
                    session.getUserId(), LIST_LIMIT, null, null, null, false);
            String selectedSource = sourceFilter.getValue();
            List<String> sources = allItems.stream().map(NotificationCenterItemDTO::sourceKey)
                    .filter(Objects::nonNull).distinct().sorted().toList();
            sourceFilter.setItems(sources);
            if (selectedSource != null && sources.contains(selectedSource)) {
                sourceFilter.setValue(selectedSource);
            } else if (selectedSource != null) {
                sourceFilter.clear();
            }

            List<NotificationCenterItemDTO> visibleItems = notificationService.findRecentItems(
                    session.getUserId(), LIST_LIMIT, sourceFilter.getValue(), categoryFilter.getValue(),
                    priorityFilter.getValue(), unreadOnly.getValue());

            grid.setItems(visibleItems);
            NotificationCenterItemDTO nextSelected = selectedItem == null ? null : visibleItems.stream()
                    .filter(item -> item.itemType() == selectedItem.itemType() && item.id().equals(selectedItem.id()))
                    .findFirst().orElse(null);
            if (nextSelected == null && !visibleItems.isEmpty()) {
                nextSelected = visibleItems.get(0);
            }
            if (nextSelected == null) {
                selectedItem = null;
                grid.deselectAll();
                renderEmptyDetails();
            } else {
                grid.select(nextSelected);
                showDetails(nextSelected);
            }
            unreadCountListener.accept(notificationService.getUnreadCount(session.getUserId()));
        } finally {
            refreshing = false;
        }
    }

    private void showDetails(NotificationCenterItemDTO item) {
        selectedItem = item;
        details.removeAll();
        H3 title = new H3(item.title());
        Paragraph metadata = new Paragraph(item.sourceKey() + " · " + item.category().name() + " · "
                + TIME_FORMAT.format(item.createdAt()));
        metadata.getStyle().set("color", "var(--lumo-secondary-text-color)");
        Span priority = new Span(item.priority().name());
        priority.getElement().getThemeList().add("badge");
        if (item.priority() == NotificationPriority.WARNING) {
            priority.getElement().getThemeList().add("warning");
        } else if (item.priority() == NotificationPriority.ERROR || item.priority() == NotificationPriority.CRITICAL) {
            priority.getElement().getThemeList().add("error");
        }
        details.add(title, metadata, priority);

        if (item.summary() != null && !item.summary().isBlank()) {
            Paragraph summary = new Paragraph(item.summary());
            summary.getStyle().set("font-weight", "600");
            details.add(summary);
        }
        if (item.body() != null && !item.body().isBlank()) {
            Paragraph body = new Paragraph(item.body());
            body.getStyle().set("white-space", "pre-wrap");
            details.add(body);
        }
        if (item.itemType() == NotificationItemType.OPERATION) {
            NotificationOperationStatus status = item.operationStatus();
            if (status != null) {
                Span statusBadge = new Span(status.name());
                statusBadge.getElement().getThemeList().add("badge");
                if (status == NotificationOperationStatus.FAILED) {
                    statusBadge.getElement().getThemeList().add("error");
                } else if (status.isTerminal()) {
                    statusBadge.getElement().getThemeList().add("contrast");
                } else {
                    statusBadge.getElement().getThemeList().add("success");
                }
                details.add(statusBadge);
            }
            if (item.progress() != null) {
                ProgressBar progressBar = new ProgressBar();
                progressBar.setMin(0);
                progressBar.setMax(100);
                progressBar.setValue(item.progress() / 100.0);
                progressBar.setWidthFull();
                details.add(new Paragraph("Progress: " + item.progress() + "%"), progressBar);
            }
            if (item.currentStep() != null && !item.currentStep().isBlank()) {
                details.add(new Paragraph("Current step: " + item.currentStep()));
            }
        }
        if (item.actionUrl() != null && (item.actionUrl().startsWith("https://")
                || item.actionUrl().startsWith("http://"))) {
            Anchor link = new Anchor(item.actionUrl(), "Open details");
            link.setTarget("_blank");
            details.add(link);
        }

        UserSession session = currentUserSession();
        if (session != null) {
            Button readButton = new Button(item.read() ? "Mark as unread" : "Mark as read",
                    item.read() ? VaadinIcon.ENVELOPE_OPEN.create() : VaadinIcon.ENVELOPE.create());
            readButton.addClickListener(event -> {
                if (item.read()) {
                    notificationService.markUnread(session.getUserId(), item.itemType(), item.id());
                } else {
                    notificationService.markRead(session.getUserId(), item.itemType(), item.id());
                }
                refresh();
            });
            details.add(readButton);
            if (item.requiresAcknowledgement() && !item.acknowledged()
                    && item.itemType() == NotificationItemType.MESSAGE) {
                Button acknowledgeButton = new Button("Acknowledge", VaadinIcon.CHECK_CIRCLE.create());
                acknowledgeButton.addClickListener(event -> {
                    notificationService.acknowledge(session.getUserId(), item.id());
                    refresh();
                });
                details.add(acknowledgeButton);
            }
        }
    }

    private void renderEmptyDetails() {
        details.removeAll();
        details.add(new H3("Select a notification"));
        details.add(new Paragraph("Choose a message or an operation from the list to see its details."));
    }

    private UserSession currentUserSession() {
        if (VaadinSession.getCurrent() == null) {
            return null;
        }
        return VaadinSession.getCurrent().getAttribute(UserSession.class);
    }
}
