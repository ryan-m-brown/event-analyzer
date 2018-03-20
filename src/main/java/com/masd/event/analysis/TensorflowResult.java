package com.masd.event.analysis;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 */
public class TensorflowResult {

    @JsonProperty
    private final String label;
    @JsonProperty
    private final Float score;

    public TensorflowResult(String label, Float score) {
        this.label = label;
        this.score = score;
    }

    public String getLabel() {
        return label;
    }

    public Float getScore() {
        return score;
    }
}
