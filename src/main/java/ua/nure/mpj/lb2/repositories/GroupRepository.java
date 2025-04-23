package ua.nure.mpj.lb2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.mpj.lb2.entities.Group;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByNameEquals(String name);
}
