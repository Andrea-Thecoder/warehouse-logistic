package it.warehouse.optimization.dto.stock;

import it.warehouse.optimization.dto.product.SimpleDetailProductDTO;
import it.warehouse.optimization.dto.warehouse.SimpleDetailWarehouseDTO;
import it.warehouse.optimization.model.Stock;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public final class DetailStockDTO extends  BaseDetailStockDTO {

    private SimpleDetailWarehouseDTO warehouse;


    public static DetailStockDTO of (Stock stock){
        DetailStockDTO dto = new DetailStockDTO();
        dto.setId(stock.getId());
        dto.setProduct(SimpleDetailProductDTO.of(stock.getProduct()));
        dto.setWarehouse(SimpleDetailWarehouseDTO.of(stock.getWarehouse()));
        dto.setQuantity(stock.getQuantity());
        return dto;
    }
}
