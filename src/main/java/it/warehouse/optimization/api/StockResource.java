package it.warehouse.optimization.api;


import it.warehouse.optimization.dto.SimpleResultDTO;
import it.warehouse.optimization.dto.stock.InsertStockDTO;
import it.warehouse.optimization.service.StockService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "API Stock")
@Path("stockage")
@Slf4j

public class StockResource {


    @Inject
    StockService stockService;

    @POST
    @Operation(
            summary = "Create Stock.",
            description = "API create new Stock for specified warehouse. Use this API only when product be sent from external society."
    )
    public SimpleResultDTO<Long> insertStock(
            @Valid InsertStockDTO dto
            ){
        log.info("StockResource - insertStock");
        return SimpleResultDTO.<Long>builder()
                .payload(stockService.createStock(dto))
                .message("Stock inserted successfully")
                .build();
    }




}
