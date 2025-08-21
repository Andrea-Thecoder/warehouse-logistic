package it.warehouse.optimization.service;

import io.ebean.Database;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Transaction;
import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.product.BaseDetailProductDTO;
import it.warehouse.optimization.dto.product.InsertProductDTO;
import it.warehouse.optimization.dto.search.ProductSearchRequest;
import it.warehouse.optimization.exception.ServiceException;
import it.warehouse.optimization.model.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ApplicationScoped
@Slf4j

public class ProductService {

    @Inject
    Database db;

    public UUID createProduct(InsertProductDTO dto) {
        log.info("createProduct: Starting creating new product on database. Product name: {}", dto.getName());
        try (Transaction tx = db.beginTransaction()) {
            Product product = dto.toEntity();
            product.insert(tx);
            tx.commit();
            return product.getId();
        } catch (Exception e) {
            log.error("createProduct: Error occured while create new product. Error message: {}", e.getMessage());
            throw new ServiceException("Error occured while insert a new product. Please try again later.");
        }
    }

    private static final String DEFAULT_SORT = "name ASC, category.id ASC";

    public PagedResultDTO<BaseDetailProductDTO> findProducts(ProductSearchRequest request) {
        log.info("findProducts: Starting retrieving products list.");
        ExpressionList<Product> query = db.find(Product.class)
                .setLabel("FindProducts")
                .where();

        request.filterBuilder(query);
        request.pagination(query, DEFAULT_SORT);

        PagedList<Product> list = query.findPagedList();
        return PagedResultDTO.of(list, BaseDetailProductDTO::of);
    }

    public BaseDetailProductDTO getProductById(UUID id) {
        log.info("getProductById: Starting retrieving product with id: {}", id);
        return BaseDetailProductDTO.of(getProductByIdOrThrow(id));
    }

    public UUID updateProductById(UUID id, InsertProductDTO dto) {
        log.info("updateProductById: Starting updating product with id: {}", id);
        try (Transaction tx = db.beginTransaction()) {
            Product product = getProductByIdOrThrow(id);
            dto.toUpdate(product);
            product.update(tx);
            tx.commit();
            return product.getId();
        } catch (Exception e) {
            log.error("updateProductById: Error occured while update product. Error message: {}", e.getMessage());
            throw new ServiceException("Error occured while update the product. Please try again later.");
        }
    }

    public UUID deleteProduct(UUID id) {
        log.info("deleteProduct: Starting deleting product with id: {}", id);
        try (Transaction tx = db.beginTransaction()) {
            Product product = getProductByIdOrThrow(id);
            product.delete(tx);
            tx.commit();
            return product.getId();
        } catch (Exception e) {
            log.error("deleteProduct: Error occured while delete product. Error message: {}", e.getMessage());
            throw new ServiceException("Error occured while delete the product. Please try again later.");
        }
    }


    public Product getProductByIdOrThrow(UUID id) {
        return db.find(Product.class)
                .setLabel("GetProductById")
                .where()
                .idEq(id)
                .findOneOrEmpty()
                .orElseThrow(() -> {
                    log.error("getProductByIdOrThrow: Error product with id :{} , not exist!", id);
                    return new ServiceException(" Error product not exist.");
                });
    }


}
