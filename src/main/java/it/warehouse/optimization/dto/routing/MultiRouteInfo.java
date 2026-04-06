package it.warehouse.optimization.dto.routing;

import it.warehouse.optimization.dto.warehouse.BaseDetailWarehouseDTO;
import it.warehouse.optimization.dto.warehouse.SimpleDetailWarehouseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MultiRouteInfo extends SingleRouteInfo{

    private int stopOrder;
    private SimpleDetailWarehouseDTO fromWarehouse;
    private SimpleDetailWarehouseDTO toWarehouse;

}
