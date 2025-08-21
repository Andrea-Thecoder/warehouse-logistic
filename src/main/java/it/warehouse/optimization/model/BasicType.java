package it.warehouse.optimization.model;


import io.ebean.Model;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class BasicType extends Model {

    @Id
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
}
