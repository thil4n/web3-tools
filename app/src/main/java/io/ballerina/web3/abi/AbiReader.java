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
