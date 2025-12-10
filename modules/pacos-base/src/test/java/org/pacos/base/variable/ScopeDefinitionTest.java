package org.pacos.base.variable;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.vaadin.addons.variablefield.data.Scope;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScopeDefinitionTest {

    @Test
    void whenCreateSystemScopeThenScopeHasCorrectProperties() {
        Scope systemScope = ScopeDefinition.SYSTEM;
        assertEquals("System", systemScope.getName());
        assertEquals(1, systemScope.getId());
        assertEquals('G', systemScope.getScopeIcon());
        assertEquals("#0262f3", systemScope.getCssColor());
    }

    @Test
    void whenCreateUserScopeThenScopeHasCorrectProperties() {
        int userId = 123;
        Scope userScope = ScopeDefinition.userCollection(userId);
        assertEquals("User", userScope.getName());
        assertEquals(userId, userScope.getId());
        assertEquals('U', userScope.getScopeIcon());
        assertEquals("#076F14", userScope.getCssColor());
    }

    @Test
    void whenCreateGlobalScopeThenScopeHasCorrectProperties() {
        int userId = 123;
        Scope globalScope = ScopeDefinition.globalCollectionId(userId);
        assertEquals("Global", globalScope.getName());
        assertEquals(userId, globalScope.getId());
        assertEquals('U', globalScope.getScopeIcon());
        assertEquals("#072F14", globalScope.getCssColor());
    }

    @Test
    void whenGetUserScopesThenContainsUserGlobalAndSystemScopes() {
        int userId = 123;
        List<Scope> userScopes = ScopeDefinition.userScopes(userId);

        assertEquals(3, userScopes.size());

        assertEquals("User", userScopes.get(0).getName());
        assertEquals(userId, userScopes.get(0).getId());

        assertEquals("Global", userScopes.get(1).getName());
        assertEquals(userId, userScopes.get(1).getId());

        assertEquals("System", userScopes.get(2).getName());
        assertEquals(1, userScopes.get(2).getId());
    }
}
