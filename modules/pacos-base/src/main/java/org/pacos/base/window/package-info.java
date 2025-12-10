/**
 * Contains DesktopWindow implementation - base user interface object on pacos
 * To create a new application window, you need {@link org.pacos.base.window.DesktopWindow} and {@link org.pacos.base.window.config.WindowConfig} implementation.
 * All DesktopWindow implementations are detected during system startup based on {@link org.pacos.base.window.config.WindowConfig} and managed by pacos.
 * PacOS is responsible for object initialization and dependency injection
 * <p>
 * Example of usage
 * </p>
 * <code>
 * @Component
 * @Scope("prototype")
 * public class MyWindow extends DesktopWindow {
 *
 *    @Autowired
 *    public MyWindow(MyServiceBean myService) {
 *        add(new Span("Hello world"));
 *        addAttachListener(attachEvent -> attachEvent.getUI().getPage().addStylesheet("frontend/css/database.css"));<- import additional css during initialization on client side
 *    }
 *
 *    @Override
 *    public void onMinimizeEvent() {
 *        //you can add additional behaviour for minimize event
 *    }
 *
 *     //you can add additional behaviour for close event
 *    @Override
 *    public void onCloseEvent() {
 *       //you can add additional behaviour for minimize event
 *    }
 *}
 * </code>
 * <code>
 * public MyWindowConfig implements WindowConfig{
 *     @Override
 *     public String title() {
 *         return "My window";
 *     }
 *
 *     @Override
 *     public String icon() {
 *         return "img/icon/my_icon.png";
 *     }
 *
 *     @Override
 *     public Class<? extends DesktopWindow> activatorClass() {
 *         return MyWindow.class;
 *     }
 *
 *     @Override
 *     public boolean isApplication() {
 *         return true;
 *     }
 *
 *     @Override
 *     public boolean isAllowMultipleInstance() {
 *         return false;
 *     }
 *
 *     @Override
 *     public boolean isAllowMultipleInstance() {
 *         return false;
 *     }
 *
 *     @Override
 *     public boolean isAllowedForCurrentSession(UISystem uiSystem) {
 *        return !UserSession.getCurrent().getUser().isGuestSession();
 *     }
 * }
 * </code>
 */
package org.pacos.base.window;