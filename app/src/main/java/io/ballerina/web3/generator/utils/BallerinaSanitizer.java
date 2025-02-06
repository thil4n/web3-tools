package io.ballerina.web3.generator.utils;

import java.util.Set;

public class BallerinaSanitizer {
    // List of Reserved Keywords in Ballerina
    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "function", "int", "boolean", "string", "record", "resource", "isolated",
            "error", "returns", "public", "private", "remote", "client", "self", "if", "else",
            "while", "foreach", "continue", "break", "return", "import", "type", "map"
    );

    /**
     * Sanitizes the method name to ensure it is valid in Ballerina.
     */
    public static String sanitizeMethodName(String methodName) {
        // Replace invalid characters (e.g., spaces, special chars)
        String sanitized = methodName.replaceAll("[^a-zA-Z0-9_]", "_");

        // Ensure it does not start with a number
        if (Character.isDigit(sanitized.charAt(0))) {
            sanitized = "_" + sanitized;
        }

        // Ensure it does not conflict with reserved keywords
        if (RESERVED_KEYWORDS.contains(sanitized)) {
            sanitized = sanitized + "_method"; // Append _method to avoid conflicts
        }

        return sanitized;
    }
}
