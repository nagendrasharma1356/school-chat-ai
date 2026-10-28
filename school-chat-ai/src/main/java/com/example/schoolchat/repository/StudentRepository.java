package com.example.schoolchat.repository;

import com.example.schoolchat.model.Student;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    @Query("""
            select c.section, count(s)
            from Student s join s.schoolClass c
            where lower(c.name) = lower(:className)
              and upper(s.status) = 'ACTIVE'
            group by c.section
            order by c.section
            """)
    List<Object[]> countSectionWise(@Param("className") String className);

    @Query("""
            select c.id, count(s)
            from Student s join s.schoolClass c
            where upper(s.status) = 'ACTIVE'
            group by c.id
            """)
    List<Object[]> countPerClass();
}
