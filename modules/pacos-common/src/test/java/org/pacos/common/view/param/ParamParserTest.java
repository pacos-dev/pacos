package org.pacos.common.view.param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
        Map<String, String> expected = Map.of(
                "name1", "value1",
                "name2", "value2",
                "name3", "value3"
        );
        Map<String, String> actual = result.stream()
                .collect(Collectors.toMap(Param::getName, Param::getValue));
        assertEquals(expected, actual);
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
        Set<Param> result = ParamParser.mapFromString("\"name\":\" \"");

        // Then
        assertEquals(1, result.size());
        assertTrue(result.iterator().next().getValue().isEmpty());
        assertEquals("name", result.iterator().next().getName());
    }

    @ParameterizedTest
    @MethodSource("lineSeparatorCases")
    void whenTextContainsLineSeparatorThenReturnEscapedSeparator(String text, String expected) {
        assertEquals(expected, ParamParser.detectLineSeparator(text));
    }

    private static Stream<Arguments> lineSeparatorCases() {
        return Stream.of(
                Arguments.of("Line1\r\nLine2", "\\r\\n"),
                Arguments.of("Line1\nLine2", "\\n"),
                Arguments.of("Line1\rLine2", "\\r"),
                Arguments.of("Line1Line2", "\\r")
        );
    }
}
