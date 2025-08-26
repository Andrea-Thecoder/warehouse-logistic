package it.warehouse.optimization.dto.stock;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public final class UpdateQuantityStockDTO {

    @QueryParam("quantity")
    @NotNull(message = "Quantity must be valorized.")
    @Positive(message = "Quantity must be a positive value")
    private Integer quantity;
}


