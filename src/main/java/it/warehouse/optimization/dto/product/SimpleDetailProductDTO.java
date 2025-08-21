package it.warehouse.optimization.dto.product;

import it.warehouse.optimization.model.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class SimpleDetailProductDTO {

    private UUID id;
    private String name;
    
    public static SimpleDetailProductDTO of (Product product){
        SimpleDetailProductDTO dto = new SimpleDetailProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        return dto;
    }
}
