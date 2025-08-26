package it.warehouse.optimization.dto.stock;

import it.warehouse.optimization.dto.product.SimpleDetailProductDTO;
import it.warehouse.optimization.dto.warehouse.SimpleDetailWarehouseDTO;
import it.warehouse.optimization.model.Stock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public sealed class BaseDetailStockDTO permits  DetailStockDTO, DetailStockWarehouseDTO {

    protected Long id;
    protected SimpleDetailProductDTO product;
    protected int quantity;



}
