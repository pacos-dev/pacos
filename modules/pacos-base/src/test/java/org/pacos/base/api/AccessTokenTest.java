package org.pacos.base.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class AccessTokenTest {

    @Test
    void testConstructorAndGetters() {
        AccessToken accessToken = new AccessToken("API", "123456");

        assertEquals("API", accessToken.name());
        assertEquals("123456", accessToken.token());
    }

    @Test
    void testEqualsAndHashCode() {
        AccessToken accessToken1 = new AccessToken("API", "123456");
        AccessToken accessToken2 = new AccessToken("API", "123456");
        AccessToken accessToken3 = new AccessToken("API", "654321");

        assertEquals(accessToken1, accessToken2);
        assertNotEquals(accessToken1, accessToken3);
        assertEquals(accessToken1.hashCode(), accessToken2.hashCode());
        assertNotEquals(accessToken1.hashCode(), accessToken3.hashCode());
    }

    @Test
    void testToString() {
        AccessToken accessToken = new AccessToken("API", "123456");

        String expectedString = "AccessToken[name=API, token=123456]";
        assertEquals(expectedString, accessToken.toString());
    }
}