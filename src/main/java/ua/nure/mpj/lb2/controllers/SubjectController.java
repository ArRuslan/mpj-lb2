package ua.nure.mpj.lb2.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ua.nure.mpj.lb2.entities.Subject;
import ua.nure.mpj.lb2.exceptions.EntityNotFoundException;
import ua.nure.mpj.lb2.requests.SubjectCreateRequest;
import ua.nure.mpj.lb2.requests.SubjectUpdateRequest;
import ua.nure.mpj.lb2.responses.PaginatedListResponse;
import ua.nure.mpj.lb2.services.SubjectService;

import java.util.Optional;

@RestController
@RequestMapping("/subjects")
public class SubjectController {
    private final SubjectService subjectService;

    @Autowired
    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @GetMapping("/")
    public PaginatedListResponse<Subject> listSubjects(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "page_size", defaultValue = "50") Integer pageSize
    ) {
        if(pageSize > 100) {
            pageSize = 100;
        } else if(pageSize < 1) {
            pageSize = 1;
        }

        Page<Subject> result = subjectService.list(page - 1, pageSize);
        return new PaginatedListResponse<>(result.getContent(), result.getTotalElements());
    }

    @PostMapping("/")
    public Subject createSubject(@RequestBody SubjectCreateRequest createBody) {
        return subjectService.save(new Subject(createBody.getName(), createBody.getShortName()));
    }

    @GetMapping("/{id}")
    public Subject getSubjectById(@PathVariable long id) {
        Optional<Subject> subject = subjectService.get(id);
        if(subject.isEmpty()) {
            throw new EntityNotFoundException(String.format("Could not find subject with id %d", id));
        }
        return subject.get();
    }

    @PatchMapping("/{id}")
    public Subject updateSubjectEntity(@PathVariable long id, @RequestBody SubjectUpdateRequest updateBody) {
        Optional<Subject> subjectOpt = subjectService.get(id);
        if(subjectOpt.isEmpty()) {
            throw new EntityNotFoundException(String.format("Could not find subject with id %d", id));
        }

        Subject subject = subjectOpt.get();

        if((updateBody.getName() == null || updateBody.getName().isEmpty())) {
            return subject;
        }

        subject.setName(updateBody.getName());
        subjectService.save(subject);

        return subject;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubjectEntityById(@PathVariable long id) {
        subjectService.deleteById(id);
    }
}
