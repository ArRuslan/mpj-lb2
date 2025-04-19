package ua.nure.mpj.lb2.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.mpj.lb2.entities.Group;
import ua.nure.mpj.lb2.entities.ScheduleItem;

public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {
    public Page<ScheduleItem> findAllByGroup(Group group, Pageable pageable);
}
