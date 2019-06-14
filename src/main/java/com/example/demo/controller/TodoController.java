package com.example.demo.controller;

import com.example.demo.domain.Todo;
import com.example.demo.domain.TodoRepository;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/todos")//, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@RequiredArgsConstructor
public class TodoController {

    private final TodoRepository todoRepository;

    @GetMapping
    public Iterable<Todo> findAll() {
        return todoRepository.findAll();
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<?> get(@PathVariable("id") final Integer id) {
        return ResponseEntity.of(todoRepository.findById(id));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") final Integer id) {
        if (!todoRepository.existsById(id)) {
            return ResponseEntity.badRequest().build();
        }
        todoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Todo todo) {
        final Todo createdTodo = todoRepository.save(todo);
        return ResponseEntity.status(HttpStatus.SC_CREATED).body(createdTodo);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<?> update(@PathVariable("id") final Integer id, final @RequestBody Todo todo) {
        return todoRepository.findById(id).map(t -> {
            t.setText(todo.getText());
            t.setStatus(todo.getStatus());
            return todoRepository.save(t);
        }).map(t -> ResponseEntity.status(HttpStatus.SC_ACCEPTED).body(t))
            .orElse(ResponseEntity.badRequest().build());
    }
}
