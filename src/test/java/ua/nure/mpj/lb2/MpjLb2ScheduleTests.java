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
import ua.nure.mpj.lb2.entities.ScheduleItem;
import ua.nure.mpj.lb2.entities.Subject;
import ua.nure.mpj.lb2.requests.GroupCreateRequest;
import ua.nure.mpj.lb2.requests.GroupUpdateRequest;
import ua.nure.mpj.lb2.requests.ScheduleItemCreateRequest;
import ua.nure.mpj.lb2.requests.ScheduleItemUpdateRequest;
import ua.nure.mpj.lb2.services.GroupService;
import ua.nure.mpj.lb2.services.ScheduleItemService;
import ua.nure.mpj.lb2.services.SubjectService;

import java.sql.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = MpjLb2Application.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test.properties")
class MpjLb2ScheduleTests {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private GroupService groupService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private ScheduleItemService scheduleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "schedule_items", "groups", "subjects");
    }

    @Test
    void createSchedule_ok() throws Exception {
        Group group = groupService.save(new Group("test_group"));
        Subject subject = subjectService.save(new Subject("test_subject", "ts1"));

        mvc.perform(post("/scheduleItems/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new ScheduleItemCreateRequest(group.getId(), subject.getId(), ScheduleItem.Type.LAB, new Date(2025, 4, 24), (byte) 3))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.type").value(ScheduleItem.Type.LAB.name()))
                .andExpect(jsonPath("$.group.id").value(group.getId()))
                .andExpect(jsonPath("$.subject.id").value(subject.getId()))
        ;
    }

    @Test
    void listSchedules_empty() throws Exception {
        Group group = groupService.save(new Group("test_group"));

        mvc.perform(get("/groups/{groupId}/scheduleItems", group.getId()))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.count").value(0))
        ;
    }

    @Test
    void listSchedules_multiple() throws Exception {
        Group group1 = groupService.save(new Group("test_group1"));
        Group group2 = groupService.save(new Group("test_group2"));
        Subject subject = subjectService.save(new Subject("test_subject", "ts1"));
        scheduleService.save(new ScheduleItem(group1, subject, new Date(2025, 4, 24), (byte)3, ScheduleItem.Type.LAB));
        scheduleService.save(new ScheduleItem(group2, subject, new Date(2025, 4, 24), (byte)4, ScheduleItem.Type.LAB));

        mvc.perform(get("/scheduleItems/"))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.items[0].position").value(3))
                .andExpect(jsonPath("$.items[0].type").value(ScheduleItem.Type.LAB.name()))
                .andExpect(jsonPath("$.items[0].group.id").value(group1.getId()))
                .andExpect(jsonPath("$.items[0].subject.id").value(subject.getId()))
                .andExpect(jsonPath("$.items[1].position").value(4))
                .andExpect(jsonPath("$.items[1].type").value(ScheduleItem.Type.LAB.name()))
                .andExpect(jsonPath("$.items[1].group.id").value(group2.getId()))
                .andExpect(jsonPath("$.items[1].subject.id").value(subject.getId()))
        ;

        mvc.perform(get("/groups/{groupId}/scheduleItems", group1.getId()))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.items[0].position").value(3))
                .andExpect(jsonPath("$.items[0].type").value(ScheduleItem.Type.LAB.name()))
                .andExpect(jsonPath("$.items[0].group.id").value(group1.getId()))
                .andExpect(jsonPath("$.items[0].subject.id").value(subject.getId()))
        ;

        mvc.perform(get("/groups/{groupId}/scheduleItems", group2.getId()))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.items[0].position").value(4))
                .andExpect(jsonPath("$.items[0].type").value(ScheduleItem.Type.LAB.name()))
                .andExpect(jsonPath("$.items[0].group.id").value(group2.getId()))
                .andExpect(jsonPath("$.items[0].subject.id").value(subject.getId()))
        ;
    }

    @Test
    void getSchedule_ok() throws Exception {
        Group group = groupService.save(new Group("test_group"));
        Subject subject = subjectService.save(new Subject("test_subject", "ts1"));
        ScheduleItem item = scheduleService.save(new ScheduleItem(group, subject, new Date(2025, 4, 24), (byte)3, ScheduleItem.Type.LAB));

        mvc.perform(get("/scheduleItems/{scheduleItemId}", item.getId()))
                .andExpect(status().is(200))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.position").value(3))
                .andExpect(jsonPath("$.type").value(ScheduleItem.Type.LAB.name()))
                .andExpect(jsonPath("$.group.id").value(group.getId()))
                .andExpect(jsonPath("$.subject.id").value(subject.getId()))
        ;
    }

    @Test
    void getSchedule_notFound() throws Exception {
        Group group = groupService.save(new Group("test_group"));
        Subject subject = subjectService.save(new Subject("test_subject", "ts1"));
        ScheduleItem item = scheduleService.save(new ScheduleItem(group, subject, new Date(2025, 4, 24), (byte)3, ScheduleItem.Type.LAB));

        mvc.perform(get("/scheduleItems/{scheduleItemId}", item.getId() + 100))
                .andExpect(status().is(404))
        ;
    }

    @Test
    void createSchedule_groupNotFound() throws Exception {
        Group group = groupService.save(new Group("test_group"));
        Subject subject = subjectService.save(new Subject("test_subject", "ts1"));

        mvc.perform(post("/scheduleItems/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new ScheduleItemCreateRequest(group.getId() + 1000, subject.getId(), ScheduleItem.Type.LAB, new Date(2025, 4, 24), (byte) 3))))
                .andExpect(status().is(404))
        ;
    }

    @Test
    void createSchedule_subjectNotFound() throws Exception {
        Group group = groupService.save(new Group("test_group"));
        Subject subject = subjectService.save(new Subject("test_subject", "ts1"));

        mvc.perform(post("/scheduleItems/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new ScheduleItemCreateRequest(group.getId(), subject.getId() + 1000, ScheduleItem.Type.LAB, new Date(2025, 4, 24), (byte) 3))))
                .andExpect(status().is(404))
        ;
    }

    @Test
    void deleteSchedule_ok() throws Exception {
        Group group = groupService.save(new Group("test_group"));
        Subject subject = subjectService.save(new Subject("test_subject", "ts1"));
        ScheduleItem item = scheduleService.save(new ScheduleItem(group, subject, new Date(2025, 4, 24), (byte)3, ScheduleItem.Type.LAB));

        mvc.perform(delete("/scheduleItems/{scheduleItemId}", item.getId()))
                .andExpect(status().is(204))
        ;

        mvc.perform(get("/scheduleItems/{scheduleItemId}", item.getId()))
                .andExpect(status().is(404))
        ;
    }

    @Test
    void updateSchedule_group_ok() throws Exception {
        Group group = groupService.save(new Group("test_group"));
        Group group2 = groupService.save(new Group("test_group2"));
        Subject subject = subjectService.save(new Subject("test_subject", "ts1"));
        ScheduleItem item = scheduleService.save(new ScheduleItem(group, subject, new Date(2025, 4, 24), (byte)3, ScheduleItem.Type.LAB));

        mvc.perform(patch("/scheduleItems/{scheduleItemId}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new ScheduleItemUpdateRequest(group2.getId(), null, null, null, null))))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.position").value(3))
                .andExpect(jsonPath("$.type").value(ScheduleItem.Type.LAB.name()))
                .andExpect(jsonPath("$.group.id").value(group2.getId()))
                .andExpect(jsonPath("$.subject.id").value(subject.getId()))
        ;
    }

    @Test
    void updateSchedule_subject_ok() throws Exception {
        Group group = groupService.save(new Group("test_group"));
        Subject subject = subjectService.save(new Subject("test_subject", "ts1"));
        Subject subject2 = subjectService.save(new Subject("test_subject2", "ts2"));
        ScheduleItem item = scheduleService.save(new ScheduleItem(group, subject, new Date(2025, 4, 24), (byte)3, ScheduleItem.Type.LAB));

        mvc.perform(patch("/scheduleItems/{scheduleItemId}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new ScheduleItemUpdateRequest(null, subject2.getId(), null, null, null))))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.position").value(3))
                .andExpect(jsonPath("$.type").value(ScheduleItem.Type.LAB.name()))
                .andExpect(jsonPath("$.group.id").value(group.getId()))
                .andExpect(jsonPath("$.subject.id").value(subject2.getId()))
        ;
    }

    @Test
    void updateSchedule_nothing_ok() throws Exception {
        Group group = groupService.save(new Group("test_group"));
        Subject subject = subjectService.save(new Subject("test_subject", "ts1"));
        ScheduleItem item = scheduleService.save(new ScheduleItem(group, subject, new Date(2025, 4, 24), (byte)3, ScheduleItem.Type.LAB));

        mvc.perform(patch("/scheduleItems/{scheduleItemId}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new ScheduleItemUpdateRequest(null, null, null, null, null))))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.position").value(3))
                .andExpect(jsonPath("$.type").value(ScheduleItem.Type.LAB.name()))
                .andExpect(jsonPath("$.group.id").value(group.getId()))
                .andExpect(jsonPath("$.subject.id").value(subject.getId()))
        ;
    }

    @Test
    void updateSchedule_group_notFound() throws Exception {
        Group group = groupService.save(new Group("test_group"));
        Subject subject = subjectService.save(new Subject("test_subject", "ts1"));
        ScheduleItem item = scheduleService.save(new ScheduleItem(group, subject, new Date(2025, 4, 24), (byte)3, ScheduleItem.Type.LAB));

        mvc.perform(patch("/scheduleItems/{scheduleItemId}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new ScheduleItemUpdateRequest(group.getId()+100, null, null, null, null))))
                .andExpect(status().is(404))
        ;
    }

    @Test
    void updateSchedule_subject_notFound() throws Exception {
        Group group = groupService.save(new Group("test_group"));
        Subject subject = subjectService.save(new Subject("test_subject", "ts1"));
        ScheduleItem item = scheduleService.save(new ScheduleItem(group, subject, new Date(2025, 4, 24), (byte)3, ScheduleItem.Type.LAB));

        mvc.perform(patch("/scheduleItems/{scheduleItemId}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(new ScheduleItemUpdateRequest(null, subject.getId()+100, null, null, null))))
                .andExpect(status().is(404))
        ;
    }
/*


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
*/
}
