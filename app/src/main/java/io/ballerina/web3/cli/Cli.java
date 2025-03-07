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
// TODO: io.ballerina.lib.web3
package io.ballerina.web3.cli;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.web3.abi.AbiEntry;
import io.ballerina.web3.abi.AbiReader;
import io.ballerina.web3.generator.Generator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Command(name = "web3", description = "Generates Ballerina connectors from Ethereum Smart Contract ABI.")
public class Cli implements BLauncherCmd {

    private static final String CMD_NAME = "web3";

    @Option(names = { "-a", "--abi" }, required = true, description = "Path to the ABI JSON file")
    private String abiPath;

    @Option(names = { "-o", "--output" }, description = "Output directory")
    private String outputDir = "./generated/";

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "Display help information")
    private boolean helpFlag;

    @Override
    public void execute() {
        if (helpFlag) {
            printUsage(new StringBuilder());
            return;
        }

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
        AbiReader abiReader = new AbiReader(abiPath);
        List<AbiEntry> abiEntries = abiReader.read();
        Generator.generate(abiEntries, outputDir);
    }

    private boolean isFileValid(String filePath) {
        Path path = Path.of(filePath);
        return Files.exists(path) && Files.isRegularFile(path) && Files.isReadable(path);
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Generates Ballerina connectors from Ethereum Smart Contract ABI.\n")
                .append("This tool takes an ABI JSON file and generates a Ballerina connector.\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("bal web3 -a <path/to/abi.json> [-o <output-dir>]\n");
    }

    @Override
    public void setParentCmdParser(picocli.CommandLine parentCmdParser) {
        // Not needed for now
    }
}
