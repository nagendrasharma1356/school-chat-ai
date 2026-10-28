package com.example.schoolchat.web;

import com.example.schoolchat.model.Teacher;
import com.example.schoolchat.repository.TeacherRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherRepository repo;

    public TeacherController(TeacherRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Teacher> all() {
        return repo.findAll(Sort.by("name"));
    }

    @GetMapping("/{id}")
    public Teacher one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Teacher " + id + " nahi mila"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Teacher create(@Valid @RequestBody Teacher t) {
        t.setId(null);
        return repo.save(t);
    }

    @PutMapping("/{id}")
    public Teacher update(@PathVariable Long id, @Valid @RequestBody Teacher t) {
        Teacher existing = one(id);
        existing.setName(t.getName());
        existing.setEmail(t.getEmail());
        existing.setPhone(t.getPhone());
        existing.setSubject(t.getSubject());
        return repo.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repo.delete(one(id));
    }
}
