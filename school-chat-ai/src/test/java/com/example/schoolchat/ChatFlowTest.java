package com.example.schoolchat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

import com.example.schoolchat.chat.GeminiClient;
import com.example.schoolchat.chat.GeminiException;
import com.example.schoolchat.service.SchoolQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** Gemini ko mock karke poora chat flow (controller -> service -> tools -> H2 DB) test karta hai. */
@SpringBootTest
@AutoConfigureMockMvc
class ChatFlowTest {

    @Autowired MockMvc mvc;
    @Autowired SchoolQueryService svc;
    @MockBean GeminiClient gemini;

    private ResultActions chat(String question, String geminiJson) throws Exception {
        when(gemini.generateJson(any(), eq(question))).thenReturn(geminiJson);
        return mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"" + question + "\"}"));
    }

    @Test
    void countStudentsInOneSection() throws Exception {
        long expected = svc.countStudents("10", "A");
        chat("10-A mein kitne", "{\"tool\":\"count_students\",\"className\":\"10\",\"section\":\"A\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tool").value("count_students"))
                .andExpect(jsonPath("$.answer").value(containsString(expected + " active students")))
                .andExpect(jsonPath("$.rows[0].Students").value(expected));
    }

    @Test
    void countStudentsWholeClassGivesSectionWiseTable() throws Exception {
        long total = svc.countStudents("10", null);
        chat("class 10 kitne", "{\"tool\":\"count_students\",\"className\":\"Class 10th\",\"section\":null}")
                .andExpect(jsonPath("$.answer").value(containsString("total " + total)))
                .andExpect(jsonPath("$.rows", hasSize(2)));
    }

    @Test
    void inactiveStudentsAreNotCounted() throws Exception {
        // seed data mein 3 INACTIVE students hain: 40 total, 37 active
        chat("poore school mein kitne", "{\"tool\":\"count_students\"}")
                .andExpect(jsonPath("$.answer").value(containsString("37 active students")))
                .andExpect(jsonPath("$.rows", hasSize(5)));
    }

    @Test
    void listTeachersBySubject() throws Exception {
        chat("maths teacher", "{\"tool\":\"list_teachers\",\"subject\":\"Mathematics\"}")
                .andExpect(jsonPath("$.rows", hasSize(1)))
                .andExpect(jsonPath("$.rows[0].Name").value("Anita Sharma"));
    }

    @Test
    void classTeacherOfOneSection() throws Exception {
        chat("9-B ke class teacher", "{\"tool\":\"class_teacher\",\"className\":\"9\",\"section\":\"b\"}")
                .andExpect(jsonPath("$.answer").value(containsString("Rajesh Verma")));
    }

    @Test
    void listStudentsByNameFragmentIsCaseInsensitive() throws Exception {
        chat("aar naam", "{\"tool\":\"list_students\",\"name\":\"AAR\"}")
                .andExpect(jsonPath("$.tool").value("list_students"))
                .andExpect(jsonPath("$.rows[0].Name").value(containsString("Aarav")));
    }

    @Test
    void listClassesShowsCounts() throws Exception {
        chat("saari classes", "{\"tool\":\"list_classes\"}")
                .andExpect(jsonPath("$.rows", hasSize(5)))
                .andExpect(jsonPath("$.columns[3]").value("Students"));
    }

    @Test
    void unknownToolIsRejected() throws Exception {
        chat("drop karo", "{\"tool\":\"drop_table\",\"reply\":\"ok\"}")
                .andExpect(jsonPath("$.tool").value("none"))
                .andExpect(jsonPath("$.rows", hasSize(0)));
    }

    @Test
    void suspiciousParametersAreDropped() throws Exception {
        // className mein SQL jaisa text: validation ise null bana deta hai, to poore school ki ginti aati hai
        chat("hack", "{\"tool\":\"count_students\",\"className\":\"10'; DROP TABLE student;--\"}")
                .andExpect(jsonPath("$.answer").value(containsString("School mein total")));
    }

    @Test
    void geminiFailureGivesFriendlyError() throws Exception {
        when(gemini.generateJson(any(), eq("limit"))).thenThrow(new GeminiException("Gemini ki free limit abhi khatam ho gayi."));
        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"limit\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tool").value("error"))
                .andExpect(jsonPath("$.answer").value(containsString("free limit")));
    }

    @Test
    void garbageFromGeminiGivesFriendlyError() throws Exception {
        chat("kuch bhi", "yeh JSON nahi hai")
                .andExpect(jsonPath("$.tool").value("error"));
    }

    @Test
    void emptyMessageIsRejected() throws Exception {
        mvc.perform(post("/api/chat").contentType(MediaType.APPLICATION_JSON).content("{\"message\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }
}
