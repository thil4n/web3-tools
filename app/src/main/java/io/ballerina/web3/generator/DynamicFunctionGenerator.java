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

package io.ballerina.web3.generator;

import java.util.ArrayList;
import java.util.List;

import org.ballerinalang.formatter.core.FormatterException;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.web3.abi.AbiEntry;
import io.ballerina.web3.abi.AbiInput;
import io.ballerina.web3.abi.AbiOutput;
import io.ballerina.web3.generator.utils.BallerinaUtils;
import io.ballerina.web3.generator.utils.CodeGeneratorUtils;

public class DynamicFunctionGenerator {

        private static String convertAbiTypeToBallerina(String abiType) {
                if (abiType.endsWith("[]")) {
                        String baseType = abiType.substring(0, abiType.length() - 2);
                        return convertAbiTypeToBallerina(baseType) + "[]";
                }

                return switch (abiType) {
                        // All unsigned integer types
                        case "uint8", "uint16", "uint32", "uint64", "uint128", "uint256" -> "int";
                        // All signed integer types
                        case "int8", "int16", "int32", "int64", "int128", "int256" -> "int";
                        case "bool" -> "boolean";
                        case "address" -> "string";
                        case "string" -> "string";
                        // Fixed-size byte arrays (bytes1 through bytes32)
                        case "bytes", "bytes1", "bytes2", "bytes3", "bytes4",
                             "bytes5", "bytes6", "bytes7", "bytes8",
                             "bytes9", "bytes10", "bytes11", "bytes12",
                             "bytes13", "bytes14", "bytes15", "bytes16",
                             "bytes17", "bytes18", "bytes19", "bytes20",
                             "bytes21", "bytes22", "bytes23", "bytes24",
                             "bytes25", "bytes26", "bytes27", "bytes28",
                             "bytes29", "bytes30", "bytes31", "bytes32" -> "byte[]";
                        default -> {
                                if (abiType.startsWith("uint") || abiType.startsWith("int")) {
                                        yield "int";
                                }
                                yield "json"; // Fallback for complex/unknown types
                        }
                };
        }

        private static String generateBallerinaReturnType(List<AbiOutput> outputs, String functionName,
                        String stateMutability) {
                boolean isReadOnly = "view".equals(stateMutability) || "pure".equals(stateMutability);

                if (!isReadOnly) {
                        // State-mutating functions return the transaction hash
                        return "string|error";
                }

                if (outputs.isEmpty()) {
                        return "error?";
                }

                // If there's only one output, return its native type
                if (outputs.size() == 1) {
                        return convertAbiTypeToBallerina(outputs.get(0).getType()) + "|error?";
                }

                // If multiple outputs, generate an inline record type
                StringBuilder recordType = new StringBuilder("record {| ");

                for (int i = 0; i < outputs.size(); i++) {
                        AbiOutput output = outputs.get(i);
                        recordType.append(convertAbiTypeToBallerina(output.getType()))
                                        .append(" ")
                                        .append(output.getName().isEmpty() ? "value" + i
                                                        : output.getName())
                                        .append("; ");
                }

                recordType.append("|}|error?");
                return recordType.toString();
        }

        private static String generateResourceFunctionSignature(List<AbiInput> inputs, List<AbiOutput> outputs,
                        String methodName, String stateMutability) {
                StringBuilder data = new StringBuilder();

                // Define the function signature
                data.append("resource isolated function post ").append(methodName).append("(");

                // Add function parameters
                if (!inputs.isEmpty()) {
                        for (int i = 0; i < inputs.size(); i++) {
                                AbiInput input = inputs.get(i);

                                String sanitizedInputName = BallerinaUtils.sanitizeParameterName(input.getName(), i);

                                data.append(convertAbiTypeToBallerina(input.getType())).append(" ")
                                                .append(sanitizedInputName);

                                if (i < inputs.size() - 1) {
                                        data.append(", "); // Add comma separator
                                }
                        }
                }

                data.append(") returns ");

                String returnType = generateBallerinaReturnType(outputs, methodName, stateMutability);
                data.append(returnType);
                return data.toString();
        }

        private static String generateDecodingLogic(List<AbiOutput> outputs) {
                if (outputs.size() == 1) {
                        return generateSingleOutputDecoding(outputs.get(0).getType());
                }

                // For multiple outputs, decode each 32-byte slot
                StringBuilder logic = new StringBuilder();
                logic.append("string rawHex = response.result.startsWith(\"0x\") ? response.result.substring(2) : response.result;\n");

                for (int i = 0; i < outputs.size(); i++) {
                        AbiOutput output = outputs.get(i);
                        String slotVar = "slot" + i;
                        int offset = i * 64;
                        logic.append("string ").append(slotVar).append(" = rawHex.substring(")
                                        .append(offset).append(", ").append(offset + 64).append(");\n");

                        String fieldName = output.getName().isEmpty() ? "value" + i : output.getName();
                        String decodeExpr = generateSlotDecoding(output.getType(), slotVar);
                        logic.append(convertAbiTypeToBallerina(output.getType())).append(" ").append(fieldName)
                                        .append(" = ").append(decodeExpr).append(";\n");
                }

                // Build the return record
                logic.append("return {");
                for (int i = 0; i < outputs.size(); i++) {
                        AbiOutput output = outputs.get(i);
                        String fieldName = output.getName().isEmpty() ? "value" + i : output.getName();
                        if (i > 0) {
                                logic.append(", ");
                        }
                        logic.append(fieldName);
                }
                logic.append("};\n");

                // Remove the trailing "return result;" that the caller appends
                return logic.toString();
        }

        private static String generateSlotDecoding(String type, String slotVar) {
                if (type.equals("bool")) {
                        return slotVar + ".endsWith(\"1\")";
                } else if (type.equals("address")) {
                        return "\"0x\" + " + slotVar + ".substring(24)";
                } else if (type.equals("string") || type.equals("bytes")) {
                        return slotVar; // Dynamic types need offset-based decoding (simplified)
                } else if (type.startsWith("uint") || type.startsWith("int")) {
                        return "check hexToDecimal(" + slotVar + ")";
                } else if (type.startsWith("bytes")) {
                        return slotVar + ".toBytes()";
                }
                return slotVar;
        }

        private static String generateSingleOutputDecoding(String outputType) {
                return switch (outputType) {
                        case "uint256", "uint128", "uint64", "uint32", "uint16", "uint8",
                             "int256", "int128", "int64", "int32", "int16", "int8" ->
                                """
                                string rawHex = response.result.startsWith("0x") ? response.result.substring(2) : response.result;
                                int result = check hexToDecimal(rawHex);
                                """;
                        case "bool" ->
                                "boolean result = response.result.endsWith(\"1\");\n";
                        case "address" ->
                                """
                                string rawHex = response.result.startsWith("0x") ? response.result.substring(2) : response.result;
                                string result = "0x" + rawHex.substring(24);
                                """;
                        case "string" ->
                                """
                                string rawHex = response.result.startsWith("0x") ? response.result.substring(2) : response.result;
                                string result = rawHex;
                                """;
                        default -> {
                                if (outputType.startsWith("uint") || outputType.startsWith("int")) {
                                        yield """
                                        string rawHex = response.result.startsWith("0x") ? response.result.substring(2) : response.result;
                                        int result = check hexToDecimal(rawHex);
                                        """;
                                } else if (outputType.startsWith("bytes")) {
                                        yield """
                                        string rawHex = response.result.startsWith("0x") ? response.result.substring(2) : response.result;
                                        byte[] result = rawHex.toBytes();
                                        """;
                                }
                                yield "json result = response.result;\n";
                        }
                };
        }

        private static String generateParameterList(List<AbiInput> inputs) {
                StringBuilder data = new StringBuilder();

                for (int i = 0; i < inputs.size(); i++) {

                        AbiInput input = inputs.get(i);
                        String sanitizedInputName = BallerinaUtils.sanitizeParameterName(input.getName(), i);

                        data.append(sanitizedInputName);

                        if (i < inputs.size() - 1) {
                                data.append(", "); // Add comma separator
                        }
                }

                return data.toString();
        }

        private static String generateResourceFunctionBody(List<AbiInput> inputs, List<AbiOutput> outputs,
                        String functionSelector, String stateMutability) {

                String parameterList = generateParameterList(inputs);
                boolean isReadOnly = "view".equals(stateMutability) || "pure".equals(stateMutability);
                boolean isPayable = "payable".equals(stateMutability);

                StringBuilder result = new StringBuilder();

                result.append("""
                                // Encode function parameters
                                string encodedParameters = encodeParameters([%s]);
                                string callData =  "0x" + "%s" + encodedParameters;

                                """.formatted(parameterList, functionSelector));

                if (isReadOnly) {
                        // View/pure functions use eth_call
                        result.append("""
                                // Generate the JSON-RPC request body (read-only call)
                                json requestBody = {
                                    "jsonrpc": "2.0",
                                    "method": "eth_call",
                                    "params": [
                                        {"to": self.address, "data": callData},
                                        "latest"
                                    ],
                                    "id": 1
                                };

                                // Send the request and get response
                                record {string result;} response = check self.rpcClient->post("/", requestBody);

                                """);
                } else {
                        // State-mutating functions use eth_sendTransaction
                        result.append("""
                                // Generate the JSON-RPC request body (state-changing transaction)
                                json txnParams = {
                                    "from": self.sender,
                                    "to": self.address,
                                    "data": callData
                                };
                                """);

                        if (isPayable) {
                                result.append("""

                                // Note: For payable functions, set the "value" field in txnParams before sending.
                                """);
                        }

                        result.append("""

                                json requestBody = {
                                    "jsonrpc": "2.0",
                                    "method": "eth_sendTransaction",
                                    "params": [txnParams],
                                    "id": 1
                                };

                                // Send the transaction and get the transaction hash
                                record {string result;} response = check self.rpcClient->post("/", requestBody);

                                """);
                }

                // Add decoding logic only if outputs are present
                if (!outputs.isEmpty() && isReadOnly) {
                        String decodingLogic = generateDecodingLogic(outputs);
                        result.append(decodingLogic);
                        // Multi-output decoding already includes its own return statement
                        if (outputs.size() == 1) {
                                result.append("return result;");
                        }
                } else if (!isReadOnly) {
                        // State-mutating functions return the transaction hash
                        result.append("string result = response.result;\n");
                        result.append("return result;");
                }

                return result.toString();
        }

        private static FunctionDefinitionNode generateResourceFunction(AbiEntry abiEntry) {
                List<AbiInput> inputs = abiEntry.getInputs() != null ? abiEntry.getInputs() : List.of();
                List<AbiOutput> outputs = abiEntry.getOutputs() != null ? abiEntry.getOutputs() : List.of();
                String stateMutability = abiEntry.getStateMutability() != null ? abiEntry.getStateMutability() : "";

                String methodName = BallerinaUtils.sanitizeMethodName(abiEntry.getName());

                String functionSelector = CodeGeneratorUtils.generateFunctionSelector(abiEntry);

                // Generate function signature and body
                String functionSignature = generateResourceFunctionSignature(inputs, outputs, methodName,
                                stateMutability);
                String functionBody = generateResourceFunctionBody(inputs, outputs, functionSelector, stateMutability);

                // Ensure correct Ballerina syntax
                return (FunctionDefinitionNode) NodeParser.parseObjectMember(
                                String.format("""
                                                %s {
                                                    %s
                                                }
                                                """, functionSignature, functionBody));
        }

        public static List<Node> generate(List<AbiEntry> abiEntries) throws FormatterException {

                List<Node> memberNodes = new ArrayList<>();

                // Generate resource functions for each ABI entry
                for (AbiEntry abiEntry : abiEntries) {
                        FunctionDefinitionNode resourceMethod = generateResourceFunction(abiEntry);
                        memberNodes.add(resourceMethod);
                }

                return memberNodes;
        }
}
