package it.warehouse.optimization.dto.stock;

import it.warehouse.optimization.model.Product;
import it.warehouse.optimization.model.Stock;
import it.warehouse.optimization.model.Warehouse;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class InsertStockDTO {

    @NotNull(message = "Warehouse id must be valorized.")
    private UUID warehouseId;

    @NotNull(message = "Product id must be valorized.")
    private UUID productId;

    @NotNull(message = "Quantity must be valorized.")
    private Integer quantity;


    public Stock toEntity (){
        Stock stock = new Stock();
        stock.setWarehouse(stock.db().reference(Warehouse.class,warehouseId));
        stock.setProduct(stock.db().reference(Product.class,productId));
        stock.setQuantity(quantity);
        return stock;

    }



}
