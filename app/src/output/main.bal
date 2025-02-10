import ballerina/http;

public client class Web3 {
    // The base URL of the Ethereum JSON-RPC API.
    private final string api;

    // The contract address.
    private  string address;

    // The address of the sender.
    private string|() sender = ();

    // HTTP client to send JSON-RPC requests to the Ethereum node.
    private final http:Client rpcClient;

    /// Initialize the Ethereum client.
    public function init(string api, string address) returns error? {
        self.api = api;
        self.address = address;

        http:ClientConfiguration clientConfig = {
            httpVersion: http:HTTP_1_1
        };

        self.rpcClient = check new (self.api, clientConfig);
    }

    /// Set the contract address.
    public function setContractAddress(string address) {
        self.address = address;
    }

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

        record {string[] result;} response = check self.rpcClient->post("/", requestBody);
        return response.result;
    }

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

        record {string result;} response = check self.rpcClient->post("/", requestBody);

        string sanitizedHex = response.result.substring(2);
        return check hexToDecimal(sanitizedHex);
    }

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

        record {string result;} response = check self.rpcClient->post("/", requestBody);

        string sanitizedHex = response.result.substring(2);
        return check hexToDecimal(sanitizedHex);
    }

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

        record {string result;} response = check self.rpcClient->post("/", requestBody);

        string sanitizedHex = response.result.substring(2);
        return check hexToDecimal(sanitizedHex);
    }

    public function weiToEther(decimal weiAmount) returns decimal {
        decimal etherValue = weiAmount / 1e18;
        return etherValue;
    }

    public function ethToWei(decimal etherValue) returns decimal {
        decimal weiAmount = etherValue * 1e18;
        return weiAmount;
    }

    resource isolated function post store(int _value) returns error {
        // Encode function parameters
        string encodedParameters = encodeParameters([_value]);
        string callData = "0x" + "6057361d" + encodedParameters;

        // Generate the JSON-RPC request body
        json requestBody = {
            "jsonrpc": "2.0",
            "method": "eth_call",
            "params": [
                {"to": self.address, "data": callData},
                "latest"
            ],
            "id": 1
        };

        // Send the request and get response
        record {string result;} response = check self.rpcClient->post("/", requestBody);

    }

    resource isolated function post retrieve() returns int|error {
        // Encode function parameters
        string encodedParameters = encodeParameters([]);
        string callData = "0x" + "2e64cec1" + encodedParameters;

        // Generate the JSON-RPC request body
        json requestBody = {
            "jsonrpc": "2.0",
            "method": "eth_call",
            "params": [
                {"to": self.address, "data": callData},
                "latest"
            ],
            "id": 1
        };

        // Send the request and get response
        record {string result;} response = check self.rpcClient->post("/", requestBody);

        int|error result = check hexToDecimal(response.result.substring(2));
        return result
    }
}
