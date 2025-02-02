package io.ballerina.web3.abi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AbiEntry {
    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("stateMutability")
    private String stateMutability;

    @JsonProperty("inputs")
    private List<AbiInput> inputs;

    @JsonProperty("outputs")
    private List<AbiOutput> outputs;

    @JsonProperty("constant")
    private String constant;



    public String getName() {
        return name;
    }

    public List<AbiInput> getInputs() {
        return inputs;
    }

    public List<AbiOutput> getOutputs() {
        return outputs;
    }

    public String getStateMutability() {
        return stateMutability;
    }

    public String getType() {
        return type;
    }

    public String getConstant() {
        return constant;
    }

}
