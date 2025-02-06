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
import java.util.ArrayList;
import java.util.List;

import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

public class ClientGenerator {

        public static String generate(AbiEntry[] abiEntries) throws FormatterException {

                // Using NodeParser API with templates to generate client declaration
                ClassDefinitionNode serviceDecl = (ClassDefinitionNode) NodeParser.parseModuleMemberDeclaration(
                                "public client class Web3 {}");

                // Using NodeFactory API to modify service declaration with resource methods
                List<Node> members = new ArrayList<>();

                StringBuilder data = new StringBuilder();

                data.append("\n// The base URL of the Ethereum JSON-RPC API.\n");
                data.append("private final string api;\n");

                data.append("\n// HTTP client to send JSON-RPC requests to the Ethereum node.\n");
                data.append("private final http:Client rpcClient;\n");


                // Node apiProperty =   NodeParser.parseObjectMember("private final string api;\n");
                Node clientProperty =   NodeParser.parseObjectMember(data.toString());

                // members.add(apiProperty);
                members.add(clientProperty);

                members.addAll(FunctionGenerator.generate(abiEntries));

                NodeList<Node> memberNodeList = NodeFactory.createNodeList(members);

                serviceDecl = serviceDecl.modify().withMembers(memberNodeList).apply();

                // Create a ModulePartNode including the import and service
                List<ModuleMemberDeclarationNode> moduleMembers = new ArrayList<>();
                moduleMembers.add(serviceDecl);

                // Using NodeParser API to parse import declaration
                ImportDeclarationNode importDecl = (ImportDeclarationNode) NodeParser.parseImportDeclaration(
                                "import ballerina/http;");

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

                String formattedSourceCode = syntaxTree.toSourceCode();

                return formattedSourceCode;
        }
}
