package io.ballerina.web3;

import io.ballerina.compiler.syntax.tree.*;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.*;
import static io.ballerina.compiler.syntax.tree.NodeFactory.*;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.*;

import java.util.ArrayList;
import java.util.List;


// SyntaxKind, MetadataNode, NodeList<Token>, Token, IdentifierToken, NodeList<Node>, FunctionSignatureNode, FunctionBodyNode
// null,       null,         Token,                  IdentifierToken,                 FunctionSignatureNode, FunctionBodyNode




public class Main {
    public static void main(String[] args) {
        FunctionDefinitionNode functionNode = createFunctionDefinitionNode(
            FUNCTION_DEFINITION, // No metadata
            null, // No metadata
            createEmptyNodeList(), // No qualifier
            createToken(FUNCTION_KEYWORD),  // "function" keyword
            createIdentifierToken("getBalance"), // Function name
            createEmptyNodeList(), // No qualifiers
            generateRemoteFunctionSignature(), // Function parameters
            generateRemoteFunctionBody() // Function body
    );


        NodeList<ModuleMemberDeclarationNode> members = createNodeList(functionNode);
        ModulePartNode modulePartNode = createModulePartNode(createEmptyNodeList(), members, createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from("");
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        syntaxTree.modifyWith(modulePartNode);

            System.out.println(syntaxTree.toSourceCode());
            System.out.println("syntaxTree.toSourceCode()");
        
    }

    /**
     * Generates the client class remote function body.
     *
     * @param queryDefinition the object instance of a single query definition in a
     *                        query document
     * @param graphQLSchema   the object instance of the GraphQL schema (SDL)
     * @param authConfig      the object instance representing authentication
     *                        configuration information
     * @return the node which represent the remote function body
     */
    public static FunctionBodyNode generateRemoteFunctionBody() {
        List<StatementNode> assignmentNodes = new ArrayList<>();

        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);

        return createFunctionBodyBlockNode(createToken(OPEN_BRACE_TOKEN),
                null, statementList, createToken(CLOSE_BRACE_TOKEN), null);
    }

    /**
     * Generates the client class remote function signature.
     *
     * @param queryDefinition the object instance of a single query definition in a
     *                        query document
     * @param graphQLSchema   the object instance of the GraphQL schema (SDL)
     * @return the node which represent the remote function signature
     */
    public static FunctionSignatureNode generateRemoteFunctionSignature() {
        // Define a parameter: `int amount`
        RequiredParameterNode param1 = NodeFactory.createRequiredParameterNode(
            createEmptyNodeList(), // No annotations
            NodeFactory.createBuiltinSimpleNameReferenceNode(null, createIdentifierToken("int")), // Type
                createIdentifierToken("amount") // Parameter name
        );

        // Define another parameter: `string name`
        RequiredParameterNode param2 = NodeFactory.createRequiredParameterNode(
            createEmptyNodeList(), // No annotations
                NodeFactory.createBuiltinSimpleNameReferenceNode(null, createIdentifierToken("string")),
                createIdentifierToken("name"));

        // Initialize a SeparatedNodeList with parameters
        SeparatedNodeList<ParameterNode> parameterList = NodeFactory.createSeparatedNodeList(
                param1, createToken(SyntaxKind.COMMA_TOKEN), param2);

        BuiltinSimpleNameReferenceNode returnType = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken("int"));

        ReturnTypeDescriptorNode returnTypeDescriptorNode = createReturnTypeDescriptorNode(
                createToken(RETURNS_KEYWORD), createEmptyNodeList(), returnType);

        return createFunctionSignatureNode(
                createToken(OPEN_PAREN_TOKEN), parameterList, createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode);
    }

    public static String getRemoteFunctionSignatureReturnTypeName(String operationName) {
        return operationName.substring(0, 1).toUpperCase() +
                operationName.substring(1).concat("Response|graphql:ClientError");
    }

}
