package io.ballerina.web3.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "web3-cli", mixinStandardHelpOptions = true, version = "1.0",
        description = "Generates Ballerina connectors from Ethereum Smart Contract ABI.")
public class Cli implements Runnable {

    @Option(names = {"-a", "--abi"}, required = true, description = "Path to the ABI JSON file")
    private String abiPath;

    @Option(names = {"-o", "--output"}, description = "Output directory")
    private String outputDir = "./generated/";

    @Override
    public void run() {
        System.out.println("📂 ABI File: " + abiPath);
        System.out.println("📂 Output Directory: " + outputDir);
        generateBallerinaConnector();
    }

    private void generateBallerinaConnector() {
        System.out.println("🚀 Generating Ballerina connector...");
    }
}
