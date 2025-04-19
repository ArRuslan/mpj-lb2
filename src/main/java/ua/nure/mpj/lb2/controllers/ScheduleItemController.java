package ua.nure.mpj.lb2.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ua.nure.mpj.lb2.entities.Group;
import ua.nure.mpj.lb2.entities.Subject;
import ua.nure.mpj.lb2.entities.ScheduleItem;
import ua.nure.mpj.lb2.exceptions.EntityNotFoundException;
import ua.nure.mpj.lb2.requests.ScheduleItemCreateRequest;
import ua.nure.mpj.lb2.requests.ScheduleItemUpdateRequest;
import ua.nure.mpj.lb2.responses.PaginatedListResponse;
import ua.nure.mpj.lb2.services.GroupService;
import ua.nure.mpj.lb2.services.ScheduleItemService;
import ua.nure.mpj.lb2.services.SubjectService;

import java.util.Optional;

@RestController
@RequestMapping("/scheduleItems")
public class ScheduleItemController {
    private final GroupService groupService;
    private final SubjectService subjectService;
    private final ScheduleItemService scheduleItemService;

    @Autowired
    public ScheduleItemController(GroupService groupService, SubjectService subjectService, ScheduleItemService scheduleItemService) {
        this.groupService = groupService;
        this.subjectService = subjectService;
        this.scheduleItemService = scheduleItemService;
    }

    @GetMapping("/")
    public PaginatedListResponse<ScheduleItem> listScheduleItems(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "page_size", defaultValue = "50") Integer pageSize
    ) {
        if(pageSize > 100) {
            pageSize = 100;
        } else if(pageSize < 1) {
            pageSize = 1;
        }

        Page<ScheduleItem> result = scheduleItemService.list(page - 1, pageSize);
        return new PaginatedListResponse<>(result.getContent(), result.getTotalElements());
    }

    private Group getGroupOrThrow(long groupId) {
        Optional<Group> groupOpt = groupService.get(groupId);
        if(groupOpt.isEmpty()) {
            throw new EntityNotFoundException(String.format("Could not find group with id %d", groupId));
        }
        return groupOpt.get();
    }

    private Subject getSubjectOrThrow(long subjectId) {
        Optional<Subject> subjectOpt = subjectService.get(subjectId);
        if(subjectOpt.isEmpty()) {
            throw new EntityNotFoundException(String.format("Could not find subject with id %d", subjectId));
        }
        return subjectOpt.get();
    }

    @PostMapping("/")
    public ScheduleItem createScheduleItem(@RequestBody ScheduleItemCreateRequest createBody) {
        Group group = getGroupOrThrow(createBody.getGroupId());
        Subject subject = getSubjectOrThrow(createBody.getSubjectId());

        return scheduleItemService.save(new ScheduleItem(group, subject, createBody.getDate(), createBody.getPosition(), createBody.getType()));
    }

    @GetMapping("/{id}")
    public ScheduleItem getScheduleItemById(@PathVariable long id) {
        Optional<ScheduleItem> scheduleItem = scheduleItemService.get(id);
        if(scheduleItem.isEmpty()) {
            throw new EntityNotFoundException(String.format("Could not find schedule item with id %d", id));
        }
        return scheduleItem.get();
    }

    @PatchMapping("/{id}")
    public ScheduleItem updateScheduleItemEntity(@PathVariable long id, @RequestBody ScheduleItemUpdateRequest updateBody) {
        Optional<ScheduleItem> scheduleItemOpt = scheduleItemService.get(id);
        if(scheduleItemOpt.isEmpty()) {
            throw new EntityNotFoundException(String.format("Could not find schedule item with id %d", id));
        }

        ScheduleItem scheduleItem = scheduleItemOpt.get();

        if(updateBody.getGroupId() != null) {
            scheduleItem.setGroup(getGroupOrThrow(updateBody.getGroupId()));
        }
        if(updateBody.getSubjectId() != null) {
            scheduleItem.setSubject(getSubjectOrThrow(updateBody.getSubjectId()));
        }
        if(updateBody.getType() != null) {
            scheduleItem.setType(updateBody.getType());
        }
        if(updateBody.getDate() != null) {
            scheduleItem.setDate(updateBody.getDate());
        }
        if(updateBody.getPosition() != null) {
            scheduleItem.setPosition(updateBody.getPosition());
        }

        return scheduleItemService.save(scheduleItem);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteScheduleItemEntityById(@PathVariable long id) {
        scheduleItemService.deleteById(id);
    }
}
