package it.warehouse.optimization.dto.warehouse;

import it.warehouse.optimization.model.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class SimpleDetailWarehouseDTO {

    protected UUID id;
    protected String name;
    protected String cityName;

    public static SimpleDetailWarehouseDTO of (Warehouse warehouse){
        SimpleDetailWarehouseDTO dto = new SimpleDetailWarehouseDTO();
        dto.setId(warehouse.getId());
        dto.setName(warehouse.getName());
        dto.setCityName(warehouse.getCity().getName());
        return dto;
    }

}
