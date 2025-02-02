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

package io.ballerina.web3.abi;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;


public class AbiReader {
    public static void main(String[] args) {
        try {
            // Load ABI JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File("src/main/resources/SimpleStorage.json");

            // Deserialize JSON
            ContractJson contractJson = objectMapper.readValue(file,ContractJson.class);

            AbiEntry[] abiEntries = contractJson.getAbi();

            // Print parsed ABI details
            for (AbiEntry entry : abiEntries) {
                System.out.println("Function: " + entry.getName());
                System.out.println("Type: " + entry.getType());
                System.out.println("State Mutability: " + entry.getStateMutability());

                if (entry.getInputs() != null) {
                    for (AbiInput input : entry.getInputs()) {
                        System.out.println("Input: " + input.getName() + " (" + input.getType() + ")");
                    }
                }

                if (entry.getOutputs() != null) {
                    for (AbiOutput output : entry.getOutputs()) {
                        System.out.println("Output: " + output.getType());
                    }
                }

                System.out.println("----------------------");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
