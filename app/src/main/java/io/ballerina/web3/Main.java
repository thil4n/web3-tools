package io.ballerina.web3;

import io.ballerina.compiler.syntax.tree.*;

import static io.ballerina.compiler.syntax.tree.NodeFactory.*;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.*;

import java.util.ArrayList;
import java.util.List;

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

        String sourceCode = modulePartNode.toSourceCode();

        System.out.print(sourceCode);
    }



    // Generate the function body
    public static FunctionBodyNode generateRemoteFunctionBody() {
        List<StatementNode> assignmentNodes = new ArrayList<>();
        NodeList<StatementNode> statementList = createNodeList(assignmentNodes);

        return createFunctionBodyBlockNode(
            createToken(OPEN_BRACE_TOKEN),
            null, statementList,
            createToken(CLOSE_BRACE_TOKEN),
            null
        );
    }


    // Generate the function signature
    public static FunctionSignatureNode generateRemoteFunctionSignature() {
        RequiredParameterNode param1 = createRequiredParameterNode(
            createEmptyNodeList(),
            createBuiltinSimpleNameReferenceNode(null, createIdentifierToken("int")),
            createIdentifierToken("amount")
        );

        RequiredParameterNode param2 = createRequiredParameterNode(
            createEmptyNodeList(),
            createBuiltinSimpleNameReferenceNode(null, createIdentifierToken("string")),
            createIdentifierToken("name")
        );

        SeparatedNodeList<ParameterNode> parameterList = createSeparatedNodeList(
            param1, createToken(SyntaxKind.COMMA_TOKEN), param2
        );

        BuiltinSimpleNameReferenceNode returnType = createBuiltinSimpleNameReferenceNode(null, createIdentifierToken("int"));

        ReturnTypeDescriptorNode returnTypeDescriptorNode = createReturnTypeDescriptorNode(
            createToken(RETURNS_KEYWORD), createEmptyNodeList(), returnType
        );

        return createFunctionSignatureNode(
            createToken(OPEN_PAREN_TOKEN), parameterList,
            createToken(CLOSE_PAREN_TOKEN), returnTypeDescriptorNode
        );
    }
}
