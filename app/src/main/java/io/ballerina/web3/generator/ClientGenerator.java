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

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import io.ballerina.web3.abi.AbiEntry;
import io.ballerina.web3.generator.utils.CodeGeneratorUtils;

import java.util.ArrayList;
import java.util.List;

import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

public class ClientGenerator {

        public static String generate(List<AbiEntry> abiEntries) throws FormatterException {

                // Separate functions and events
                List<AbiEntry> functions = new ArrayList<>();
                List<AbiEntry> events = new ArrayList<>();

                for (AbiEntry entry : abiEntries) {
                        if ("function".equals(entry.getType())) {
                                functions.add(entry);
                        } else if ("event".equals(entry.getType())) {
                                events.add(entry);
                        }
                }

                // Using NodeParser API with templates to generate client declaration
                ClassDefinitionNode serviceDecl = (ClassDefinitionNode) NodeParser.parseModuleMemberDeclaration(
                                "public client class Web3 {}");

                List<Node> members = new ArrayList<>();

                StringBuilder data = new StringBuilder();

                data.append("\n// The base URL of the Ethereum JSON-RPC API.\n");
                data.append("private final string api;\n");

                data.append("\n// The contract address.\n");
                data.append("private string address;\n");

                data.append("\n// The address of the sender (used for transactions).\n");
                data.append("private string sender = \"\";\n");

                data.append("\n// HTTP client to send JSON-RPC requests to the Ethereum node.\n");
                data.append("private final http:Client rpcClient;\n");

                Node clientProperties = NodeParser.parseObjectMember(data.toString());

                members.add(clientProperties);

                members.addAll(StaticFunctionGenerator.generate());
                members.addAll(DynamicFunctionGenerator.generate(functions));

                NodeList<Node> memberNodeList = NodeFactory.createNodeList(members);

                serviceDecl = serviceDecl.modify().withMembers(memberNodeList).apply();

                // Build module members: event constants + client class
                List<ModuleMemberDeclarationNode> moduleMembers = new ArrayList<>();

                // Generate event topic hash constants
                for (AbiEntry event : events) {
                        String eventHash = generateEventTopicHash(event);
                        if (eventHash != null) {
                                ModuleMemberDeclarationNode eventConst = (ModuleMemberDeclarationNode)
                                                NodeParser.parseModuleMemberDeclaration(eventHash);
                                moduleMembers.add(eventConst);
                        }
                }

                moduleMembers.add(serviceDecl);

                ImportDeclarationNode importDecl = (ImportDeclarationNode) NodeParser.parseImportDeclaration(
                                "import ballerina/http;");

                ModulePartNode modulePartNode = NodeFactory.createModulePartNode(
                                NodeFactory.createNodeList(importDecl),
                                NodeFactory.createNodeList(moduleMembers),
                                NodeFactory.createToken(SyntaxKind.EOF_TOKEN));

                String sourceCode = modulePartNode.toSourceCode();

                TextDocument textDocument = TextDocuments.from(sourceCode);
                SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
                syntaxTree = Formatter.format(syntaxTree);

                return syntaxTree.toSourceCode();
        }

        private static String generateEventTopicHash(AbiEntry event) {
                if (event.getName() == null) {
                        return null;
                }
                StringBuilder sig = new StringBuilder(event.getName()).append("(");
                if (event.getInputs() != null) {
                        for (int i = 0; i < event.getInputs().size(); i++) {
                                if (i > 0) {
                                        sig.append(",");
                                }
                                sig.append(event.getInputs().get(i).getType());
                        }
                }
                sig.append(")");
                String hash = CodeGeneratorUtils.hashKeccak256(sig.toString());
                String constName = "EVENT_" + event.getName().replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
                return String.format("# Event topic hash for `%s`\npublic const string %s = \"0x%s\";\n",
                                sig, constName, hash);
        }
}
