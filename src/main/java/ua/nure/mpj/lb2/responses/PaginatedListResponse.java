package ua.nure.mpj.lb2.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaginatedListResponse<T> {
    @JsonProperty(value = "items", required = true)
    private List<T> items;

    @JsonProperty(value = "count", required = true)
    private long count;
}
