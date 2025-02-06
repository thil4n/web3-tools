package io.ballerina.web3.generator.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileUtils {
    /**
     * Writes content to a file at the given path.
     * If the file exists, it will be overwritten.
     */
    public static void writeToFile(String path, String content) {
        try {
            Path filePath = Path.of(path);

            // Ensure parent directories exist
            Files.createDirectories(filePath.getParent());

            // Write content to the file (overwrite if exists)
            Files.writeString(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Successfully wrote to file: " + path);
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
