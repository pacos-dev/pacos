package org.pacos.core.component.credential.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CredentialFileInputTest {

    @Test
    void whenConstructingThenCopyContent() {
        byte[] content = {1, 2, 3};

        CredentialFileInput input = new CredentialFileInput("private.key", "application/octet-stream", content);
        content[0] = 9;

        assertArrayEquals(new byte[]{1, 2, 3}, input.content());
        assertEquals(3, input.size());
    }

    @Test
    void whenReadingContentThenReturnCopy() {
        CredentialFileInput input = new CredentialFileInput("private.key", null, new byte[]{1, 2, 3});

        byte[] content = input.content();
        content[0] = 9;

        assertArrayEquals(new byte[]{1, 2, 3}, input.content());
    }

    @Test
    void whenOriginalFilenameIsNullThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new CredentialFileInput(null, null, new byte[]{1}));
    }

    @Test
    void whenOriginalFilenameIsBlankThenThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new CredentialFileInput("  ", null, new byte[]{1}));
    }

    @Test
    void whenContentIsNullThenThrowNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new CredentialFileInput("private.key", null, null));
    }

    @Test
    void whenContentIsEmptyThenReturnZeroSize() {
        CredentialFileInput input = new CredentialFileInput("empty.txt", null, new byte[0]);

        assertEquals(0, input.size());
        assertEquals(0, input.content().length);
    }

    @Test
    void whenClearThenZeroInternalContent() {
        CredentialFileInput input = new CredentialFileInput("private.key", null, new byte[]{1, 2, 3});

        input.clear();

        assertArrayEquals(new byte[]{0, 0, 0}, input.content());
    }

    @Test
    void whenComparingInputsWithSameValuesThenReturnEqualAndSameHashCode() {
        CredentialFileInput first = new CredentialFileInput("private.key", "application/octet-stream",
                new byte[]{1, 2, 3});
        CredentialFileInput second = new CredentialFileInput("private.key", "application/octet-stream",
                new byte[]{1, 2, 3});

        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void whenComparingInputsWithDifferentValuesThenReturnNotEqual() {
        CredentialFileInput first = new CredentialFileInput("private.key", "application/octet-stream",
                new byte[]{1, 2, 3});
        CredentialFileInput second = new CredentialFileInput("other.key", "text/plain", new byte[]{1, 2, 4});

        assertNotEquals(first, second);
    }

    @Test
    void whenComparingInputsWithDifferentContentTypeThenReturnNotEqual() {
        CredentialFileInput first = new CredentialFileInput("private.key", "application/octet-stream",
                new byte[]{1, 2, 3});
        CredentialFileInput second = new CredentialFileInput("private.key", "text/plain", new byte[]{1, 2, 3});

        assertNotEquals(first, second);
    }

    @Test
    void whenComparingInputsWithDifferentContentThenReturnNotEqual() {
        CredentialFileInput first = new CredentialFileInput("private.key", null, new byte[]{1, 2, 3});
        CredentialFileInput second = new CredentialFileInput("private.key", null, new byte[]{1, 2, 4});

        assertNotEquals(first, second);
    }

    @Test
    void whenComparingInputWithNullThenReturnNotEqual() {
        CredentialFileInput input = new CredentialFileInput("private.key", null, new byte[]{1});

        assertNotEquals(null, input);
    }

    @Test
    void whenConvertingInputToStringThenDoNotExposeContent() {
        CredentialFileInput input = new CredentialFileInput("private.key", "application/octet-stream",
                new byte[]{1, 2, 3});

        assertEquals("CredentialFileInput[originalFilename=private.key, contentType=application/octet-stream, size=3]",
                input.toString());
    }
}
