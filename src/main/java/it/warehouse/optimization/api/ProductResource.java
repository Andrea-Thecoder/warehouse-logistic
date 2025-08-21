package it.warehouse.optimization.api;

import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.SimpleResultDTO;
import it.warehouse.optimization.dto.product.BaseDetailProductDTO;
import it.warehouse.optimization.dto.product.InsertProductDTO;
import it.warehouse.optimization.dto.search.ProductSearchRequest;
import it.warehouse.optimization.service.ProductService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;

@Tag(name = "API Products")
@Path("products")
@Slf4j
public class ProductResource {

    @Inject
    ProductService productService;


    @POST
    @Operation(
            summary = "Insert Product.",
            description = "API for insert a new product."
    )
    public SimpleResultDTO<UUID> insertProduct(
            @Valid InsertProductDTO dto
            ){
        log.info("ProductResource - insertProduct");
        return SimpleResultDTO.<UUID>builder()
                .payload(productService.createProduct(dto))
                .message("Product inserted successfully")
                .build();
    }

    @GET
    @Operation(
            summary = "Find Products.",
            description = "API find all  Products. Can apply filter by name and category."
    )
    public PagedResultDTO<BaseDetailProductDTO> findlAllProducts(
            @BeanParam @Valid ProductSearchRequest request
            ){
        log.info("ProductResource - findlAllProducts");
        return productService.findProducts(request);
    }



    @GET
    @Path("{id}")
    @Operation(
            summary = "Find Product by ID",
            description = "API find product by their ID."
    )
    public BaseDetailProductDTO getProduct(
            @PathParam("id") UUID id
    ){
        log.info("ProductResource - getProduct");
        return productService.getProductById(id);
    }


    @PUT
    @Path("{id}")
    @Operation(
            summary = "Update the Product.",
            description = "API for Update the Product by their ID."
    )
    public SimpleResultDTO<UUID> updateProduct(
            @PathParam("id") UUID id,
            @Valid InsertProductDTO dto
    ){
        log.info("ProductResource - updateProduct");
        return SimpleResultDTO.<UUID>builder()
                .payload(productService.updateProductById(id,dto))
                .message("Product updated successfully")
                .build();
    }


    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Delete Product by ID",
            description = "API for Deleting Product by their ID."
    )
    public SimpleResultDTO<UUID> deleteProduct(
            @PathParam("id") UUID id
    ){
        log.info("ProductResource - deleteProduct");
        return SimpleResultDTO.<UUID>builder()
                .payload(productService.deleteProduct(id))
                .message("Product deleted successfully")
                .build();
    }






}
