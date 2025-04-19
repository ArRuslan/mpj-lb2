package ua.nure.mpj.lb2.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ua.nure.mpj.lb2.entities.Group;
import ua.nure.mpj.lb2.entities.ScheduleItem;
import ua.nure.mpj.lb2.exceptions.EntityNotFoundException;
import ua.nure.mpj.lb2.requests.GroupCreateRequest;
import ua.nure.mpj.lb2.requests.GroupUpdateRequest;
import ua.nure.mpj.lb2.responses.PaginatedListResponse;
import ua.nure.mpj.lb2.services.GroupService;
import ua.nure.mpj.lb2.services.ScheduleItemService;

import java.util.Optional;

@RestController
@RequestMapping("/groups")
public class GroupController {
    private final GroupService groupService;
    private final ScheduleItemService scheduleItemService;

    @Autowired
    public GroupController(GroupService groupService, ScheduleItemService scheduleItemService) {
        this.groupService = groupService;
        this.scheduleItemService = scheduleItemService;
    }

    @GetMapping("/")
    public PaginatedListResponse<Group> listGroups(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "page_size", defaultValue = "50") Integer pageSize
    ) {
        if(pageSize > 100) {
            pageSize = 100;
        } else if(pageSize < 1) {
            pageSize = 1;
        }

        Page<Group> result = groupService.list(page - 1, pageSize);
        return new PaginatedListResponse<>(result.getContent(), result.getTotalElements());
    }

    @PostMapping("/")
    public Group createGroup(@RequestBody GroupCreateRequest createBody) {
        return groupService.save(new Group(createBody.getName()));
    }

    @GetMapping("/{id}")
    public Group getGroupById(@PathVariable long id) {
        Optional<Group> group = groupService.get(id);
        if(group.isEmpty()) {
            throw new EntityNotFoundException(String.format("Could not find group with id %d", id));
        }
        return group.get();
    }

    @PatchMapping("/{id}")
    public Group updateGroupEntity(@PathVariable long id, @RequestBody GroupUpdateRequest updateBody) {
        Optional<Group> groupOpt = groupService.get(id);
        if(groupOpt.isEmpty()) {
            throw new EntityNotFoundException(String.format("Could not find group with id %d", id));
        }

        Group group = groupOpt.get();

        if((updateBody.getName() == null || updateBody.getName().isEmpty())) {
            return group;
        }

        group.setName(updateBody.getName());
        groupService.save(group);

        return group;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroupEntityById(@PathVariable long id) {
        groupService.deleteById(id);
    }

    @GetMapping("/{id}/scheduleItems")
    public PaginatedListResponse<ScheduleItem> getGroupScheduleItems(
            @PathVariable long id,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "page_size", defaultValue = "50") Integer pageSize
    ) {
        Optional<Group> group = groupService.get(id);
        if(group.isEmpty()) {
            throw new EntityNotFoundException(String.format("Could not find entity with id %d", id));
        }

        Page<ScheduleItem> result = scheduleItemService.list(group.get(), page - 1, pageSize);

        return new PaginatedListResponse<>(
                result.getContent(),
                result.getTotalElements()
        );
    }
}
