package it.warehouse.optimization.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class SimpleResultDTO<T> {
    private T payload;
    private String message;
}
