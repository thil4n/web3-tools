package io.ballerina.web3.abi;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AbiInput {
    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("internalType")
    private String internalType;


    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getInternalType() {
        return internalType;
    }
}
