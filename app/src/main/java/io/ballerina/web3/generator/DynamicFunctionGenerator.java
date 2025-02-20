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
                        return convertAbiTypeToBallerina(abiType.replace("[]", "")) + "[]";
                }

                return switch (abiType) {
                        case "uint256", "int256", "uint8", "int8", "uint16", "int16" -> "decimal";
                        case "bool" -> "boolean";
                        case "address" -> "string";
                        case "string", "bytes" -> "string";
                        default -> "anydata"; // Fallback
                };
        }

        private static String generateBallerinaReturnType(List<AbiOutput> outputs, String functionName) {
                if (outputs.isEmpty()) {
                        return "error?"; // Default to error if no return type
                }

                // If there's only one output, return its native type
                if (outputs.size() == 1) {
                        return convertAbiTypeToBallerina(outputs.get(0).getType()) + "|error?";
                }

                // If multiple outputs, generate a record type
                String recordName = functionName + "Response";
                StringBuilder recordType = new StringBuilder("type ").append(recordName).append(" record {| ");

                for (AbiOutput output : outputs) {
                        recordType.append(convertAbiTypeToBallerina(output.getType()))
                                        .append(" ")
                                        .append(output.getName().isEmpty() ? "value" + outputs.indexOf(output)
                                                        : output.getName())
                                        .append("; ");
                }

                recordType.append("|};");
                return recordType.toString();
        }

        private static String generateResourceFunctionSignature(List<AbiInput> inputs, List<AbiOutput> outputs,
                        String methodName) {
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

                String returnType = generateBallerinaReturnType(outputs, methodName);
                data.append(returnType);
                return data.toString();
        }

        private static String generateDecodingLogic(List<AbiOutput> outputs) {
                String outputType = outputs.get(0).getType(); // Assuming single output for simplicity

                switch (outputType) {
                        case "uint256":
                        case "int256":
                                return "decimal|error result = check hexToDecimal(response.result.substring(2));\n";
                        case "bool":
                                return "boolean result = response.result == \"0x1\";\n";
                        case "address":
                                return "string result = \"0x\" + response.result.substring(26);\n"; // Last 20 bytes
                        case "string":
                                return "string result = response.result.substring(2);\n";
                        default:
                                return "// Unsupported type: " + outputType + "\n";
                }
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
                        String functionSelector) {

                String parameterList = generateParameterList(inputs);

                String result = """
                                // Encode function parameters
                                string encodedParameters = encodeParameters([%s]);
                                string callData =  "0x" + "%s" + encodedParameters;

                                // Generate the JSON-RPC request body
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

                                """.formatted(parameterList, functionSelector);

                // Add decoding logic only if outputs are present
                if (!outputs.isEmpty()) {
                        String decodingLogic = generateDecodingLogic(outputs);
                        result += decodingLogic;
                        result += "return result;";
                }

                return result;
        }

        private static FunctionDefinitionNode generateResourceFunction(AbiEntry abiEntry) {
                List<AbiInput> inputs = abiEntry.getInputs();
                List<AbiOutput> outputs = abiEntry.getOutputs();

                String methodName = BallerinaUtils.sanitizeMethodName(abiEntry.getName());

                String functionSelector = CodeGeneratorUtils.generateFunctionSelector(abiEntry);

                // Generate function signature and body
                String functionSignature = generateResourceFunctionSignature(inputs, outputs, methodName);
                String functionBody = generateResourceFunctionBody(inputs, outputs, functionSelector);

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
