package com.masd.event.models.db;

import java.time.Instant;
import java.util.Objects;

/**
 *
 */
public class ExecutionLog {

    private String fileHash;

    private Instant executionTime;

    private Float score;

    private String label;


    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public Instant getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Instant executionTime) {
        this.executionTime = executionTime;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionLog that = (ExecutionLog) o;
        return Objects.equals(fileHash, that.fileHash) &&
                Objects.equals(executionTime, that.executionTime) &&
                Objects.equals(score, that.score) &&
                Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {

        return Objects.hash(fileHash, executionTime, score, label);
    }

}
