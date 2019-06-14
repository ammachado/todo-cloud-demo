package com.example.demo.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.domain.Todo;
import com.example.demo.domain.TodoRepository;
import com.example.demo.domain.TodoStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.junit.VerificationCollector;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = TodoController.class)
public class TodoControllerTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Rule
    public final VerificationCollector collector = MockitoJUnit.collector();

    @MockBean
    private TodoRepository todoRepository;

    @Captor
    private ArgumentCaptor<Todo> todoArgumentCaptor;

    @Autowired
    private MockMvc mockMvc;

    private JacksonTester<Todo> jacksonTester;

    @Before
    public void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonTester.initFields(this, objectMapper);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(todoRepository);
        collector.collectAndReport();
    }

    @Test
    public void findAll_success() throws Exception {
        when(todoRepository.findAll()).thenReturn(Arrays.asList(create("First", TodoStatus.COMPLETED), create("Second", TodoStatus.PENDING)));

        mockMvc.perform(get("/todos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].text").value("First"))
            .andExpect(jsonPath("$[0].status").value(TodoStatus.COMPLETED.toString()))
            .andExpect(jsonPath("$[1].text").value("Second"))
            .andExpect(jsonPath("$[1].status").value(TodoStatus.PENDING.toString()));

        verify(todoRepository).findAll();
    }

    @Test
    public void get_valid_id() throws Exception {
        final Todo todo = create("Todo", TodoStatus.COMPLETED);
        when(todoRepository.findById(1)).thenReturn(Optional.of(todo));

        mockMvc.perform(get("/todos/{id}", 1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.text").value(todo.getText()))
            .andExpect(jsonPath("$.status").value(todo.getStatus().toString()));

        verify(todoRepository).findById(1);
    }

    @Test
    public void get_invalid_id() throws Exception {
        when(todoRepository.findById(1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/todos/{id}", 1))
            .andExpect(status().isNotFound());

        verify(todoRepository).findById(1);
    }

    @Test
    public void delete_valid_id() throws Exception {
        when(todoRepository.existsById(1)).thenReturn(true);
        doNothing().when(todoRepository).deleteById(1);

        mockMvc.perform(delete("/todos/{id}", 1))
            .andExpect(status().isOk());

        verify(todoRepository).existsById(1);
        verify(todoRepository).deleteById(1);
    }

    @Test
    public void delete_invalid_id() throws Exception {
        when(todoRepository.existsById(1)).thenReturn(false);

        mockMvc.perform(delete("/todos/{id}", 1))
            .andExpect(status().isBadRequest());

        verify(todoRepository).existsById(1);
    }

    @Test
    public void create() throws Exception {
        final Todo todo = create("Todo", TodoStatus.PENDING);
        final Todo createdTodo = createWithId(todo.getText(), todo.getStatus());
        when(todoRepository.save(todoArgumentCaptor.capture())).thenReturn(createdTodo);

        final MockHttpServletRequestBuilder requestBuilder = post("/todos")
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(jacksonTester.write(todo).getJson());

        mockMvc.perform(requestBuilder)
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(createdTodo.getId()))
            .andExpect(jsonPath("$.text").value(createdTodo.getText()))
            .andExpect(jsonPath("$.status").value(createdTodo.getStatus().toString()));

        final Todo captured = todoArgumentCaptor.getValue();
        verify(todoRepository).save(captured);
        assertEquals(captured.getText(), createdTodo.getText());
        assertEquals(captured.getStatus(), createdTodo.getStatus());
    }

    @Test
    public void update_invalid_id() throws Exception {
        when(todoRepository.findById(1)).thenReturn(Optional.empty());

        final MockHttpServletRequestBuilder requestBuilder = put("/todos/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content("{ \"status\": \"COMPLETED\" }");

        mockMvc.perform(requestBuilder).andExpect(status().isBadRequest());

        verify(todoRepository).findById(1);
    }

    @Test
    public void update_status() throws Exception {
        final Todo existingTodo = createWithId("Todo", TodoStatus.PENDING);
        final Todo updatedTodo = createWithId(existingTodo.getText(), TodoStatus.COMPLETED);
        testUpdate(existingTodo, updatedTodo, "{ \"status\": \"COMPLETED\" }");
    }

    @Test
    public void update_text() throws Exception {
        final Todo existingTodo = createWithId("Todo", TodoStatus.PENDING);
        final Todo updatedTodo = createWithId("New Todo", TodoStatus.PENDING);
        testUpdate(existingTodo, updatedTodo, "{ \"text\": \"New Todo\" }");
    }

    private void testUpdate(Todo existingTodo, Todo updatedTodo, String content) throws Exception {
        when(todoRepository.findById(existingTodo.getId())).thenReturn(Optional.of(existingTodo));
        when(todoRepository.save(todoArgumentCaptor.capture())).thenReturn(updatedTodo);

        final MockHttpServletRequestBuilder requestBuilder = put("/todos/{id}", 1)
            .contentType(MediaType.APPLICATION_JSON_UTF8)
            .content(content);

        mockMvc.perform(requestBuilder)
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.id").value(updatedTodo.getId()))
            .andExpect(jsonPath("$.text").value(updatedTodo.getText()))
            .andExpect(jsonPath("$.status").value(updatedTodo.getStatus().toString()));

        final Todo captured = todoArgumentCaptor.getValue();
        verify(todoRepository).findById(1);
        verify(todoRepository).save(captured);
        assertEquals(captured.getId(), existingTodo.getId());
        assertEquals(captured.getText(), existingTodo.getText());
        assertEquals(captured.getStatus(), existingTodo.getStatus());
    }

    private static Todo create(@NotNull String text, @NotNull TodoStatus status) {
        return new Todo(text, status);
    }

    private static Todo createWithId(@NotNull String text, @NotNull TodoStatus status) {
        return new CustomTodo(1, text, status);
    }

    private static class CustomTodo extends Todo {

        CustomTodo(int id, String text, TodoStatus status) {
            super(text, status);
            setId(id);
        }

        @Override
        public void setId(Integer id) {
            super.setId(id);
        }
    }
}
