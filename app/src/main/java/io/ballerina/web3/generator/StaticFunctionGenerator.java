package io.ballerina.web3.generator;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.NodeParser;

import java.util.List;

public class StaticFunctionGenerator {

    /**
     * Generates the function for initializing the Ethereum client.
     * It takes API and address as input parameters and initializes the client.
     * 
     * @return The generated function definition for the initialization.
     */
    private static FunctionDefinitionNode generateInitFunction() {
        String data = """
                /// Initialize the Ethereum client.
                public function init(string api, string address) returns error? {
                    self.api = api;
                    self.address = address;

                    http:ClientConfiguration clientConfig = {
                        httpVersion: http:HTTP_1_1
                    };

                    self.rpcClient = check new(self.api, clientConfig);
                }
                """;
        return (FunctionDefinitionNode) NodeParser.parseObjectMember(data);
    }

    /**
     * Generates the function to set the Ethereum contract address.
     * It takes an address string as input and assigns it to the class instance.
     * 
     * @return The generated function definition for setting the contract address.
     */
    private static FunctionDefinitionNode generateSetContractAddressFunction() {
        String data = """
                /// Set the contract address.
                public function setContractAddress(string address) {
                    self.address = address;
                }
                """;
        return (FunctionDefinitionNode) NodeParser.parseObjectMember(data);
    }

    /**
     * Generates the function to get the list of Ethereum accounts available on the node.
     * It performs a JSON-RPC call to retrieve the accounts.
     * 
     * @return The generated function definition for getting the list of accounts.
     */
    private static FunctionDefinitionNode generateGetAccountsFunction() {
        String data = """
                /// Get a list of Ethereum accounts available in the node.
                /// # Returns
                /// - `string[]`: List of accounts.
                /// - `error`: Error if the request fails.
                public function getAccounts() returns string[]|error {
                    json requestBody = {
                        "jsonrpc": "2.0",
                        "method": "eth_accounts",
                        "params": [],
                        "id": 1
                    };

                    record{string[] result;} response = check self.rpcClient->post("/", requestBody);
                    return response.result;
                }
                """;
        return (FunctionDefinitionNode) NodeParser.parseObjectMember(data);
    }

    /**
     * Generates the function to get the balance of an Ethereum address.
     * It takes an Ethereum address as input and returns the balance in Wei.
     * 
     * @param address The Ethereum address to get the balance for.
     * @return The generated function definition for getting the balance.
     */
    private static FunctionDefinitionNode generateGetBalanceFunction() {
        String data = """
                /// Get the balance of an Ethereum address.
                /// # Parameters
                /// - `address`: The Ethereum address.
                /// # Returns
                /// - `decimal`: Balance in Wei.
                /// - `error`: If the request fails.
                public function getBalance(string address) returns decimal|error {
                    json requestBody = {
                        "jsonrpc": "2.0",
                        "method": "eth_getBalance",
                        "params": [address, "latest"],
                        "id": 1
                    };

                    record { string result; } response = check self.rpcClient->post("/", requestBody);

                    string sanitizedHex = response.result.substring(2);
                    return check hexToDecimal(sanitizedHex);
                }
                """;
        return (FunctionDefinitionNode) NodeParser.parseObjectMember(data);
    }

    /**
     * Generates the function to get the latest block number on the Ethereum blockchain.
     * It makes a JSON-RPC call to retrieve the current block number.
     * 
     * @return The generated function definition for getting the block number.
     */
    private static FunctionDefinitionNode generateGetBlockNumberFunction() {
        String data = """
                /// Get the latest block number on the Ethereum blockchain.
                /// # Returns
                /// - `decimal`: The block number.
                /// - `error`: Error if the request fails.
                public function getBlockNumber() returns decimal|error {
                    json requestBody = {
                        "jsonrpc": "2.0",
                        "method": "eth_blockNumber",
                        "params": [],
                        "id": 1
                    };

                    record { string result; } response = check self.rpcClient->post("/", requestBody);

                    string sanitizedHex = response.result.substring(2);
                    return check hexToDecimal(sanitizedHex);
                }
                """;
        return (FunctionDefinitionNode) NodeParser.parseObjectMember(data);
    }

    /**
     * Generates the function to get the number of transactions sent from an Ethereum address.
     * It takes an address as input and returns the transaction count.
     * 
     * @param address The Ethereum address to get the transaction count for.
     * @return The generated function definition for getting the transaction count.
     */
    private static FunctionDefinitionNode generateGetTransactionCountFunction() {
        String data = """
                /// Get the number of transactions sent from an address.
                /// # Parameters
                /// - `address`: The Ethereum address.
                /// # Returns
                /// - `decimal`: The number of transactions sent from the address.
                /// - `error`: Error if the request fails.
                public function getTransactionCount(string address) returns decimal|error {
                    json requestBody = {
                        "jsonrpc": "2.0",
                        "method": "eth_getTransactionCount",
                        "params": [address, "latest"],
                        "id": 1
                    };

                    record { string result; } response = check self.rpcClient->post("/", requestBody);

                    string sanitizedHex = response.result.substring(2);
                    return check hexToDecimal(sanitizedHex);
                }
                """;
        return (FunctionDefinitionNode) NodeParser.parseObjectMember(data);
    }

    /**
     * Converts a value in Wei to Ether.
     * 
     * @param weiAmount The amount in Wei to be converted.
     * @return The equivalent value in Ether.
     */
    private static FunctionDefinitionNode generateWeiToEtherFunction() {
        String data = """
                 public function weiToEther(decimal weiAmount) returns decimal {
                    decimal etherValue = weiAmount / 1e18;
                    return etherValue;
                }
                """;
        return (FunctionDefinitionNode) NodeParser.parseObjectMember(data);
    }

    /**
     * Converts a value in Ether to Wei.
     * 
     * @param etherValue The amount in Ether to be converted.
     * @return The equivalent value in Wei.
     */
    private static FunctionDefinitionNode generateEthToWeiFunction() {
        String data = """
                public function ethToWei(decimal etherValue) returns decimal {
                    decimal weiAmount = etherValue * 1e18;
                    return weiAmount;
                }
                            """;
        return (FunctionDefinitionNode) NodeParser.parseObjectMember(data);
    }

    /**
     * Generates all the required functions and returns them as a list.
     * 
     * @return A list of function definitions.
     */
    public static List<FunctionDefinitionNode> generate() {
        return List.of(
                generateInitFunction(),
                generateSetContractAddressFunction(),
                generateGetAccountsFunction(),
                generateGetBalanceFunction(),
                generateGetBlockNumberFunction(),
                generateGetTransactionCountFunction(),
                generateWeiToEtherFunction(),
                generateEthToWeiFunction()
                );
    }
}
