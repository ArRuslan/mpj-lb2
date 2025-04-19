package ua.nure.mpj.lb2.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.mpj.lb2.entities.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}
