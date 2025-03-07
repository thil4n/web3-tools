/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.web3.generator.utils;

import java.util.Set;

public class BallerinaUtils {
    // List of Reserved Keywords in Ballerina
    // TODO: use keywords from compiler
    private static final Set<String> RESERVED_KEYWORDS = Set.of(
            "function", "int", "boolean", "string", "record", "resource", "isolated",
            "error", "returns", "public", "private", "remote", "client", "self", "if", "else",
            "while", "foreach", "continue", "break", "return", "import", "type", "map", "from");

    /**
     * Sanitizes the identifier to ensure is valid in Ballerina.
     */
    private static String sanitizeIdentifier(String identifier, String suffixIfKeyword) {
        String sanitized = identifier.replaceAll("[^a-zA-Z0-9_]", "_");

        if (Character.isDigit(sanitized.charAt(0))) {
            sanitized = "_" + sanitized;
        }

        if (RESERVED_KEYWORDS.contains(sanitized)) {
            sanitized += suffixIfKeyword;
        }

        return sanitized;
    }

    /**
     * Sanitizes the method name to ensure it is valid in Ballerina.
     */
    public static String sanitizeMethodName(String methodName) {
        return sanitizeIdentifier(methodName, "_method");
    }

    /**
     * Sanitizes the parameter name to ensure it is valid in Ballerina.
     */
    public static String sanitizeParameterName(String parameterName, int index) {

        if (parameterName == null || parameterName.isEmpty()) {
            return "param" + index;
        }
        return sanitizeIdentifier(parameterName, "_param");

    }
}
