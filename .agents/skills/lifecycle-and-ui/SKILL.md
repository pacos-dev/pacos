---
name: pacos-lifecycle-and-ui
description: Safely implement PacOS Vaadin windows, sessions, events, shortcuts, and UI lifecycle behavior.
---

# PacOS Lifecycle and UI

Use this skill for DesktopWindow, WindowConfig, UISystem, UserSession, shortcuts, downloads, clipboard, and UI event subscriptions.

## Window contract

A desktop application is configured through WindowConfig and activated through a DesktopWindow implementation.

WindowConfig controls title, icon, activator class, application visibility, multiple-instance behavior, and optional session-level availability.

DesktopWindow is a Vaadin Dialog with PacOS lifecycle behavior. Follow the existing prototype-scope pattern for plugin windows.

## Session and UI

Resolve session state through UserSession.getCurrent() and the associated UISystem.

Do not create independent global UI state when session or window scope is sufficient.

Admin permission bypass and action permission semantics belong to the platform contract.

## Events and listeners

Prefer SystemEvent.subscribeOnAttached(...) for component-bound UI subscriptions because detach removes the subscription.

Use manual subscription only when lifecycle and unsubscribe are explicit.

Always consider attach, detach, close, child windows, shortcut registration, and callbacks.

## Platform helpers

Use VariableManager, WindowManager, ApplicationManager, ShortcutManager, DownloadManager, and ClipboardManager through UISystem.

Do not block the Vaadin session thread on asynchronous operations such as clipboard access.

## Leak prevention

A closed window must not remain reachable through listeners, shortcuts, callbacks, or static holders.

