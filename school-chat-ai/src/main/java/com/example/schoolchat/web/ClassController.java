package com.example.schoolchat.web;

import com.example.schoolchat.dto.ClassRequest;
import com.example.schoolchat.model.SchoolClass;
import com.example.schoolchat.model.Teacher;
import com.example.schoolchat.repository.SchoolClassRepository;
import com.example.schoolchat.repository.TeacherRepository;
import com.example.schoolchat.service.SchoolQueryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
public class ClassController {

    private final SchoolClassRepository repo;
    private final TeacherRepository teachers;

    public ClassController(SchoolClassRepository repo, TeacherRepository teachers) {
        this.repo = repo;
        this.teachers = teachers;
    }

    @GetMapping
    public List<SchoolClass> all() {
        return repo.findAll().stream().sorted(SchoolQueryService.CLASS_ORDER).toList();
    }

    @GetMapping("/{id}")
    public SchoolClass one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Class " + id + " nahi mili"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SchoolClass create(@Valid @RequestBody ClassRequest req) {
        return repo.save(apply(new SchoolClass(), req));
    }

    @PutMapping("/{id}")
    public SchoolClass update(@PathVariable Long id, @Valid @RequestBody ClassRequest req) {
        return repo.save(apply(one(id), req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repo.delete(one(id));
    }

    private SchoolClass apply(SchoolClass c, ClassRequest req) {
        c.setName(req.name().trim());
        c.setSection(req.section().trim().toUpperCase());
        if (req.classTeacherId() == null) {
            c.setClassTeacher(null);
        } else {
            Teacher t = teachers.findById(req.classTeacherId())
                    .orElseThrow(() -> new NotFoundException("Teacher " + req.classTeacherId() + " nahi mila"));
            c.setClassTeacher(t);
        }
        return c;
    }
}
