import ballerina/http;

public client class Web3 {
    // The base URL of the Ethereum JSON-RPC API.
    private final string api;

    // The contract address.
    private string address;

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

    resource isolated function post approve(string to, decimal tokenId) returns error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([to, tokenId]);
        string callData = "0x" + "81c81e27" + encodedParameters;

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

    resource isolated function post balanceOf(string owner) returns decimal|error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([owner]);
        string callData = "0x" + "70a08231" + encodedParameters;

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

        decimal|error result = check hexToDecimal(response.result.substring(2));
        return result;
    }

    resource isolated function post getApproved(decimal tokenId) returns string|error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([tokenId]);
        string callData = "0x" + "081812fc" + encodedParameters;

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

        string result = "0x" + response.result.substring(26);
        return result;
    }

    resource isolated function post isApprovedForAll(string owner, string operator) returns boolean|error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([owner, operator]);
        string callData = "0x" + "0936ab2e" + encodedParameters;

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

        boolean result = response.result == "0x1";
        return result;
    }

    resource isolated function post name() returns string|error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([]);
        string callData = "0x" + "06fdde03" + encodedParameters;

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

        string result = response.result.substring(2);
        return result;
    }

    resource isolated function post owner() returns string|error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([]);
        string callData = "0x" + "8da5cb5b" + encodedParameters;

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

        string result = "0x" + response.result.substring(26);
        return result;
    }

    resource isolated function post ownerOf(decimal tokenId) returns string|error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([tokenId]);
        string callData = "0x" + "6352211e" + encodedParameters;

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

        string result = "0x" + response.result.substring(26);
        return result;
    }

    resource isolated function post renounceOwnership() returns error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([]);
        string callData = "0x" + "715018a6" + encodedParameters;

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

    resource isolated function post safeTransferFrom(string from_param, string to, decimal tokenId) returns error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([from , to, tokenId]                          );
        string callData = "0x" + "1bca66ec" + encodedParameters;

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

    resource isolated function post safeTransferFrom(string from_param, string to, decimal tokenId, string data) returns error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([from , to, tokenId, data]                          );
        string callData = "0x" + "fd43c5ac" + encodedParameters;

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

    resource isolated function post setApprovalForAll(string operator, boolean approved) returns error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([operator, approved]);
        string callData = "0x" + "7122a46a" + encodedParameters;

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

    resource isolated function post supportsInterface(anydata interfaceId) returns boolean|error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([interfaceId]);
        string callData = "0x" + "01ffc9a7" + encodedParameters;

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

        boolean result = response.result == "0x1";
        return result;
    }

    resource isolated function post symbol() returns string|error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([]);
        string callData = "0x" + "95d89b41" + encodedParameters;

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

        string result = response.result.substring(2);
        return result;
    }

    resource isolated function post tokenCounter() returns decimal|error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([]);
        string callData = "0x" + "d082e381" + encodedParameters;

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

        decimal|error result = check hexToDecimal(response.result.substring(2));
        return result;
    }

    resource isolated function post tokenIdToListed(decimal param0) returns boolean|error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([]);
        string callData = "0x" + "9c402c26" + encodedParameters;

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

        boolean result = response.result == "0x1";
        return result;
    }

    resource isolated function post tokenIdToPrice(decimal param0) returns decimal|error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([]);
        string callData = "0x" + "f4812eb9" + encodedParameters;

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

        decimal|error result = check hexToDecimal(response.result.substring(2));
        return result;
    }

    resource isolated function post tokenURI(decimal tokenId) returns string|error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([tokenId]);
        string callData = "0x" + "c87b56dd" + encodedParameters;

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

        string result = response.result.substring(2);
        return result;
    }

    resource isolated function post transferFrom(string from_param, string to, decimal tokenId) returns error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([from , to, tokenId]                          );
        string callData = "0x" + "944179b4" + encodedParameters;

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

    resource isolated function post transferOwnership(string newOwner) returns error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([newOwner]);
        string callData = "0x" + "f2fde38b" + encodedParameters;

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

    resource isolated function post mintNFT(string tokenURI, decimal price) returns error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([tokenURI, price]);
        string callData = "0x" + "cae22ac6" + encodedParameters;

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

    resource isolated function post buyNFT(decimal tokenId) returns error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([tokenId]);
        string callData = "0x" + "51ed8288" + encodedParameters;

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

    resource isolated function post getListedNFTs() returns anydata[]|error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([]);
        string callData = "0x" + "012386f0" + encodedParameters;

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

        // Unsupported type: tuple[]
        return result;
    }
}
