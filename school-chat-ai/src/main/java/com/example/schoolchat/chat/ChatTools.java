package com.example.schoolchat.chat;

import com.example.schoolchat.model.SchoolClass;
import com.example.schoolchat.model.Student;
import com.example.schoolchat.model.Teacher;
import com.example.schoolchat.service.SchoolQueryService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ChatTools {

    static final int MAX_ROWS = 50;

    private static final List<String> CLASS_COLS = List.of("Class", "Section", "Class Teacher", "Students");

    private final SchoolQueryService svc;

    public ChatTools(SchoolQueryService svc) {
        this.svc = svc;
    }

    public ToolResult countStudents(String cls, String sec) {
        if (cls == null && sec == null) {
            long total = svc.countStudents(null, null);
            return new ToolResult("School mein total " + total + " active students hain.", CLASS_COLS, classRows());
        }
        long total = svc.countStudents(cls, sec);
        String label = classLabel(cls, sec);

        if (total == 0) {
            return new ToolResult(label + " mein koi active student nahi mila.", List.of(), List.of());
        }
        if (cls != null && sec == null) {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Map<String, Object> s : svc.sectionWise(cls)) {
                rows.add(row("Class", cls, "Section", s.get("section"), "Students", s.get("count")));
            }
            return new ToolResult(label + " mein total " + total + " active students hain.",
                    List.of("Class", "Section", "Students"), rows);
        }
        return new ToolResult(label + " mein " + total + " active students hain.",
                List.of("Class", "Section", "Students"),
                List.of(row("Class", cls == null ? "-" : cls, "Section", sec == null ? "-" : sec, "Students", total)));
    }

    public ToolResult listStudents(String cls, String sec, String name) {
        Page<Student> page = svc.listStudents(cls, sec, name, true, MAX_ROWS);
        long total = page.getTotalElements();
        String where = filterText(cls, sec, name, null);

        if (total == 0) {
            return new ToolResult(where + "koi active student nahi mila.", List.of(), List.of());
        }
        List<String> cols = List.of("Roll No", "Name", "Class", "Section", "Guardian", "Phone");
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Student s : page.getContent()) {
            SchoolClass c = s.getSchoolClass();
            rows.add(row("Roll No", nz(s.getRollNo()), "Name", s.getName(),
                    "Class", c == null ? "-" : c.getName(), "Section", c == null ? "-" : c.getSection(),
                    "Guardian", nz(s.getGuardianName()), "Phone", nz(s.getPhone())));
        }
        String more = total > MAX_ROWS ? " (pehle " + MAX_ROWS + " dikha raha hu)" : "";
        return new ToolResult(where + total + " active students mile" + more + ".", cols, rows);
    }

    public ToolResult listTeachers(String subject, String name) {
        List<Teacher> list = svc.listTeachers(subject, name);
        String where = filterText(null, null, name, subject);

        if (list.isEmpty()) {
            return new ToolResult(where + "koi teacher nahi mila.", List.of(), List.of());
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Teacher t : list) {
            rows.add(row("Name", t.getName(), "Subject", nz(t.getSubject()),
                    "Email", nz(t.getEmail()), "Phone", nz(t.getPhone())));
        }
        return new ToolResult(where + list.size() + " teachers mile.",
                List.of("Name", "Subject", "Email", "Phone"), rows);
    }

    public ToolResult listClasses() {
        List<Map<String, Object>> rows = classRows();
        return new ToolResult("Total " + rows.size() + " classes/sections hain.", CLASS_COLS, rows);
    }

    public ToolResult classTeacher(String cls, String sec) {
        if (cls == null) {
            return listClasses();
        }
        List<SchoolClass> found = new ArrayList<>();
        if (sec != null) {
            svc.findClass(cls, sec).ifPresent(found::add);
        } else {
            found.addAll(svc.findClassesByName(cls));
        }
        if (found.isEmpty()) {
            return new ToolResult(classLabel(cls, sec) + " nahi mili.", List.of(), List.of());
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SchoolClass c : found) {
            Teacher t = c.getClassTeacher();
            rows.add(row("Class", c.getName(), "Section", c.getSection(),
                    "Class Teacher", t == null ? "-" : t.getName(), "Subject", t == null ? "-" : nz(t.getSubject())));
        }
        List<String> cols = List.of("Class", "Section", "Class Teacher", "Subject");
        if (found.size() == 1) {
            SchoolClass c = found.get(0);
            String label = classLabel(c.getName(), c.getSection());
            String answer = c.getClassTeacher() == null
                    ? label + " ke liye abhi class teacher set nahi hai."
                    : label + " ke class teacher " + c.getClassTeacher().getName() + " hain.";
            return new ToolResult(answer, cols, rows);
        }
        return new ToolResult(classLabel(cls, null) + " ke sections ke class teachers neeche hain.", cols, rows);
    }

    private List<Map<String, Object>> classRows() {
        Map<Long, Long> counts = svc.activeCountPerClass();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (SchoolClass c : svc.listClasses()) {
            rows.add(row("Class", c.getName(), "Section", c.getSection(),
                    "Class Teacher", c.getClassTeacher() == null ? "-" : c.getClassTeacher().getName(),
                    "Students", counts.getOrDefault(c.getId(), 0L)));
        }
        return rows;
    }

    private static String classLabel(String cls, String sec) {
        if (cls == null && sec == null) return "School";
        if (sec == null) return "Class " + cls;
        if (cls == null) return "Section " + sec;
        return "Class " + cls + "-" + sec;
    }

    private static String filterText(String cls, String sec, String name, String subject) {
        StringBuilder sb = new StringBuilder();
        if (cls != null || sec != null) sb.append(classLabel(cls, sec)).append(" mein ");
        if (subject != null) sb.append(subject).append(" ke ");
        if (name != null) sb.append("'").append(name).append("' naam ke ");
        return sb.toString();
    }

    private static Object nz(Object o) {
        return o == null ? "-" : o;
    }

    private static Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
        return m;
    }
}
