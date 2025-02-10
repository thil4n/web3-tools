import ballerina/http;

public client class Web3 {
    // The base URL of the Ethereum JSON-RPC API.
    private final string api;

    // The contract address.
    private final string address;

    // The address of the sender.
    private string|() sender = ();

    // HTTP client to send JSON-RPC requests to the Ethereum node.
    private final http:Client rpcClient;

    //Initialize the client
    public function init(string api, string address) returns error? {
        self.api = api;

        self.address = address;

        // Create a client configuration to disable HTTP/2 upgrades
        http:ClientConfiguration clientConfig = {
            httpVersion: http:HTTP_1_1
        };

        // Initialize the HTTP client with the given API URL and configuration
        self.rpcClient = check new (self.api, clientConfig);
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
        json response = check self.httpClient->post("/", requestBody);
        if response is json {
            return response;
        } else {
            return error("Blockchain call failed");
        }

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
        json response = check self.httpClient->post("/", requestBody);
        if response is json {
            return response;
        } else {
            return error("Blockchain call failed");
        }

    }
}
