import ballerina/http;
import ballerina/io;

public function main() returns error? {
    io:println("Hello, World!");

    Web3 web3Client = check new ("http://127.0.0.1:8545", "0x5F683991AfB640eb6d9d744d74a538789481D435");


    web3Client.setSender("0xB2B06E311277cb10cD2AcfcF50CAA87b1813Aa00");
    check web3Client->/store(7);
    _ = check web3Client->/retrieve;

}

public client class Web3 {
    // The base URL of the Ethereum JSON-RPC API.
    private final string api;

    private final string address;

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
    public function setSender(string sender) {
        self.sender = sender;
    }

    resource isolated function get store(int _value) returns error? {
        // Encode function parameters
        string encodedParameters = encodeParameters([_value]);
        string callData = "0x6057361d" + encodedParameters;


        if self.sender is () {
            return error("Sender address is not set");
        }

        // Generate the JSON-RPC request body
        json requestBody = {
            "jsonrpc": "2.0",
            "method": "eth_sendTransaction",
            "params": [
                {"from": self.sender, "to": self.address, "data": callData}
            ],
            "id": 1
        };

        // Send the request and get response
        json response = check self.rpcClient->post("/", requestBody);

        io:print(response);
    }

    resource isolated function get retrieve() returns json|error {
        // Encode function parameters
        string encodedParameters = encodeParameters([]);
        string callData = "0x2e64cec1" + encodedParameters;

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
        json response = check self.rpcClient->post("/", requestBody);


        io:print(response);
        return response;
    }

        public function encodeParameters(json[] params) returns string {
        string encodedParams = "";
        foreach var param in params {
            string paramEncoded = "";
            if param is int {
                paramEncoded = param.toHexString().padStart(64, "0");
            } else if param is string {
                if param.startsWith("0x") && param.length() == 42 {
                    paramEncoded = param.substring(2).padStart(64, "0");
                } else {
                    paramEncoded = stringToHex(param).padStart(64, "0");
                }
            } else if param is boolean {
                paramEncoded = param ? "1".padStart(64, "0") : "0".padStart(64, "0");
            } else {
                paramEncoded = stringToHex(param.toString()).padStart(64, "0");
            }

            encodedParams += paramEncoded;
        }
        return encodedParams;
    }

    public function stringToHex(string value) returns string {
        string hexString = "";
        foreach var ch in value {
            hexString += int:toHexString(ch.toCodePointInt());
        }
        return hexString;
    }
}
