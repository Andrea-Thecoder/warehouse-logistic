package it.warehouse.optimization.api;


import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.SimpleResultDTO;
import it.warehouse.optimization.dto.search.AdvancedStockSearchRequest;
import it.warehouse.optimization.dto.search.StockSearchRequest;
import it.warehouse.optimization.dto.stock.DetailStockDTO;
import it.warehouse.optimization.dto.stock.DetailStockWarehouseDTO;
import it.warehouse.optimization.dto.stock.InsertStockDTO;
import it.warehouse.optimization.dto.stock.UpdateQuantityStockDTO;
import it.warehouse.optimization.service.StockService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
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

    @GET
    @Operation(
            summary = "Find Stock",
            description = "API for find all Stock on database. Can apply advanced filter."
    )
    public PagedResultDTO<DetailStockDTO> findAllStock(
            @BeanParam @Valid StockSearchRequest request
            ){
        log.info("StockResource - findAllStock");
        return  stockService.findAllStock(request);
    }

    @GET
    @Path("/warehouses/{warehouseId}/stocks")
    @Operation(
            summary = "Get Stock by Warehouse",
            description = "API for get all Stock for a specify warehouse. Can apply advanced filter."
    )
    public PagedResultDTO<DetailStockWarehouseDTO> getStockByWarehouse(
            @PathParam("warehouseId") UUID warehouseId,
            @BeanParam @Valid AdvancedStockSearchRequest request
    ){
        log.info("StockResource - getStockByWarehouse");
        return  stockService.getStocksByWarehouse(warehouseId,request);
    }

   /* @PUT
    @Path("/{stockId}/add-quantity")
    @Operation(
            summary = "Increase quantity for a Stock.",
            description = "API for increase the quantity for a Stock."
    )
    public SimpleResultDTO<Long> increaseQuantity(
            @PathParam("stockId") Long stockId,
            @BeanParam @Valid UpdateQuantityStockDTO dto
            ){
        log.info("StockResource - increaseQuantity");
        return SimpleResultDTO.<Long>builder()
                .payload(stockService.increaseQuantityStock(stockId,dto.getQuantity()))
                .message("Stock quantity increased successfully")
                .build();
    }*/

  /*  @PUT
    @Path("/{stockId}/subtract-quantity")
    @Operation(
            summary = "Subtract quantity for a Stock.",
            description = "API for subtract the quantity for a Stock."
    )
    public SimpleResultDTO<Long> decreaseQuantity(
            @PathParam("stockId") Long stockId,
            @BeanParam @Valid UpdateQuantityStockDTO dto
    ){
        log.info("StockResource - decreaseQuantity");
        return SimpleResultDTO.<Long>builder()
                .payload(stockService.decreaseQuantityStock(stockId,dto.getQuantity()))
                .message("Stock quantity decreased successfully")
                .build();
    }*/






}
