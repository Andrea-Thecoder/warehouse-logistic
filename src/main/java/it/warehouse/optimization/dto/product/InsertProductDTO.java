package it.warehouse.optimization.dto.product;

import it.warehouse.optimization.model.Product;
import it.warehouse.optimization.model.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class InsertProductDTO {

    @NotBlank(message = "Product name must be valorized.")
    private String name;

    @NotNull(message = "Product volume must be valorized.")
    @Positive(message = "Product volume must be a positive value.")
    private Double volume;

    @NotNull(message = "Product volume must be valorized.")
    @Positive(message = "Product volume must be a positive value.")
    private Double weight;

    @NotBlank(message = "Product name must be valorized.")
    private String categoryId;


    public Product toEntity(){
        Product product = new Product();
        toUpdate(product);
        return product;
    }

    public void toUpdate(Product product){
        product.setName(this.name);
        product.setVolume(this.volume);
        product.setWeight(this.weight);
        product.setCategory(product.db().reference(CategoryType.class,this.categoryId));
    }
}
