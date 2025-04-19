package ua.nure.mpj.lb2.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SubjectUpdateRequest {
    private String name;

    @JsonProperty(value = "short_name")
    private String shortName;
}
