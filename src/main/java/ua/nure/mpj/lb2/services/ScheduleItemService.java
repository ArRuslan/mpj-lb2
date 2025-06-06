package ua.nure.mpj.lb2.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ua.nure.mpj.lb2.entities.Group;
import ua.nure.mpj.lb2.entities.ScheduleItem;
import ua.nure.mpj.lb2.repositories.ScheduleItemRepository;

import java.util.Optional;

@Service
public class ScheduleItemService {
    private final Sort SORT_BY_ID_ASC = Sort.by(Sort.Direction.ASC, "id");

    private final ScheduleItemRepository scheduleItemRepository;

    @Autowired
    public ScheduleItemService(ScheduleItemRepository scheduleItemRepository) {
        this.scheduleItemRepository = scheduleItemRepository;
    }

    public Page<ScheduleItem> list(int page, int pageSize) {
        return scheduleItemRepository.findAll(PageRequest.of(page, pageSize, SORT_BY_ID_ASC));
    }

    public Page<ScheduleItem> list(Group group, int page, int pageSize) {
        return scheduleItemRepository.findAllByGroup(group, PageRequest.of(page, pageSize, SORT_BY_ID_ASC));
    }

    public Optional<ScheduleItem> get(long id) {
        return scheduleItemRepository.findById(id);
    }

    public ScheduleItem save(ScheduleItem scheduleItem) {
        return scheduleItemRepository.save(scheduleItem);
    }

    public void deleteById(long id) {
        scheduleItemRepository.deleteById(id);
    }
}
