package ua.nure.mpj.mpjlb1.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Entity
@Table(name = "schedule_items")
@Getter
@NoArgsConstructor
public class ScheduleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Setter
    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Setter
    @Column(nullable = false)
    private Date date;

    @Setter
    @Column(nullable = false)
    private byte position;

    @Setter
    @Column(nullable = false)
    private Type type;

    public static enum Type {
        LECTURE,
        PRACTICE,
        LAB,
        EXAM,
    }
}
