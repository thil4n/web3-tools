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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AbiReader {

    private String abiPath;

    public AbiReader(String abiPath) {
        this.abiPath = abiPath;
    }

    private static final List<String> METHODS_TO_SKIP = List.of(
            "safeTransferFrom", "approve", "setApprovalForAll", "transferFrom", "renounceOwnership",
            "isApprovedForAll", "owner", "supportsInterface", "symbol", "getApproved");

    public List<AbiEntry> read() throws Exception {

        // Load ABI JSON file
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(abiPath);

        // Deserialize JSON
        ContractJson contractJson = objectMapper.readValue(file, ContractJson.class);

        AbiEntry[] abiEntries = contractJson.getAbi();

        return Arrays.stream(abiEntries)
                .filter(entry -> "function".equals(entry.getType())) // Consider only functions
                .filter(entry -> !METHODS_TO_SKIP.contains(entry.getName())) // Skip default methods
                .collect(Collectors.toList());
    }
}
