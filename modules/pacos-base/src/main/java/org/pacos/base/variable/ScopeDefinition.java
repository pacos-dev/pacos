package org.pacos.base.variable;

import java.util.Arrays;
import java.util.List;

import org.vaadin.addons.variablefield.data.Scope;
import org.vaadin.addons.variablefield.data.ScopeName;

/**
 * Contains definition for all system variable scopes
 * Can be used for own queue definition of scopes
 */
public class ScopeDefinition {

    private ScopeDefinition() {
    }

    public static final ScopeName SYSTEM_NAME = new ScopeName("System");
    public static final ScopeName USER_NAME = new ScopeName("User");
    public static final ScopeName GLOBAL_NAME = new ScopeName("Global");

    public static final Scope SYSTEM = new Scope(SYSTEM_NAME.name(), 1, 'G', "#0262f3");

    public static Scope userCollection(int userId) {
        return new Scope(USER_NAME.name(), userId, 'U', "#076F14");
    }

    public static Scope globalCollectionId(int userId) {
        return new Scope(GLOBAL_NAME.name(), userId, 'U', "#072F14");
    }

    public static List<Scope> userScopes(Integer userId) {
        final Scope userScope = ScopeDefinition.userCollection(userId);
        final Scope globalScope = ScopeDefinition.globalCollectionId(userId);
        return Arrays.asList(userScope, globalScope, ScopeDefinition.SYSTEM);
    }
}
