package com.example.schoolchat.repository;

import com.example.schoolchat.model.SchoolClass;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {

    List<SchoolClass> findByNameIgnoreCaseOrderBySection(String name);

    Optional<SchoolClass> findByNameIgnoreCaseAndSectionIgnoreCase(String name, String section);
}
