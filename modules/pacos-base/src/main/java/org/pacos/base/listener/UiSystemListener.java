package org.pacos.base.listener;

import org.pacos.base.event.UISystem;

/**
 * After initialized Desktop view for the User, each implementation of this interface will be triggered
 * Implementation of this interface should be done in separate class to avoid wrong injection and proper initialization
 * <p>
 * Example of usage
 * <code>
 *
 * @Component public class MyListener implements UiSystemListener {
 * <p>
 * private final SpringBean springBean;
 * @Autowired public MyListener(SpringBean springBean) {
 * this.springBean = springBean;
 * }
 * @Override public void onSystemInitialized(UISystem system) {
 * //operation called after system initialization
 * }
 * }
 * </code>
 */
@FunctionalInterface
public interface UiSystemListener {
    void onSystemInitialized(UISystem system);
}
