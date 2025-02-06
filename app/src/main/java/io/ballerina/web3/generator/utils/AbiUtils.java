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
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.ballerina.web3.abi.AbiInput;

public class AbiUtils {
    public static String encodeParameters(List<AbiInput> params) {
        StringBuilder encodedParams = new StringBuilder();

        for (Object param : params) {
            String paramEncoded;

            if (param instanceof Integer || param instanceof Long) {
                paramEncoded = String.format("%064x", (Integer) param); // 64-character hex
            } else if (param instanceof Boolean) {
                paramEncoded = ((Boolean) param) ? "0000000000000000000000000000000000000000000000000000000000000001"
                        : "0000000000000000000000000000000000000000000000000000000000000000";
            } else if (param instanceof String strParam) {
                if (strParam.startsWith("0x") && strParam.length() == 42) {
                    paramEncoded = strParam.substring(2); // Address (40 hex chars)
                } else {
                    paramEncoded = stringToHex(strParam).concat("0".repeat(64)).substring(0, 64); // ✅ String encoding
                }
            } else {
                throw new IllegalArgumentException("Unsupported ABI type: " + param.getClass().getSimpleName());
            }

            encodedParams.append(paramEncoded);
        }

        return encodedParams.toString();
    }


    private static String stringToHex(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
