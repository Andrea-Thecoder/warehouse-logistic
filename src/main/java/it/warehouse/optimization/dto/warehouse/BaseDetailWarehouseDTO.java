package it.warehouse.optimization.dto.warehouse;

import it.warehouse.optimization.model.City;
import it.warehouse.optimization.model.Warehouse;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class BaseDetailWarehouseDTO {

    protected UUID id;
    protected String name;
    protected Double volumeCapacity;
    protected Double weightCapacity;
    protected String cityName;
    protected String regionName;

    public static BaseDetailWarehouseDTO of (Warehouse warehouse){
        BaseDetailWarehouseDTO dto = new BaseDetailWarehouseDTO();
        dto.setId(warehouse.getId());
        dto.setName(warehouse.getName());
        dto.setVolumeCapacity(warehouse.getVolumeCapacity());
        dto.setWeightCapacity(warehouse.getWeightCapacity());
        City warehouseCity = warehouse.getCity();
        dto.setCityName(warehouseCity.getName());
        dto.setRegionName(warehouseCity.getRegion().getName());
        return dto;
    }
}
