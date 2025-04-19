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
public class ScheduleItemCreateRequest {
    @JsonProperty(value = "group_id", required = true)
    private long groupId;

    @JsonProperty(value = "subject_id", required = true)
    private long subjectId;

    @JsonProperty(required = true)
    private ScheduleItem.Type type;

    @JsonProperty(required = true)
    private Date date;

    @JsonProperty(required = true)
    private byte position;
}
