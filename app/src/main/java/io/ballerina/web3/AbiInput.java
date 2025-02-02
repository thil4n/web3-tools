package io.ballerina.web3;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AbiInput {
    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;


    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
