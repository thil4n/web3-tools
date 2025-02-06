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
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.web3.abi.AbiEntry;
import io.ballerina.web3.abi.AbiInput;
import io.ballerina.web3.abi.AbiOutput;

import java.util.ArrayList;
import java.util.List;

import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

public class CodeGenerator {

        private String convertAbiTypeToBallerina(String abiType) {
                return switch (abiType) {
                        case "uint256", "int256", "uint8", "int8", "uint16", "int16" -> "int";
                        case "bool" -> "boolean";
                        case "address" -> "string";
                        case "string", "bytes" -> "string";
                        case "array" -> "anydata[]";
                        default -> "anydata";
                };
        }

        public String generateResourceFunctionSignature(List<AbiInput> inputs, List<AbiOutput> outputs,
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

                // Determine return type
                if (outputs.isEmpty()) {
                        data.append("error"); // Default to error if no outputs
                } else if (outputs.size() == 1) {
                        data.append(convertAbiTypeToBallerina(outputs.get(0).getType())).append("|error");
                } else {
                        data.append("record {| ");
                        for (AbiOutput output : outputs) {
                                data.append(convertAbiTypeToBallerina(output.getType())).append(" ")
                                                .append(output.getName()).append("; ");
                        }
                        data.append("|} | error");
                }

                return data.toString();
        }

        FunctionDefinitionNode generateResourceFunction(AbiEntry abiEntry) {

                List<AbiInput> inputs = abiEntry.getInputs();
                List<AbiOutput> outputs = abiEntry.getOutputs();
                String methodName = abiEntry.getName();

                // Generate the resource function signature
                String functionSignature = generateResourceFunctionSignature(inputs, outputs, methodName);

                return (FunctionDefinitionNode) NodeParser.parseObjectMember(
                                String.format("resource function %s %s() returns %s {}", "method", "name", "returnType"));
        }

        public static void generate(AbiEntry[] abiEntries) throws FormatterException {

                // Using NodeParser API to parse import declaration
                ImportDeclarationNode importDecl = (ImportDeclarationNode) NodeParser.parseImportDeclaration(
                                "import ballerina/http;");

                // Using NodeParser API with templates to generate service declaration
                ServiceDeclarationNode serviceDecl = (ServiceDeclarationNode) NodeParser.parseModuleMemberDeclaration(
                                String.format("service %s on %s { }", "/", "new http:Listener(9090)"));

                // Using NodeParser API with templates to generate resource method
                FunctionDefinitionNode resourceMethod = (FunctionDefinitionNode) NodeParser.parseObjectMember(
                                String.format("resource function %s %s() returns %s {}", "get", "foo", "json"));

                // Using NodeFactory API to modify service declaration with resource methods
                List<Node> memberNodes = new ArrayList<>();
                memberNodes.add(resourceMethod);
                NodeList<Node> members = NodeFactory.createNodeList(memberNodes);
                serviceDecl = serviceDecl.modify().withMembers(members).apply();

                // Create a ModulePartNode including the import and service
                List<ModuleMemberDeclarationNode> moduleMembers = new ArrayList<>();
                moduleMembers.add(serviceDecl);

                ModulePartNode modulePartNode = NodeFactory.createModulePartNode(
                                NodeFactory.createNodeList(importDecl),
                                NodeFactory.createNodeList(moduleMembers),
                                NodeFactory.createToken(SyntaxKind.EOF_TOKEN));

                // Generate the source code
                String sourceCode = modulePartNode.toSourceCode();

                // Parse the source code
                TextDocument textDocument = TextDocuments.from(sourceCode);
                SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
                syntaxTree = Formatter.format(syntaxTree);

                System.out.println(syntaxTree.toSourceCode());
        }
}
