# pacos-base
The pacos-base module provides essential functionality to integrate with the PacOS system, granting access to core system features such as user session management. Additionally, it offers the foundational code needed to implement custom, independent windows within the PacOS application—akin to standalone, windowed applications on Windows.

### Key Features
#### 1. User Session Management
   pacos-base facilitates seamless access to user session data and authentication states within the PacOS environment. This enables secure and user-specific interactions, ensuring that applications built with pacos-base can fully leverage user context.

#### 2. Custom Window Implementation
   The module includes a framework that allows developers to create fully independent windows within PacOS. These windows operate as standalone components within the main application, enabling a modular and extensible approach to application development.

#### 3. General System Components
   pacos-base provides access to PacOS’s general-purpose components, such as a file-picker or code-mirror making it easy to incorporate common UI elements without additional development effort. These components ensure consistency with the PacOS ecosystem, contributing to a cohesive user experience.

#### 4. Event Management System
   The module offers an event management system that allows applications to both emit and listen to system events. With this feature, developers can create highly interactive applications that respond dynamically to user actions and system changes within PacOS.

#### 5. Variable Processor Extension
   One of the powerful features of pacos-base is the VariableProcessor, which provides an interface to expand the PacOS system’s variable management capabilities. This enables developers to create custom variable collections, allowing for more complex data manipulations and customized application behavior.

### Example
To create own desktop window, implementation of DesktopWindow and WindowConfig are needed
The configuration file provides PacOS information about the basic behavior of the window and the class 
responsible for its initialization.
```
public MyWindowConfig implements WindowConfig{

    @Override
    public String title() {
      return "My window";
    }
    
    @Override
    public String icon() {
      return "img/icon/my_icon.png";
    }
    
    @Override
    public Class<? extends DesktopWindow> activatorClass() {
      return MyWindow.class;
    }
    
    @Override
    public boolean isApplication() {
      return true;
    }
    
    @Override
    public boolean isAllowMultipleInstance() {
      return false;
    }
    
    @Override
    public boolean isAllowedForCurrentSession(UISystem uiSystem) {
     return !UserSession.getCurrent().getUser().isGuestSession();
    }
}
```
<br>
The window constructor should pass its configuration and optionally spring beans in the argument. 
The pacos is responsible for injecting and creating the window during user interaction

```
@Component
@Scope("prototype")
public class MyWindow extends DesktopWindow {

    @Autowired
    public MyWindow(MyWindowConfig myWindwConfig, MyBean myBean) {
        super(myWindwConfig);
        add(new Span("Hello world"));
    }
   
}
```

### 6. Notification Center Publication

PacOS modules can publish user-facing messages through the `NotificationPublisher` contract in `pacos-base`. The implementation in `pacos-core` owns persistence, audience filtering, and deduplication; producer modules do not need to depend on core entities or UI components.

Use a stable `sourceKey` for the publishing module and an optional `businessKey` for one logical message:

```java
NotificationPublisher publisher;

NotificationRequest request = new NotificationRequest(
    "APP_CENTER",
    "plugin:org.example:calendar:capabilities",
    "Calendar plugin",
    "The Calendar plugin adds event and reminder management.",
    "Full plugin description goes here.",
    NotificationCategory.ANNOUNCEMENT,
    NotificationPriority.INFO,
    null,                         // no expiry
    "https://docs.pacos.dev/plugins/calendar",
    false,                        // no acknowledgement required
    NotificationAudience.ALL_USERS,
    Set.of());                     // no explicit recipients for ALL_USERS

NotificationPublishResult result = publisher.publish(request);
```

The unique logical identity is `(sourceKey, businessKey)`. Publishing again with the same pair updates the existing message if its content or audience changed; otherwise it returns `UNCHANGED`. The original creation timestamp and per-user read/acknowledgement state are preserved. A request without a business key always creates a new message.

For a user-specific message, use `NotificationAudience.SELECTED_USERS` and pass the intended registered user IDs. The request rejects an empty recipient set. Do not supply recipient IDs for `ALL_USERS`.

Choose keys based on product semantics, not message text. For example, `plugin:org.example:calendar:capabilities` remains stable across uninstall/reinstall so the announcement is not duplicated. Add a version or campaign suffix only when a genuinely new announcement should be created.
