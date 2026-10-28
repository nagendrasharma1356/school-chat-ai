package com.example.schoolchat.config;

import com.example.schoolchat.model.SchoolClass;
import com.example.schoolchat.model.Student;
import com.example.schoolchat.model.Teacher;
import com.example.schoolchat.repository.SchoolClassRepository;
import com.example.schoolchat.repository.StudentRepository;
import com.example.schoolchat.repository.TeacherRepository;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String[] BOYS = {"Aarav", "Vivaan", "Arjun", "Kabir", "Rohan", "Ishaan", "Dev", "Yash",
            "Aditya", "Karan", "Neel", "Rahul", "Sahil", "Harsh", "Manav", "Pranav", "Nikhil", "Varun", "Om", "Tanmay"};
    private static final String[] GIRLS = {"Diya", "Anaya", "Myra", "Saanvi", "Riya", "Kiara", "Isha", "Pooja",
            "Aditi", "Neha", "Tara", "Priya", "Sneha", "Kavya", "Meera", "Ananya", "Nisha", "Simran", "Ira", "Tanvi"};
    private static final String[] SURNAMES = {"Sharma", "Verma", "Gupta", "Singh", "Patel", "Reddy", "Nair", "Iyer",
            "Khan", "Joshi"};

    private final TeacherRepository teachers;
    private final SchoolClassRepository classes;
    private final StudentRepository students;

    public DataSeeder(TeacherRepository teachers, SchoolClassRepository classes, StudentRepository students) {
        this.teachers = teachers;
        this.classes = classes;
        this.students = students;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (teachers.count() > 0 || classes.count() > 0 || students.count() > 0) {
            return;
        }

        Teacher anita = teacher("Anita Sharma", "Mathematics", "anita.sharma@school.test", "9810000001");
        Teacher rajesh = teacher("Rajesh Verma", "Science", "rajesh.verma@school.test", "9810000002");
        Teacher sunita = teacher("Sunita Rao", "English", "sunita.rao@school.test", "9810000003");
        Teacher vikram = teacher("Vikram Singh", "Hindi", "vikram.singh@school.test", "9810000004");
        Teacher meena = teacher("Meena Iyer", "Social Science", "meena.iyer@school.test", "9810000005");
        teacher("Arjun Nair", "Computer Science", "arjun.nair@school.test", "9810000006");

        List<SchoolClass> cls = List.of(
                clazz("9", "A", anita), clazz("9", "B", rajesh), clazz("10", "A", sunita),
                clazz("10", "B", vikram), clazz("11", "A", meena));

        List<Student> all = new ArrayList<>();
        int n = 0;
        for (SchoolClass c : cls) {
            for (int roll = 1; roll <= 8; roll++, n++) {
                boolean male = n % 2 == 0;
                String first = male ? BOYS[(n / 2) % BOYS.length] : GIRLS[(n / 2) % GIRLS.length];
                String last = SURNAMES[(n * 3 + 1) % SURNAMES.length];
                Student s = new Student();
                s.setName(first + " " + last);
                s.setRollNo(roll);
                s.setGender(male ? "Male" : "Female");
                s.setGuardianName("Mr. " + last);
                s.setPhone("9876" + String.format("%06d", 100000 + n * 37));
                s.setStatus(n % 13 == 12 ? "INACTIVE" : "ACTIVE");
                s.setSchoolClass(c);
                all.add(s);
            }
        }
        students.saveAll(all);
        log.info("Demo data add hua: 6 teachers, {} classes, {} students", cls.size(), all.size());
    }

    private Teacher teacher(String name, String subject, String email, String phone) {
        Teacher t = new Teacher();
        t.setName(name);
        t.setSubject(subject);
        t.setEmail(email);
        t.setPhone(phone);
        return teachers.save(t);
    }

    private SchoolClass clazz(String name, String section, Teacher classTeacher) {
        SchoolClass c = new SchoolClass();
        c.setName(name);
        c.setSection(section);
        c.setClassTeacher(classTeacher);
        return classes.save(c);
    }
}
