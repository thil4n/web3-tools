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

package io.ballerina.web3.generator.utils;

import java.nio.charset.StandardCharsets;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import io.ballerina.web3.abi.AbiEntry;
import io.ballerina.web3.abi.AbiInput;

public class CodeGeneratorUtils {
    public static String hashKeccak256(String str) {
        Keccak.Digest256 digest = new Keccak.Digest256();
        byte[] hash = digest.digest(str.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    public static String generateFunctionSelector(AbiEntry abi){

        String str = abi.getName() + "(";
        
        for (AbiInput param : abi.getInputs()) {
            str += param.getType();
        }
        
        str += ")";

        String hash = hashKeccak256(str);

        return hash.substring(0, 8);
    }
}
