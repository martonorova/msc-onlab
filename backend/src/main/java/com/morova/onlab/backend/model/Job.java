package com.morova.onlab.backend.model;


import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Job implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int input;

    @Column(nullable = true)
    private Long result;

    public Job() {}

    public Job(int input) {
        this.input = input;
    }

    public Job(Long id, int input, Long result) {
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
        return "Job{" +
                "id=" + id +
                ", input=" + input +
                ", result=" + result +
                '}';
    }
}
