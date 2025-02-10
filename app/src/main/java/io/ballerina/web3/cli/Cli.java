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

package io.ballerina.web3.cli;

import io.ballerina.web3.abi.AbiEntry;
import io.ballerina.web3.abi.AbiReader;
import io.ballerina.web3.generator.Generator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;

@Command(name = "web3-cli", mixinStandardHelpOptions = true, version = "1.0",
        description = "Generates Ballerina connectors from Ethereum Smart Contract ABI.")
public class Cli implements Runnable {

    @Option(names = {"-a", "--abi"}, required = true, description = "Path to the ABI JSON file")
    private String abiPath;

    @Option(names = {"-o", "--output"}, description = "Output directory")
    private String outputDir = "./generated/";

    @Override
    public void run() {
        // Validate ABI file before processing
        if (!isFileValid(abiPath)) {
            System.err.println("Error: ABI file does not exist or is not a valid file: " + abiPath);
            return;
        }

        System.out.println("ABI File: " + abiPath);
        System.out.println("Output Directory: " + outputDir);

        try {
            generateBallerinaConnector();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateBallerinaConnector() throws Exception {
        System.out.println("Generating Ballerina connector...");

        AbiReader abiReader = new AbiReader(abiPath);

        AbiEntry[] abiEntries = abiReader.read();

        Generator.generate(abiEntries);
    }

    private boolean isFileValid(String filePath) {
        Path path = Path.of(filePath);
        return Files.exists(path) && Files.isRegularFile(path) && Files.isReadable(path);
    }
}
