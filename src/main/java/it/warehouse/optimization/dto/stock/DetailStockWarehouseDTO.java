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

public final  class DetailStockWarehouseDTO extends  BaseDetailStockDTO {

    private double totalWeight;
    private double totalVolume;


    public static DetailStockWarehouseDTO of (Stock stock){
        DetailStockWarehouseDTO dto = new DetailStockWarehouseDTO();
        dto.setId(stock.getId());
        dto.setProduct(SimpleDetailProductDTO.of(stock.getProduct()));
        dto.setQuantity(stock.getQuantity());
        dto.setTotalVolume(stock.getQuantity() * stock.getProduct().getVolume());
        dto.setTotalWeight(stock.getQuantity() * stock.getProduct().getWeight());
        return dto;
    }

}
