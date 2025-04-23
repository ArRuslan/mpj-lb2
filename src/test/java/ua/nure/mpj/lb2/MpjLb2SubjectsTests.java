package ua.nure.mpj.lb2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import ua.nure.mpj.lb2.entities.Subject;
import ua.nure.mpj.lb2.requests.SubjectCreateRequest;
import ua.nure.mpj.lb2.requests.SubjectUpdateRequest;
import ua.nure.mpj.lb2.services.SubjectService;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = MpjLb2Application.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test.properties")
class MpjLb2SubjectsTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "subjects", "subjects", "schedule_items");
    }

    @Test
    void createSubject_ok() throws Exception {
        mvc.perform(post("/subjects/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new SubjectCreateRequest("test_subject", "ts1"))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("test_subject"))
        ;
    }

    @Test
    void createSubject_subjectNameTaken_ok() throws Exception {
        Subject subject = subjectService.save(new Subject("test_subject", "ts1"));

        mvc.perform(post("/subjects/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new SubjectCreateRequest("test_subject", "ts1"))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("test_subject"))
        ;
    }

    @Test
    void listSubjects_empty() throws Exception {
        mvc.perform(get("/subjects/"))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.count").value(0))
        ;
    }

    @Test
    void listSubjects_multipleSubjects() throws Exception {
        Subject subject1 = new Subject("test_subject1", "ts1");
        subjectService.save(subject1);
        Subject subject2 = new Subject("test_subject2", "ts2");
        subjectService.save(subject2);
        Subject subject3 = new Subject("test_subject3", "ts3");
        subjectService.save(subject3);

        mvc.perform(get("/subjects/"))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.items[0].id").value(subject1.getId()))
                .andExpect(jsonPath("$.items[0].name").value(subject1.getName()))
                .andExpect(jsonPath("$.items[1].id").value(subject2.getId()))
                .andExpect(jsonPath("$.items[1].name").value(subject2.getName()))
                .andExpect(jsonPath("$.items[2].id").value(subject3.getId()))
                .andExpect(jsonPath("$.items[2].name").value(subject3.getName()))
        ;
    }

    @Test
    void listSubjects_multiplePages() throws Exception {
        Subject subject1 = new Subject("test_subject1", "ts1");
        subjectService.save(subject1);
        Subject subject2 = new Subject("test_subject2", "ts2");
        subjectService.save(subject2);
        Subject subject3 = new Subject("test_subject3", "ts3");
        subjectService.save(subject3);

        mvc.perform(get("/subjects/?page=1&page_size=2"))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.items[0].id").value(subject1.getId()))
                .andExpect(jsonPath("$.items[0].name").value(subject1.getName()))
                .andExpect(jsonPath("$.items[1].id").value(subject2.getId()))
                .andExpect(jsonPath("$.items[1].name").value(subject2.getName()))
        ;

        mvc.perform(get("/subjects/?page=2&page_size=2"))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.items[0].id").value(subject3.getId()))
                .andExpect(jsonPath("$.items[0].name").value(subject3.getName()))
        ;

        mvc.perform(get("/subjects/?page=3&page_size=2"))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.count").value(3))
        ;
    }

    @Test
    void getSubject_ok() throws Exception {
        Subject subject = new Subject("test_subject", "ts1");
        subjectService.save(subject);

        mvc.perform(get(String.format("/subjects/%d", subject.getId())))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(subject.getId()))
                .andExpect(jsonPath("$.name").value("test_subject"))
        ;
    }

    @Test
    void getSubject_notFound_fail() throws Exception {
        Subject subject = new Subject("test_subject", "ts1");
        subjectService.save(subject);

        mvc.perform(get(String.format("/subjects/%d", subject.getId()+100)))
                .andExpect(status().is(404))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void editSubject_name_ok() throws Exception {
        Subject subject = new Subject("test_subject", "ts1");
        subjectService.save(subject);

        mvc.perform(patch(String.format("/subjects/%d", subject.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new SubjectUpdateRequest("test_123", null))))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(subject.getId()))
                .andExpect(jsonPath("$.name").value("test_123"))
        ;
    }

    @Test
    void editSubject_nothing_ok() throws Exception {
        Subject subject = new Subject("test_subject", "ts1");
        subjectService.save(subject);

        mvc.perform(patch(String.format("/subjects/%d", subject.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new SubjectUpdateRequest(null, null))))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(subject.getId()))
                .andExpect(jsonPath("$.name").value("test_subject"))
        ;
    }

    @Test
    void editSubject_notFound_fail() throws Exception {
        Subject subject = new Subject("test_subject", "ts1");
        subjectService.save(subject);

        mvc.perform(patch(String.format("/subjects/%d", subject.getId()+100))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new SubjectUpdateRequest("new_name", null))))
                .andExpect(status().is(404))
        ;
    }

    @Test
    void editSubject_nameTaken_ok() throws Exception {
        Subject subject = new Subject("test_subject", "ts1");
        subjectService.save(subject);
        subjectService.save(new Subject("taken_name", "tn1"));

        mvc.perform(patch(String.format("/subjects/%d", subject.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new SubjectUpdateRequest("taken_name", null))))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name").value("taken_name"))
        ;
    }

    @Test
    void deleteSubject_ok() throws Exception {
        Subject subject = new Subject("test_subject", "ts1");
        subjectService.save(subject);

        mvc.perform(delete(String.format("/subjects/%d", subject.getId()+100)))
                .andExpect(status().is(204))
        ;
    }

}
