package app.src.main.java.io.ballerina.web3;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


class HelloWorld {
    private static void readAbi(){
        File abi = new File("SimpleStorage.json");

        try {
            Scanner abiReader = new Scanner(abi);

            for (int i = 0; i < 10; i++) {

                String data = abiReader.nextLine();
                System.out.println(data);
                
            }

            abiReader.close();
        } catch (FileNotFoundException  e) {
            System.out.println("File not found!");
        }


    }
    public static void main(String[] args){
        readAbi();
        System.out.println("hello world");
    }
}