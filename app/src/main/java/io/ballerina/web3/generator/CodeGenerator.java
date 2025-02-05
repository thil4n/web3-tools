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

public class CodeGenerator {
    public static void generate(AbiEntry[] abiEntries) throws FormatterException {

        // Using NodeParser API to parse import declaration
        ImportDeclarationNode importDecl = (ImportDeclarationNode) NodeParser.parseImportDeclaration(
                "import ballerina/http;"
        );

        // Using NodeParser API with templates to generate service declaration
        ServiceDeclarationNode serviceDecl = (ServiceDeclarationNode) NodeParser.parseModuleMemberDeclaration(
                String.format("service %s on %s { }", "/", "new http:Listener(9090)")
        );
        
        // Using NodeParser API with templates to generate resource method
        FunctionDefinitionNode resourceMethod = (FunctionDefinitionNode) NodeParser.parseObjectMember(
                String.format("resource function %s %s() returns %s {}", "get", "foo", "json")
        );

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
                NodeFactory.createToken(SyntaxKind.EOF_TOKEN)
        );

        // Generate the source code
        String sourceCode = modulePartNode.toSourceCode();

        // Parse the source code
        TextDocument textDocument = TextDocuments.from(sourceCode);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        syntaxTree = Formatter.format(syntaxTree);

        System.out.println(syntaxTree.toSourceCode());
    }
}
