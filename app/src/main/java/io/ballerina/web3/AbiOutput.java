package io.ballerina.web3;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AbiOutput {
    @JsonProperty("type")
    private String type;


    public String getType() {
        return type;
    }
}
