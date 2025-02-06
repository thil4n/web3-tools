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
import io.ballerina.web3.generator.utils.BallerinaSanitizer;
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

        private static String generateResourceFunctionBody(String functionSelector) {
                StringBuilder data = new StringBuilder();

                data.append("    // Encode function parameters\n");
                data.append("    string encodedParameters = self.encodeParameters(parameters);\n");
                data.append("    string callData = \"0x\" + functionSelector + encodedParameters;\n\n");

                data.append("    // Generate the JSON-RPC request body\n");
                data.append("    json requestBody = {\n");
                data.append("        \"jsonrpc\": \"2.0\",\n");
                data.append("        \"method\": \"eth_call\",\n");
                data.append("        \"params\": [\n");
                data.append("            {\"to\": self.address, \"data\": callData},\n");
                data.append("            \"latest\"\n");
                data.append("        ],\n");
                data.append("        \"id\": 1\n");
                data.append("    };\n\n");

                data.append("    // Send the request and get response\n");
                data.append("    json response = check self.httpClient->post(self.nodeUrl, requestBody);\n");
                data.append("    if response is json {\n");
                data.append("        return response;\n");
                data.append("    } else {\n");
                data.append("        return error(\"Blockchain call failed\");\n");
                data.append("    }\n");

                return data.toString();
        }

        private static FunctionDefinitionNode generateResourceFunction(AbiEntry abiEntry) {
                List<AbiInput> inputs = abiEntry.getInputs();
                List<AbiOutput> outputs = abiEntry.getOutputs();

                String methodName = BallerinaSanitizer.sanitizeMethodName(abiEntry.getName());

                String functionSelector = CodeGeneratorUtils.generateFunctionSelector(abiEntry);

                // Generate function signature and body
                String functionSignature = generateResourceFunctionSignature(inputs, outputs, methodName);
                String functionBody = generateResourceFunctionBody(functionSelector);

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

                data.append("//Encode function parameters\n");
                data.append("public function init(string api) returns error? {\n");
                data.append("self.api = api;\n\n");

                data.append("// Create a client configuration to disable HTTP/2 upgrades\n");
                data.append("http:ClientConfiguration clientConfig = {\n");
                data.append("httpVersion: http:HTTP_1_1\n");
                data.append("};\n");

                data.append("// Initialize the HTTP client with the given API URL and configuration\n");
                data.append("self.rpcClient = check new (self.api, clientConfig)\n");

                data.append("}\n");

                return  (FunctionDefinitionNode) NodeParser.parseObjectMember(data.toString());
        }

        public static NodeList<Node> generate(AbiEntry[] abiEntries) throws FormatterException {

                List<Node> memberNodes = new ArrayList<>();

                // Add the init function
                memberNodes.add(generateInitFunction());

                // Generate resource functions for each ABI entry
                for (AbiEntry abiEntry : abiEntries) {
                        FunctionDefinitionNode resourceMethod = generateResourceFunction(abiEntry);
                        memberNodes.add(resourceMethod);
                }

                NodeList<Node> members = NodeFactory.createNodeList(memberNodes);
                return members;
        }
}
