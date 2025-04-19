package ua.nure.mpj.lb2.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ua.nure.mpj.lb2.entities.ScheduleItem;

import java.sql.Date;

@Getter
@Setter
@AllArgsConstructor
public class ScheduleItemUpdateRequest {
    @JsonProperty(value = "group_id")
    private long groupId;

    @JsonProperty(value = "subject_id")
    private long subjectId;

    private ScheduleItem.Type type;

    private Date date;

    private byte position;
}
