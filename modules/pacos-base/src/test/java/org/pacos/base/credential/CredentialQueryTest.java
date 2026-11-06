package org.pacos.base.credential;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class CredentialQueryTest {

    @Test
    void whenCreateAllQueryThenLeaveFiltersEmpty() {
        CredentialQuery query = CredentialQuery.all();

        assertNull(query.type());
        assertNull(query.searchTerm());
    }
}
