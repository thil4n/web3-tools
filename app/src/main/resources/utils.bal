
// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

# Encodes an array of parameters into ABI-compatible hex format.
# Each parameter is padded to 32 bytes (64 hex characters).
#
# + params - Array of parameters to encode
# + return - Hex-encoded parameter string
public isolated function encodeParameters(json[] params) returns string {
    string encodedParams = "";
    foreach var param in params {
        string paramEncoded = "";
        if param is int {
            paramEncoded = param.toHexString().padStart(64, "0");
        } else if param is string {
            if param.startsWith("0x") && param.length() == 42 {
                // Ethereum address: strip 0x prefix and left-pad to 32 bytes
                paramEncoded = param.substring(2).padStart(64, "0");
            } else {
                paramEncoded = stringToHex(param).padStart(64, "0");
            }
        } else if param is boolean {
            paramEncoded = param ? "1".padStart(64, "0") : "0".padStart(64, "0");
        } else if param is byte[] {
            string hexStr = "";
            foreach byte b in param {
                hexStr += int:toHexString(b).padStart(2, "0");
            }
            paramEncoded = hexStr.padEnd(64, "0");
        } else {
            paramEncoded = stringToHex(param.toString()).padStart(64, "0");
        }

        encodedParams += paramEncoded;
    }
    return encodedParams;
}

# Converts a string value to its hexadecimal representation.
#
# + value - The string to convert
# + return - Hex-encoded string
public isolated function stringToHex(string value) returns string {
    string hexString = "";
    foreach var ch in value {
        hexString += int:toHexString(ch.toCodePointInt());
    }
    return hexString;
}

# Converts a hexadecimal string to an integer.
# Strips leading zeros and handles empty input.
#
# + str - Hex string (without 0x prefix)
# + return - The integer value or an error
public isolated function hexToDecimal(string str) returns int|error {
    if str.length() == 0 {
        return 0;
    }

    string hexString = str.toUpperAscii();

    if !hexString.matches(re `^[A-F0-9]+$`) {
        return error("Invalid hex string: Contains non-hexadecimal characters");
    }

    int result = 0;
    foreach int i in 0 ..< hexString.length() {
        string hexChar = hexString[i];

        map<int> values = {
            "0": 0, "1": 1, "2": 2, "3": 3,
            "4": 4, "5": 5, "6": 6, "7": 7,
            "8": 8, "9": 9, "A": 10, "B": 11,
            "C": 12, "D": 13, "E": 14, "F": 15
        };

        int digitValue = values[hexChar] ?: 0;
        result = result * 16 + digitValue;
    }

    return result;
}
