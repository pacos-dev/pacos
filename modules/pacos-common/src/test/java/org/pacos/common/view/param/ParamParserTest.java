package org.pacos.common.view.param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParamParserTest {

    @Test
    void whenMapParamThenReturnFormattedString() {
        // Given
        Collection<Param> params = List.of(
                new Param("name1", "value1", true),
                new Param("name2", "value2", true)
        );

        // When
        String result = ParamParser.mapToString(params);

        // Then
        String expected = """
                "name1":"value1"
                "name2":"value2"
                """;
        assertEquals(expected.trim(), result);
    }

    @Test
    void whenMapToStringThenShouldSkipEmptyParams() {
        // Given
        Collection<Param> params = List.of(
                new Param("name1", "value1", true),
                new Param("", "value2", true),
                new Param(null, "value3", true)
        );

        // When
        String result = ParamParser.mapToString(params);

        // Then
        String expected = """
                "name1":"value1"
                """.trim();
        assertEquals(expected, result);
    }

    @Test
    void whenMapFromStringThenShouldConvertStringToSetOfParams() {
        // Given
        String input = """
                "name1":"value1"
                "name2":"value2"
                "invalid line"
                "name3":"value3"
                """;

        // When
        Set<Param> result = ParamParser.mapFromString(input);

        // Then
        Set<Param> expected = Set.of(
                new Param("name1", "value1", true),
                new Param("name2", "value2", true),
                new Param("name3", "value3", true)
        );
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("name1")));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("name2")));
        assertTrue(result.stream().anyMatch(p -> p.getName().equals("name3")));
    }

    @Test
    void mapFromString_ShouldHandleEmptyInput() {
        // Given
        String input = "";

        // When
        Set<Param> result = ParamParser.mapFromString(input);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void whenMapFromStringThenIgnoreInvalidLines() {
        // Given
        String input = """
                "name1":"value1"
                invalid line
                "":""
                """;

        // When
        Set<Param> result = ParamParser.mapFromString(input);

        // Then
        assertEquals("name1", result.iterator().next().getName());
        assertEquals("value1", result.iterator().next().getValue());
    }

    @Test
    void whenMapFromStringThenMapEmptyValue() {
        // When
        Set<Param> result = ParamParser.mapFromString("\"name\":\"\"");

        // Then
        assertEquals(1, result.size());
        assertTrue(result.iterator().next().getValue().isEmpty());
        assertEquals("name", result.iterator().next().getName());
    }

    @Test
    void whenTextContainsWindowsSeparatorThenReturnCRLF() {
        String text = "Line1\r\nLine2";
        String result = ParamParser.detectLineSeparator(text);
        assertEquals("\\r\\n", result);
    }

    @Test
    void whenTextContainsUnixSeparatorThenReturnLF() {
        String text = "Line1\nLine2";
        String result = ParamParser.detectLineSeparator(text);
        assertEquals("\\n", result);
    }

    @Test
    void whenTextContainsOldMacSeparatorThenReturnCR() {
        String text = "Line1\rLine2";
        String result = ParamParser.detectLineSeparator(text);
        assertEquals("\\r", result);
    }

    @Test
    void whenTextContainsNoLineSeparatorThenReturnCR() {
        String text = "Line1Line2";
        String result = ParamParser.detectLineSeparator(text);
        assertEquals("\\r", result);
    }
}
