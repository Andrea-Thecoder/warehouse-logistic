package it.warehouse.optimization.exception;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionResponse {

    String title;
    Integer status;
    List<Violation> violations;

    public ExceptionResponse(Integer status, Exception ex) {
        this.title = ex.getClass().getName();
        this.status = status;
        this.addViolation(ex.getMessage());
    }

    public void addViolation(String message){
        if(violations == null)
            violations = new ArrayList<>();
        this.violations.add(new Violation(message));
    }

    @Getter
    @Setter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Violation {
        String field;
        String message;
        public Violation(String message) {
            this.message = message;
        }
    }
}
