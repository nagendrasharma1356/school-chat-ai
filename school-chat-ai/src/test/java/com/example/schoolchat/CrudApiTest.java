package com.example.schoolchat;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.schoolchat.chat.GeminiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CrudApiTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean GeminiClient gemini;

    private JsonNode create(String url, String json) throws Exception {
        String body = mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    @Test
    void teacherClassStudentLifecycle() throws Exception {
        long t = create("/api/teachers", "{\"name\":\"Test Teacher\",\"subject\":\"Art\"}").get("id").asLong();
        long c = create("/api/classes", "{\"name\":\"12\",\"section\":\"z\",\"classTeacherId\":" + t + "}").get("id").asLong();
        long s = create("/api/students",
                "{\"name\":\"Test Student\",\"rollNo\":1,\"classId\":" + c + "}").get("id").asLong();

        mvc.perform(get("/api/students/" + s))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.schoolClass.section").value("Z"))
                .andExpect(jsonPath("$.schoolClass.classTeacher.name").value("Test Teacher"));

        mvc.perform(put("/api/students/" + s).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Renamed\",\"rollNo\":2,\"status\":\"inactive\",\"classId\":" + c + "}"))
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mvc.perform(get("/api/students").param("className", "12").param("section", "Z"))
                .andExpect(jsonPath("$", hasSize(1)));

        // teacher abhi class se juda hai: delete 409 dena chahiye
        mvc.perform(delete("/api/teachers/" + t)).andExpect(status().isConflict());

        mvc.perform(delete("/api/students/" + s)).andExpect(status().isNoContent());
        mvc.perform(get("/api/students/" + s)).andExpect(status().isNotFound());

        // cleanup: doosre tests ke seed counts na bigdein
        mvc.perform(delete("/api/classes/" + c)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/teachers/" + t)).andExpect(status().isNoContent());
    }

    @Test
    void validationErrorsReturn400() throws Exception {
        mvc.perform(post("/api/teachers").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.name").exists());
        mvc.perform(post("/api/students").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"X\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listsAreSeeded() throws Exception {
        mvc.perform(get("/api/teachers")).andExpect(jsonPath("$.length()", greaterThan(5)));
        mvc.perform(get("/api/classes")).andExpect(jsonPath("$.length()", greaterThan(4)));
    }
}
