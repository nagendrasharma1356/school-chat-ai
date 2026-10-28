package com.example.schoolchat.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/** Ek class + section, jaise 10-A. Table ka naam school_class hai kyunki "class" reserved word hai. */
@Entity
@Table(name = "school_class",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "section"}))
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Class ka naam/number, jaise "10" ya "LKG" */
    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String section;

    @ManyToOne
    @JoinColumn(name = "class_teacher_id")
    private Teacher classTeacher;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
    public Teacher getClassTeacher() { return classTeacher; }
    public void setClassTeacher(Teacher classTeacher) { this.classTeacher = classTeacher; }
}
