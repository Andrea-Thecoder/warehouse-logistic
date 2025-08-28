package it.warehouse.optimization.api;


import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.SimpleResultDTO;
import it.warehouse.optimization.dto.search.WarehouseSearchRequest;
import it.warehouse.optimization.dto.warehouse.BaseDetailWarehouseDTO;
import it.warehouse.optimization.dto.warehouse.InsertWarehouseDTO;
import it.warehouse.optimization.service.WarehouseService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "API Warehouses")
@Path("warehouses")
@Slf4j
public class WarehouseResource {

    @Inject
    WarehouseService warehouseService;


    @POST
    @Operation(
            summary = "Insert Warehouse",
            description = "API for insert a new Warehouse on database."
    )
    public SimpleResultDTO<UUID> insertWarehouse(
           @Valid InsertWarehouseDTO dto
    ){
        log.info("WarehouseResource - insertWarehouse");
        return SimpleResultDTO.<UUID>builder()
                .payload(warehouseService.createWarehouse(dto))
                .message("Warehouse inserted successfully")
                .build();
    }


    @GET
    @Operation(
            summary = "Find Warehouses.",
            description = "API find all Warehouse on database. Can apply advanced filter by name, city name or min/max volume or weight capacity."
    )
    public PagedResultDTO<BaseDetailWarehouseDTO> findAllWarehouse(
            @BeanParam @Valid  WarehouseSearchRequest request
    ){
        log.info("WarehouseResource - findAllWarehouse");
        return warehouseService.findWarehouse(request);
    }





}
