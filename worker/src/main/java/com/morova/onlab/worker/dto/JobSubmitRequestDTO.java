package com.morova.onlab.worker.dto;

import java.io.Serializable;

public class JobSubmitRequestDTO implements Serializable {
    private Long id;
    private int input;
    private Long result;

    public JobSubmitRequestDTO() {}

    public JobSubmitRequestDTO(Long id, int input, Long result) {
        this.id = id;
        this.input = input;
        this.result = result;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getInput() {
        return input;
    }

    public void setInput(int input) {
        this.input = input;
    }

    public Long getResult() {
        return result;
    }

    public void setResult(Long result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "JobSubmitRequestDTO{" +
                "id=" + id +
                ", input=" + input +
                ", result=" + result +
                '}';
    }
}
