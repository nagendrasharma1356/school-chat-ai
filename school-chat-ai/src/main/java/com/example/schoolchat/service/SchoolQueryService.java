package com.example.schoolchat.service;

import com.example.schoolchat.model.SchoolClass;
import com.example.schoolchat.model.Student;
import com.example.schoolchat.model.Teacher;
import com.example.schoolchat.repository.SchoolClassRepository;
import com.example.schoolchat.repository.Specs;
import com.example.schoolchat.repository.StudentRepository;
import com.example.schoolchat.repository.TeacherRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Chat tools aur REST list endpoints dono yahin se data padhte hain. */
@Service
@Transactional(readOnly = true)
public class SchoolQueryService {

    private final StudentRepository students;
    private final TeacherRepository teachers;
    private final SchoolClassRepository classes;

    public SchoolQueryService(StudentRepository students, TeacherRepository teachers, SchoolClassRepository classes) {
        this.students = students;
        this.teachers = teachers;
        this.classes = classes;
    }

    /** Sirf ACTIVE students ginta hai. className/section null ho to us filter ko ignore karta hai. */
    public long countStudents(String className, String section) {
        return students.count(Specs.students(className, section, null, true));
    }

    /** [{section, count}] sirf ACTIVE students. */
    public List<Map<String, Object>> sectionWise(String className) {
        return students.countSectionWise(className).stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("section", r[0]);
            m.put("count", ((Number) r[1]).longValue());
            return m;
        }).toList();
    }

    public Page<Student> listStudents(String className, String section, String name, boolean activeOnly, int limit) {
        return students.findAll(Specs.students(className, section, name, activeOnly),
                PageRequest.of(0, limit, Sort.by("schoolClass.name", "schoolClass.section", "rollNo", "name")));
    }

    public List<Teacher> listTeachers(String subject, String name) {
        return teachers.findAll(Specs.teachers(subject, name), Sort.by("name"));
    }

    /** 9, 10, 11 ... aur phir LKG jaise naam: pehle chhota naam (lambai se), phir alphabet se. */
    public static final Comparator<SchoolClass> CLASS_ORDER = Comparator
            .comparingInt((SchoolClass c) -> c.getName().length())
            .thenComparing(SchoolClass::getName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(SchoolClass::getSection, String.CASE_INSENSITIVE_ORDER);

    public List<SchoolClass> listClasses() {
        return classes.findAll().stream().sorted(CLASS_ORDER).toList();
    }

    /** classId -> ACTIVE students ki ginti */
    public Map<Long, Long> activeCountPerClass() {
        Map<Long, Long> out = new HashMap<>();
        for (Object[] r : students.countPerClass()) {
            out.put(((Number) r[0]).longValue(), ((Number) r[1]).longValue());
        }
        return out;
    }

    public Optional<SchoolClass> findClass(String name, String section) {
        return classes.findByNameIgnoreCaseAndSectionIgnoreCase(name, section);
    }

    public List<SchoolClass> findClassesByName(String name) {
        return classes.findByNameIgnoreCaseOrderBySection(name);
    }
}
