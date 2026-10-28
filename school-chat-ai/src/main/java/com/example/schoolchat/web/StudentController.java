package com.example.schoolchat.web;

import com.example.schoolchat.dto.StudentRequest;
import com.example.schoolchat.model.SchoolClass;
import com.example.schoolchat.model.Student;
import com.example.schoolchat.repository.SchoolClassRepository;
import com.example.schoolchat.repository.StudentRepository;
import com.example.schoolchat.service.SchoolQueryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private static final int LIST_LIMIT = 200;

    private final StudentRepository repo;
    private final SchoolClassRepository classes;
    private final SchoolQueryService query;

    public StudentController(StudentRepository repo, SchoolClassRepository classes, SchoolQueryService query) {
        this.repo = repo;
        this.classes = classes;
        this.query = query;
    }

    /** GET /api/students?className=10&section=A&name=aar  (sab optional, INACTIVE bhi dikhte hain) */
    @GetMapping
    public List<Student> all(@RequestParam(required = false) String className,
                             @RequestParam(required = false) String section,
                             @RequestParam(required = false) String name) {
        return query.listStudents(className, section, name, false, LIST_LIMIT).getContent();
    }

    @GetMapping("/{id}")
    public Student one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Student " + id + " nahi mila"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Student create(@Valid @RequestBody StudentRequest req) {
        return repo.save(apply(new Student(), req));
    }

    @PutMapping("/{id}")
    public Student update(@PathVariable Long id, @Valid @RequestBody StudentRequest req) {
        return repo.save(apply(one(id), req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repo.delete(one(id));
    }

    private Student apply(Student s, StudentRequest req) {
        SchoolClass c = classes.findById(req.classId())
                .orElseThrow(() -> new NotFoundException("Class " + req.classId() + " nahi mili"));
        s.setName(req.name().trim());
        s.setRollNo(req.rollNo());
        s.setGender(req.gender());
        s.setGuardianName(req.guardianName());
        s.setPhone(req.phone());
        s.setStatus(req.status() == null || req.status().isBlank() ? "ACTIVE" : req.status().trim().toUpperCase());
        s.setSchoolClass(c);
        return s;
    }
}
