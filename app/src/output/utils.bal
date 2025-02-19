
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

public isolated function encodeParameters(json[] params) returns string {
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

public isolated function stringToHex(string value) returns string {
    string hexString = "";
    foreach var ch in value {
        hexString += int:toHexString(ch.toCodePointInt());
    }
    return hexString;
}

isolated function pow(decimal base, int exponent) returns decimal {
    decimal value = 1;
    foreach int i in 1 ... exponent {
        value = value * base;
    }
    return value;
}

# Description.
#
# + str - parameter description
# + return - return value description
public isolated function hexToDecimal(string str) returns decimal|error {

    // Initialize the result
    decimal decimalValue = 0;
    int length = str.length();

    string hexString = str.toUpperAscii();

    if !hexString.matches(re `^[A-F0-9]+$`) {
        return error("Invalid hex string: Contains non-hexadecimal characters");
    }

    foreach int i in 0 ..< length {
        string hexChar = hexString[i];

        map<int> values = {
            "0": 0,
            "1": 1,
            "2": 2,
            "3": 3,
            "4": 4,
            "5": 5,
            "6": 6,
            "7": 7,
            "8": 8,
            "9": 9,
            "A": 10,
            "B": 11,
            "C": 12,
            "D": 13,
            "E": 14,
            "F": 15
        };

        int position = (length - i) - 1;
        decimal power = pow(16, position);
        int value = values[hexChar] ?: 0;

        decimalValue += value * power;
    }

    return decimalValue;
}
