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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CodeGeneratorUtils {


    private String  hashKeccak256(String str) throws NoSuchAlgorithmException{
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256"); 

            byte[] hash =  messageDigest.digest(str.getBytes());
            return hash.toString();
    }


    public String generateFunctionSelector(String functionName, String[] parameters) throws NoSuchAlgorithmException{

        String str = functionName + "(";
        
        for (String param : parameters) {
            str += param;
        }
        
        str += ")";

        String hash = this.hashKeccak256(str);
        return hash.substring(0, 8);
    }
}
