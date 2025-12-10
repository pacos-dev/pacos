package org.pacos.common.view.param;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

/**
 * This class is used to parse parameter from object to string and backward
 * Used by clipboard copy and paste functionality on grid
 */
public class ParamParser {

    private ParamParser() {
    }

    public static String mapToString(Collection<Param> variables) {
        return variables.stream()
                .filter(r -> (StringUtils.hasLength(r.getName()) && !r.isEmpty()))
                .map(ParamParser::variableToString)
                .collect(Collectors.joining("\n"));
    }

    public static Set<Param> mapFromString(String string) {
        Set<Param> params = new HashSet<>();
        if (!StringUtils.hasLength(string)) {
            return params;
        }
        if (string.startsWith("{") && string.endsWith("}")) {
            string = string.substring(1, string.length() - 1);
            string = string.replace(",", "\n");
        }
        String[] lines = string.split(detectLineSeparator(string));

        for (String line : lines) {
            // Check if the line matches the expected format "name":"value"
            if (line.trim().matches("^\"([^\"]+)\":\"([^\"]*)\"$")) {
                // Remove surrounding quotes and split by ":"
                line = line.trim().replaceAll("((^\")|(\"$))", "");
                String[] parts = line.split("\":\""); // Split by ":" inside quotes
                if (parts.length == 1) {
                    params.add(new Param(parts[0].trim(), "", true));
                } else {
                    params.add(new Param(parts[0].trim(), parts[1].trim(), true));
                }
            }
        }

        return params;
    }

    static String detectLineSeparator(String text) {
        if (text.contains("\r\n")) {
            return "\\r\\n"; // Windows-style separator
        } else if (text.contains("\n")) {
            return "\\n"; // Unix-style separator
        } else {
            return "\\r"; // Old MacOS separator
        }
    }

    private static String variableToString(Param v) {
        if (!StringUtils.hasLength(v.getName())) {
            return "";
        }
        return String.format("\"%s\":\"%s\"", v.getName(), v.getValue() == null ? "" : v.getValue());
    }
}
