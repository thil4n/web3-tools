package io.ballerina.web3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true) // Ignore all other properties
public class ContractJson {
    @JsonProperty("abi")
    private AbiEntry[] abi;
}
