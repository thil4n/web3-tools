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

import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.web3.abi.AbiEntry;
import io.ballerina.web3.abi.AbiInput;
import io.ballerina.web3.abi.AbiOutput;
import io.ballerina.web3.generator.utils.AbiUtils;
import io.ballerina.web3.generator.utils.BallerinaUtils;
import io.ballerina.web3.generator.utils.CodeGeneratorUtils;

import java.util.ArrayList;
import java.util.List;

import org.ballerinalang.formatter.core.FormatterException;

public class FunctionGenerator {

        private static String convertAbiTypeToBallerina(String abiType) {
                if (abiType.endsWith("[]")) {
                        return convertAbiTypeToBallerina(abiType.replace("[]", "")) + "[]";
                }

                return switch (abiType) {
                        case "uint256", "int256", "uint8", "int8", "uint16", "int16" -> "int";
                        case "bool" -> "boolean";
                        case "address" -> "string";
                        case "string", "bytes" -> "string";
                        default -> "anydata"; // Fallback
                };
        }

        private static String generateBallerinaReturnType(List<AbiOutput> outputs, String functionName) {
                if (outputs.isEmpty()) {
                        return "error"; // Default to error if no return type
                }

                // If there's only one output, return its native type
                if (outputs.size() == 1) {
                        return convertAbiTypeToBallerina(outputs.get(0).getType()) + "|error";
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
                                data.append(convertAbiTypeToBallerina(input.getType())).append(" ")
                                                .append(input.getName());

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

        private static String generateResourceFunctionBody(List<AbiInput> inputs, String functionSelector) {

                StringBuilder callData = new StringBuilder();

                callData.append("0x");
                callData.append(functionSelector);
                
                String encodedParameters = AbiUtils.encodeParameters(inputs);
                
                callData.append(encodedParameters);

                return """            
                        // Generate the JSON-RPC request body
                        json requestBody = {
                            "jsonrpc": "2.0",
                            "method": "eth_call",
                            "params": [
                                {"to": self.address, "data": %s},
                                "latest"
                            ],
                            "id": 1
                        };
            
                        // Send the request and get response
                        json response = check self.httpClient->post(self.nodeUrl, requestBody);
                        if response is json {
                            return response;
                        } else {
                            return error("Blockchain call failed");
                        }
                        """.formatted(callData.toString());
            }
            

        private static FunctionDefinitionNode generateResourceFunction(AbiEntry abiEntry) {
                List<AbiInput> inputs = abiEntry.getInputs();
                List<AbiOutput> outputs = abiEntry.getOutputs();

                String methodName = BallerinaUtils.sanitizeMethodName(abiEntry.getName());

                String functionSelector = CodeGeneratorUtils.generateFunctionSelector(abiEntry);

                // Generate function signature and body
                String functionSignature = generateResourceFunctionSignature(inputs, outputs, methodName);
                String functionBody = generateResourceFunctionBody(inputs, functionSelector);

                // Ensure correct Ballerina syntax
                return (FunctionDefinitionNode) NodeParser.parseObjectMember(
                                String.format("""
                                                %s {
                                                    %s
                                                }
                                                """, functionSignature, functionBody));
        }

        private static FunctionDefinitionNode generateInitFunction() {
                StringBuilder data = new StringBuilder();

                data.append("//Initialize the client\n");
                data.append("public function init(string api) returns error? {\n");
                data.append("self.api = api;\n\n");

                data.append("// Create a client configuration to disable HTTP/2 upgrades\n");
                data.append("http:ClientConfiguration clientConfig = {\n");
                data.append("httpVersion: http:HTTP_1_1\n");
                data.append("};\n");

                data.append("\n// Initialize the HTTP client with the given API URL and configuration\n");
                data.append("self.rpcClient = check new (self.api, clientConfig);\n");

                data.append("}\n");

                return  (FunctionDefinitionNode) NodeParser.parseObjectMember(data.toString());
        }

        public static List<Node> generate(AbiEntry[] abiEntries) throws FormatterException {

                List<Node> memberNodes = new ArrayList<>();

                // Add the init function
                memberNodes.add(generateInitFunction());

                // Generate resource functions for each ABI entry
                for (AbiEntry abiEntry : abiEntries) {
                        FunctionDefinitionNode resourceMethod = generateResourceFunction(abiEntry);
                        memberNodes.add(resourceMethod);
                }

                return memberNodes;
        }
}
