package it.warehouse.optimization.dto.product;

import it.warehouse.optimization.model.Product;
import it.warehouse.optimization.model.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class BaseDetailProductDTO {

    protected UUID id;
    protected String name;
    protected Double volume;
    protected Double weight;
    protected CategoryType category;

    public static BaseDetailProductDTO of (Product product){
        BaseDetailProductDTO dto = new BaseDetailProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setVolume(product.getVolume());
        dto.setWeight(product.getWeight());
        dto.setCategory(product.getCategory());
        return dto;
    }
}
