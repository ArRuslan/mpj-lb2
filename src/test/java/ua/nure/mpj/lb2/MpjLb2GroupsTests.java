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
import ua.nure.mpj.lb2.entities.Group;
import ua.nure.mpj.lb2.requests.GroupCreateRequest;
import ua.nure.mpj.lb2.requests.GroupUpdateRequest;
import ua.nure.mpj.lb2.services.GroupService;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = MpjLb2Application.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test.properties")
class MpjLb2GroupsTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private GroupService groupService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "groups", "subjects", "schedule_items");
    }

    @Test
    void createGroup_ok() throws Exception {
        mvc.perform(post("/groups/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new GroupCreateRequest("test_group"))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("test_group"))
        ;
    }

    @Test
    void createGroup_groupNameTaken_ok() throws Exception {
        Group group = groupService.save(new Group("test_group"));

        mvc.perform(post("/groups/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new GroupCreateRequest("test_group"))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(group.getId()))
                .andExpect(jsonPath("$.name").value("test_group"))
        ;
    }

    @Test
    void listGroups_empty() throws Exception {
        mvc.perform(get("/groups/"))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.count").value(0))
        ;
    }

    @Test
    void listGroups_multipleGroups() throws Exception {
        Group group1 = new Group("test_group1");
        groupService.save(group1);
        Group group2 = new Group("test_group2");
        groupService.save(group2);
        Group group3 = new Group("test_group3");
        groupService.save(group3);

        mvc.perform(get("/groups/"))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(3)))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.items[0].id").value(group1.getId()))
                .andExpect(jsonPath("$.items[0].name").value(group1.getName()))
                .andExpect(jsonPath("$.items[1].id").value(group2.getId()))
                .andExpect(jsonPath("$.items[1].name").value(group2.getName()))
                .andExpect(jsonPath("$.items[2].id").value(group3.getId()))
                .andExpect(jsonPath("$.items[2].name").value(group3.getName()))
        ;
    }

    @Test
    void listGroups_multiplePages() throws Exception {
        Group group1 = new Group("test_group1");
        groupService.save(group1);
        Group group2 = new Group("test_group2");
        groupService.save(group2);
        Group group3 = new Group("test_group3");
        groupService.save(group3);

        mvc.perform(get("/groups/?page=1&page_size=2"))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.items[0].id").value(group1.getId()))
                .andExpect(jsonPath("$.items[0].name").value(group1.getName()))
                .andExpect(jsonPath("$.items[1].id").value(group2.getId()))
                .andExpect(jsonPath("$.items[1].name").value(group2.getName()))
        ;

        mvc.perform(get("/groups/?page=2&page_size=2"))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.items[0].id").value(group3.getId()))
                .andExpect(jsonPath("$.items[0].name").value(group3.getName()))
        ;

        mvc.perform(get("/groups/?page=3&page_size=2"))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.count").value(3))
        ;
    }

    @Test
    void getGroup_ok() throws Exception {
        Group group = new Group("test_group");
        groupService.save(group);

        mvc.perform(get(String.format("/groups/%d", group.getId())))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(group.getId()))
                .andExpect(jsonPath("$.name").value("test_group"))
        ;
    }

    @Test
    void getGroup_notFound_fail() throws Exception {
        Group group = new Group("test_group");
        groupService.save(group);

        mvc.perform(get(String.format("/groups/%d", group.getId()+100)))
                .andExpect(status().is(404))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    void editGroup_name_ok() throws Exception {
        Group group = new Group("test_group");
        groupService.save(group);

        mvc.perform(patch(String.format("/groups/%d", group.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new GroupUpdateRequest("test_123"))))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(group.getId()))
                .andExpect(jsonPath("$.name").value("test_123"))
        ;
    }

    @Test
    void editGroup_nothing_ok() throws Exception {
        Group group = new Group("test_group");
        groupService.save(group);

        mvc.perform(patch(String.format("/groups/%d", group.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new GroupUpdateRequest(null))))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(group.getId()))
                .andExpect(jsonPath("$.name").value("test_group"))
        ;
    }

    @Test
    void editGroup_notFound_fail() throws Exception {
        Group group = new Group("test_group");
        groupService.save(group);

        mvc.perform(patch(String.format("/groups/%d", group.getId()+100))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new GroupUpdateRequest("new_name"))))
                .andExpect(status().is(404))
        ;
    }

    @Test
    void editGroup_nameTaken_ok() throws Exception {
        Group group = new Group("test_group");
        groupService.save(group);
        groupService.save(new Group("taken_name"));

        mvc.perform(patch(String.format("/groups/%d", group.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new GroupUpdateRequest("taken_name"))))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(group.getId()))
                .andExpect(jsonPath("$.name").value("test_group"))
        ;
    }

    @Test
    void deleteGroup_ok() throws Exception {
        Group group = new Group("test_group");
        groupService.save(group);

        mvc.perform(delete(String.format("/groups/%d", group.getId()+100)))
                .andExpect(status().is(204))
        ;
    }

}
