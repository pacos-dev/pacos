/**
 * Allows to process line with provided variable
 * Example
 * <code>
 * @Component
 * class MyService{
 *    public MyService(VariableProcessor processor){
 *        String line = "line with ${variable}";
 *        String decodedLine = processor.process(myScopes,line,UserSession.getCurrent());
 *    }
 *}
 * </code>
 * <p></p>
 * To extend the system with your own variable groups, you need to perform several steps
 * - Define VariableProvider which will synchronize the available list of variables
 * <code>
 * @Component
 * public class MyVariableProvider implements VariableProvider {
 *
 *     private Scope myScope = new Scope("MY_SCOPE",1,'S', "#FAC673");
 *     @Override
 *     public List<Variable> loadVariables(Scope scope) {
 *         return myService.loadVariables(myScope.getId());
 *     }
 *
 *     @Override
 *     public Set<ScopeName> supportedScopes() {
 *         return Set.of(new ScopeName(scope.getName()));
 *     }
 *
 *     @Override
 *     public Optional<Variable> loadVariable(Scope scope, String variableName) {
 *         return myService.findVariable(scope.getId(), variableName);
 *     }
 *
 *     @Override
 *     public int hashCode() {
 *         return myScope.hashCode();
 *     }
 *
 *     @Override
 *     public boolean equals(Object o) {
 *         if (this == o) {
 *             return true;
 *         }
 *         return o != null && getClass() == o.getClass();
 *     }
 * }
 * </code>
 * - Add variable provider during window initialization
 * <code>
 * @Component
 * public class MyUIListener implements UiSystemListener {
 *
 *     private final MyVariableProvider myVariableProvider;
 *
 *     @Autowired
 *     public MockUiSystemListener(MyVariableProvider myVariableProvider) {
 *         this.myVariableProvider = myVariableProvider;
 *     }
 *
 *     @Override
 *     public void onSystemInitialized(UISystem system) {
 *         system.getVariableModal().addProvider(myVariableProvider);
 *     }
 * }
 * </code>
 * Scope myScope = myScope;
 * UISystem.getCurrent().getVariableModal().updateVariable(mySsope,myVariables);
 * UISystem.getCurrent().getVariableModal().removeVariables(mySsope,myVariables);
 *
 * </code>
 */
package org.pacos.base.variable;