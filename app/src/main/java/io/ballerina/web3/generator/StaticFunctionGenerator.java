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
     * Generates the function to set the sender address used for transactions.
     * 
     * @return The generated function definition for setting the sender.
     */
    private static FunctionDefinitionNode generateSetSenderFunction() {
        String data = """
                /// Set the sender address for transactions.
                /// # Parameters
                /// - `sender`: The Ethereum address to use as the sender.
                public function setSender(string sender) {
                    self.sender = sender;
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
     * Generates the function to get the current gas price.
     * 
     * @return The generated function definition for getting the gas price.
     */
    private static FunctionDefinitionNode generateGetGasPriceFunction() {
        String data = """
                /// Get the current gas price in Wei.
                /// # Returns
                /// - `int`: The gas price in Wei.
                /// - `error`: Error if the request fails.
                public function getGasPrice() returns int|error {
                    json requestBody = {
                        "jsonrpc": "2.0",
                        "method": "eth_gasPrice",
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
     * Generates the function to get the chain ID.
     * 
     * @return The generated function definition for getting the chain ID.
     */
    private static FunctionDefinitionNode generateGetChainIdFunction() {
        String data = """
                /// Get the chain ID of the connected network.
                /// # Returns
                /// - `int`: The chain ID.
                /// - `error`: Error if the request fails.
                public function getChainId() returns int|error {
                    json requestBody = {
                        "jsonrpc": "2.0",
                        "method": "eth_chainId",
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
     * Generates all the required functions and returns them as a list.
     * 
     * @return A list of function definitions.
     */
    public static List<FunctionDefinitionNode> generate() {
        return List.of(
                generateInitFunction(),
                generateSetContractAddressFunction(),
                generateSetSenderFunction(),
                generateGetAccountsFunction(),
                generateGetBalanceFunction(),
                generateGetBlockNumberFunction(),
                generateGetTransactionCountFunction(),
                generateGetGasPriceFunction(),
                generateGetChainIdFunction(),
                generateWeiToEtherFunction(),
                generateEthToWeiFunction()
                );
    }
}
