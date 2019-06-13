package com.example.demo.domain;

import javax.persistence.Entity;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
public class Todo extends AbstractPersistable<Integer> {

    private String text;
    private TodoStatus status;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public TodoStatus getStatus() {
        return status;
    }

    public void setStatus(TodoStatus status) {
        this.status = status;
    }
}
