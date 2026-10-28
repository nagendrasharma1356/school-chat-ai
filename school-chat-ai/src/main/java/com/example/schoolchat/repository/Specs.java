package com.example.schoolchat.repository;

import com.example.schoolchat.model.SchoolClass;
import com.example.schoolchat.model.Student;
import com.example.schoolchat.model.Teacher;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Optional filters wali queries. Sab kuch parameterized hai, to SQL injection ka khatra nahi. */
public final class Specs {

    private Specs() {}

    public static Specification<Student> students(String className, String section, String name, boolean activeOnly) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (activeOnly) {
                p.add(cb.equal(cb.upper(root.<String>get("status")), "ACTIVE"));
            }
            if (has(className) || has(section)) {
                Join<Student, SchoolClass> c = root.join("schoolClass");
                if (has(className)) {
                    p.add(cb.equal(cb.lower(c.<String>get("name")), className.trim().toLowerCase()));
                }
                if (has(section)) {
                    p.add(cb.equal(cb.lower(c.<String>get("section")), section.trim().toLowerCase()));
                }
            }
            if (has(name)) {
                p.add(like(cb, root.<String>get("name"), name));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    public static Specification<Teacher> teachers(String subject, String name) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (has(subject)) {
                p.add(like(cb, root.<String>get("subject"), subject));
            }
            if (has(name)) {
                p.add(like(cb, root.<String>get("name"), name));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    private static boolean has(String s) {
        return s != null && !s.isBlank();
    }

    private static Predicate like(CriteriaBuilder cb, Expression<String> expr, String value) {
        String escaped = value.trim().toLowerCase()
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return cb.like(cb.lower(expr), "%" + escaped + "%", '\\');
    }
}
