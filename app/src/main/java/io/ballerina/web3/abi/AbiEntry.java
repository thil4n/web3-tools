/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.web3.abi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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
