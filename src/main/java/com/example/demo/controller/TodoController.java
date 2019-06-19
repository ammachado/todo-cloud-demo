package com.example.demo.controller;

import com.example.demo.domain.Todo;
import com.example.demo.domain.TodoRepository;
import io.micrometer.core.annotation.Timed;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Timed("TODO_ENDPOINT")
@RestController
@RequestMapping(value = "/todos")
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
            log.info("GET method failed for TODO with id {}", id);
            return ResponseEntity.badRequest().build();
        }
        todoRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Todo todo) {
        final Todo createdTodo = todoRepository.save(todo);
        log.info("TODO created: {}", todo);
        return ResponseEntity.status(HttpStatus.SC_CREATED).body(createdTodo);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<?> update(@PathVariable("id") final Integer id, final @RequestBody Todo todo) {
        return todoRepository.findById(id).map(t -> {
            log.info("Updating TODO with id {}", id);
            t.setText(todo.getText());
            t.setStatus(todo.getStatus());
            return todoRepository.save(t);
        }).map(t -> ResponseEntity.status(HttpStatus.SC_ACCEPTED).body(t))
            .orElseGet(() -> {
                log.info("TODO with id {} not found for update", id);
                return ResponseEntity.badRequest().build();
            });
    }
}
