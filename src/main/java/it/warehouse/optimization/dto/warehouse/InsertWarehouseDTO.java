package it.warehouse.optimization.dto.warehouse;

import it.warehouse.optimization.model.City;
import it.warehouse.optimization.model.Warehouse;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class InsertWarehouseDTO {


    @NotBlank(message = "Warehouse name must be valorized.")
    private String name;

    @NotNull(message = "Warehouse volume capacity must be valorized.")
    @Positive(message = "Warehouse volume capacity must be a positive value")
    private Double volumeCapacity;

    @NotNull(message = "Warehouse weight capacity must be valorized.")
    @Positive(message = "Warehouse weight capacity must be a positive value")
    private Double weightCapacity;

    @NotNull(message = "city ID must be valorized")
    @Positive(message = "City ID must be a positive value")
    private Long cityId;

    public Warehouse toEntity() {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(this.name);
        warehouse.setVolumeCapacity(this.volumeCapacity);
        warehouse.setWeightCapacity(this.weightCapacity);
        warehouse.setAvailableWeight(this.weightCapacity);
        warehouse.setAvailableVolume(this.volumeCapacity);
        warehouse.setCity(warehouse.db().reference(City.class,this.cityId));
        return warehouse;
    }

}
