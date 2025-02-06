import ballerina/data.jsondata;
import ballerina/http;
import ballerina/io;
import ballerina/crypto;

/// Represents a smart contract on the Ethereum blockchain.
public class Contract {

    /// The address of the deployed smart contract.
    private final string address;

    /// The HTTP client used to interact with the Ethereum RPC server.
    private final http:Client rpcClient;

    /// A map to store contract methods and their ABI signatures (optional, not implemented here).
    private final ContractMethod[] methods;

    /// Initializes the `Contract` instance.
    ///
    /// # Parameters
    /// - `rpcClient`: The HTTP client for Ethereum RPC calls.
    /// - `jsonFilePath`: Path to the ABI JSON file (optional, for ABI parsing).
    /// - `address`: The deployed contract's Ethereum address.
    public function init(http:Client rpcClient, string jsonFilePath, string address) returns error? {
        self.rpcClient = rpcClient;
        self.address = address;

        json readJson = check io:fileReadJson(jsonFilePath);

        ContractJson content = check jsondata:parseAsType(readJson);

        self.methods = content.abi;
    }

    /// Calls a state-changing function on the contract using `eth_sendTransaction`.
    ///
    /// # Parameters
    /// - `functionHash`: The hash of the function signature.
    /// - `fromAddress`: The sender's Ethereum address.
    /// - `params`: The parameters for the function call.
    /// - `gas`: The gas limit for the transaction.
    /// - `gasPrice`: The gas price for the transaction.
    ///
    /// # Returns
    /// - The transaction hash as JSON.
    /// - An error if the call fails or the response is invalid.
    public function call(string functionName,json[] parameters) returns json|error {
        string functionSelector = self.generateFunctionSelector(functionName, ["uint256"]);
        string encodedParameters = self.encodeParameters(parameters);
        string data = "0x" + functionSelector + encodedParameters;

        

        io:println("data: ", data);

        // // json requestBody = {
        // //     "jsonrpc": "2.0",
        // //     "method": "eth_call",
        // //     "params": [
        // //         {"to": self.address, "data": data},
        // //         "latest"
        // //     ],
        // //     "id": 1
        // // };

        // json requestBody = {
        //     "jsonrpc": "2.0",
        //     "method": "eth_sendTransaction",
        //     "params": [
        //         {
        //             "from": fromAddress,
        //             "to": self.address,
        //             "gas": gas,
        //             "data": data
        //         }
        //     ],
        //     "id": 1
        // };

        // json response = check self.rpcClient->post("/", requestBody);
        // map<json>|error responseMap = response.ensureType();
        // if responseMap is error {
        //     return error("Invalid response.");
        // }

        // io:println("response: ", responseMap);

        // string|error result = responseMap.get("result").ensureType(string);
        // if result is error {
        //     return error("Invalid response.");
        // }

        // return result;
    }

    /// Estimates the gas required for a transaction using `eth_estimateGas`.
    ///
    /// # Parameters
    /// - `functionHash`: The hash of the function signature.
    /// - `fromAddress`: The sender's Ethereum address.
    /// - `params`: The parameters for the function call.
    ///
    /// # Returns
    /// - The estimated gas as an integer.
    /// - An error if the estimation fails.
    public function estimateGasFee(string functionHash, string fromAddress, json[] params) returns int|error {
        string functionSelector = functionHash.substring(0, 8);
        string encodedParams = self.encodeParameters(params);
        string data = functionSelector + encodedParams;

        json requestBody = {
            "jsonrpc": "2.0",
            "method": "eth_estimateGas",
            "params": [
                {"from": fromAddress, "to": self.address, "data": data}
            ],
            "id": 1
        };

        json response = check self.rpcClient->post("/", requestBody);
        map<json>|error responseMap = response.ensureType();
        if responseMap is error {
            return error("Invalid response.");
        }

        string|error result = responseMap.get("result").ensureType(string);
        if result is error {
            return error("Invalid response.");
        }

        return int:fromHexString(result);
    }

    /// Encodes the function parameters into hexadecimal format.
    ///
    /// # Parameters
    /// - `params`: The parameters to be encoded.
    ///
    /// # Returns
    /// - The encoded parameters as a string.
    function encodeParameters(json[] params) returns string {
        string encodedParams = "";
        foreach var param in params {
            string paramEncoded = "";
            if param is int {
                paramEncoded = param.toHexString().padStart(64, "0");
            } else if param is string {
                if param.startsWith("0x") && param.length() == 42 {
                    paramEncoded = param.substring(2).padStart(64, "0");
                } else {
                    paramEncoded = self.stringToHex(param).padStart(64, "0");
                }
            } else if param is boolean {
                paramEncoded = param ? "1".padStart(64, "0") : "0".padStart(64, "0");
            } else {
                io:println("Unsupported parameter type");
            }

            encodedParams += paramEncoded;
        }
        return encodedParams;
    }

    function generateFunctionSelector(string functionName, string[] parameters) returns string {

        string str = functionName + "(";

        foreach json param in parameters {
            str += param;
        }

        str += ")";

        byte[] hash = crypto:hashKeccak256(str.toBytes());

        return hash.toBase16().substring(0, 8);
    }

    /// Converts a string to a hexadecimal representation (UTF-8 encoding).
    ///
    /// # Parameters
    /// - `value`: The string to be converted.
    ///
    /// # Returns
    /// - The hexadecimal representation of the string.
    function stringToHex(string value) returns string {
        string hexString = "";
        foreach var ch in value {
            hexString += int:toHexString(ch.toCodePointInt());
        }
        return hexString;
    }
}
