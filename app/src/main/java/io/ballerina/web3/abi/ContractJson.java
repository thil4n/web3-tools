package io.ballerina.web3.abi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true) // Ignore all other properties
public class ContractJson {
    @JsonProperty("abi")
    private AbiEntry[] abi;


    public AbiEntry[] getAbi() {
        return abi;
    }
}
