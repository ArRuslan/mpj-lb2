package ua.nure.mpj.lb2.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GroupCreateRequest {
    @JsonProperty(required = true)
    private String name;
}
