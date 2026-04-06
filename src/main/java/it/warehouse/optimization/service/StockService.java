package it.warehouse.optimization.service;

import io.ebean.Database;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Transaction;
import io.smallrye.config.inject.ConfigProducerUtil;
import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.search.AdvancedStockSearchRequest;
import it.warehouse.optimization.dto.search.StockSearchRequest;
import it.warehouse.optimization.dto.stock.DetailStockDTO;
import it.warehouse.optimization.dto.stock.DetailStockWarehouseDTO;
import it.warehouse.optimization.dto.stock.InsertStockDTO;
import it.warehouse.optimization.exception.ServiceException;
import it.warehouse.optimization.model.Product;
import it.warehouse.optimization.model.Stock;
import it.warehouse.optimization.model.Warehouse;
import it.warehouse.optimization.model.enumerator.StockAction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ApplicationScoped
@Slf4j

public class StockService {


    @Inject
    Database db;

    @Inject
    WarehouseService warehouseService;

    @Inject
    ProductService productService;


    public Long createStock(InsertStockDTO dto) {
        log.info("createStock: Starting creating new Stock track for product id: {}", dto.getProductId());

        try (Transaction tx = db.beginTransaction()) {
            Warehouse warehouse = warehouseService.getWarehouseByIdOrThrow(dto.getWarehouseId());
            Product product = productService.getProductByIdOrThrow(dto.getProductId());

            warehouseService.checkWarehouseCapacity(warehouse, product, dto.getQuantity());

            Stock stock = getStockByWarehouseAndProduct(warehouse.getId(), product.getId());
            if (stock == null) {
                stock = dto.toEntity();
                stock.insert(tx);
            } else {
                stock.setQuantity(stock.getQuantity() + dto.getQuantity());
                stock.update(tx);
            }
            tx.commit();
            return stock.getId();

        } catch (Exception e) {
            log.error("createStock: An error occurred while creating a new stock record. Error message: {}", e.getMessage());
            throw new ServiceException("An error occurred while inserting a new stock. Please try again later.");
        }

    }


    public PagedResultDTO<DetailStockDTO> findAllStock(StockSearchRequest request) {
        ExpressionList<Stock> query = db.find(Stock.class)
                .setLabel("findAllStock")
                .where();

        request.filterBuilder(query);
        request.pagination(query);

        PagedList<Stock> stockList = query.findPagedList();
        return PagedResultDTO.of(stockList, DetailStockDTO::of);

    }

    public PagedResultDTO<DetailStockWarehouseDTO> getStocksByWarehouse(UUID warehouseId, AdvancedStockSearchRequest request) {
        ExpressionList<Stock> query = db.find(Stock.class)
                .setLabel("findAllStock")
                .where()
                .eq("warehouse.id", warehouseId);

        request.filterBuilder(query);
        request.pagination(query);

        PagedList<Stock> stockList = query.findPagedList();
        return PagedResultDTO.of(stockList, DetailStockWarehouseDTO::of);

    }


    public void increaseStock(Warehouse warehouse, Product product, int requestedQuantity, Transaction tx){
        Stock stock = getStockByWarehouseAndProduct(warehouse.getId(), product.getId());
        if(stock == null){
            InsertStockDTO dto = new InsertStockDTO();
            dto.setProductId(product.getId());
            dto.setWarehouseId(warehouse.getId());
            dto.setQuantity(0);
            stock = createStockNoTransaction(dto,tx);
        }
        warehouseService.updateWarehouseCapacityNoTransaction(
                warehouse.getId(),
                product.getWeight() * requestedQuantity,
                product.getVolume() * requestedQuantity,
                StockAction.INCREASE,
                tx);
        stock.setQuantity(stock.getQuantity() + requestedQuantity);
        stock.update(tx);
    }

    public void decrementStock(Warehouse warehouse, Product product, int requestedQuantity, Transaction tx) {
        Stock stock = getStockByWarehouseAndProduct(warehouse.getId(), product.getId());
        checkStockAvailability(stock,warehouse, product,requestedQuantity);
        warehouseService.updateWarehouseCapacityNoTransaction(
                warehouse.getId(),
                product.getWeight() * requestedQuantity,
                product.getVolume() * requestedQuantity,
                StockAction.DECREASE,
                tx);
        stock.setQuantity(stock.getQuantity() - requestedQuantity);
        stock.update(tx);
    }

    public void checkStockAvailability(Stock stock,Warehouse warehouse, Product product, int requestedQuantity) {
        if (stock == null || requestedQuantity > stock.getQuantity()) {
            int available = stock != null ? stock.getQuantity() : 0;
            log.error("checkStockAvailability: Requested quantity ({}) exceeds available stock ({}) in warehouse {} for product {}.",
                    requestedQuantity, available, warehouse.getName(), product.getName());
            throw new ServiceException(
                    "Requested quantity exceeds available stock. Available: " + available + ", Requested: " + requestedQuantity);
        }
    }


    private Stock getStockByWarehouseAndProduct(UUID warehouseId, UUID productId) {
        return db.find(Stock.class)
                .setLabel("GetStockByWarehouseAndProduct")
                .where()
                .eq("warehouse.id", warehouseId)
                .eq("product.id", productId)
                .findOne();
    }

    private Stock getStockByIdOrThrow(Long id){
        return db.find(Stock.class)
                .setLabel("getStockByIdOrThrow")
                .where()
                .idEq(id)
                .findOneOrEmpty()
                .orElseThrow(()->{
                    log.error("getStockByIdOrThrow: Error while retrieving Stock with ID: {}",id);
                    return new ServiceException("Error while retrieving Stock. Please try again later.");
                });
    }

    private Stock createStockNoTransaction( InsertStockDTO dto,Transaction tx){
        Stock stock = dto.toEntity();
        stock.insert(tx);
        return stock;
    }

}
