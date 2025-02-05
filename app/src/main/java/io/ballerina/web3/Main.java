package io.ballerina.web3;

import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import java.util.ArrayList;
import java.util.List;

import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

public class Main {
    public static void main(String[] args) throws FormatterException {

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
