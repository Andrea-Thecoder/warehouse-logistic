package it.warehouse.optimization.service;

import io.ebean.Database;
import io.ebean.Transaction;
import it.warehouse.optimization.dto.stock.InsertStockDTO;
import it.warehouse.optimization.exception.ServiceException;
import it.warehouse.optimization.model.Product;
import it.warehouse.optimization.model.Stock;
import it.warehouse.optimization.model.Warehouse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    private static final String  INCOMING = "INCOMING";
    private static final String  OUTGOING = "OUTGOING";



    public Long createStock(InsertStockDTO dto){
        log.info("createStock: Starting creating new Stock track for product id: {}", dto.getProductId());

        try(Transaction tx = db.beginTransaction()) {
            Warehouse warehouse = warehouseService.getWarehouseByIdOrThrow(dto.getWarehouseId());
            Product product = productService.getProductByIdOrThrow(dto.getProductId());

            checkWarehouseCapacity(warehouse,product,dto.getQuantity());

            Stock stock = getStockByWarehouseAndProduct(warehouse.getId(),product.getId());
            if (stock == null){
                stock = dto.toEntity();
                stock.insert(tx);
            } else {
                stock.setQuantity(stock.getQuantity() + dto.getQuantity());
                stock.update(tx);
            }
            tx.commit();
            return stock.getId();

        }catch(Exception e){
            log.error("createStock: An error occurred while creating a new stock record. Error message: {}", e.getMessage());
            throw new ServiceException("An error occurred while inserting a new stock. Please try again later.");
        }

    }

    public void decrementStock(Warehouse warehouse,Product product, int requestedQuantity,Transaction tx){
        Stock stock = getStockByWarehouseAndProduct(warehouse.getId(),product.getId());
        int newQuantity = stock.getQuantity() -requestedQuantity ;
        stock.setQuantity(newQuantity);
        stock.update(tx);
    }

    public void checkStockAvailability(Warehouse warehouse,Product product, int requestedQuantity){
        Stock stock = getStockByWarehouseAndProduct(warehouse.getId(),product.getId());
        if(stock == null  ||  requestedQuantity > stock.getQuantity()){
            int available = stock != null ? stock.getQuantity() : 0;
            log.error("checkStockAvailability: Requested quantity ({}) exceeds available stock ({}) in warehouse {} for product {}.",
                    requestedQuantity, available, warehouse.getName(), product.getName());
            throw new ServiceException(
                    "Requested quantity exceeds available stock. Available: " + available + ", Requested: " + requestedQuantity);
        }
    }

    public void checkWarehouseCapacity(Warehouse warehouse,Product product, int quantity ){
        double totalVolume = product.getVolume() * quantity;
        double totalWeight = product.getWeight() * quantity;
        boolean exceedVolumeCapacity = totalVolume >= warehouse.getVolumeCapacity();
        boolean exceedWeightCapacity = totalWeight >= warehouse.getWeightCapacity();

        if(exceedVolumeCapacity) {
            log.error("checkWarehouseCapacity: Warehouse volume capacity exceeded for product {} (total volume: {}, warehouse capacity: {})",
                    product.getName(), totalVolume, warehouse.getVolumeCapacity());
            throw new ServiceException("Error while adding stock: insufficient volume capacity for the selected product. Please try again.");
        }

        if (exceedWeightCapacity) {
            log.error("checkWarehouseCapacity: Warehouse weight capacity exceeded for product {} (total weight: {}, warehouse capacity: {})",
                    product.getName(), totalWeight, warehouse.getWeightCapacity());
            throw new ServiceException("Error while adding stock: insufficient weight capacity for the selected product. Please try again.");
        }

    }


    private Stock getStockByWarehouseAndProduct(UUID warehouseId, UUID productId){
        return db.find(Stock.class)
                .setLabel("GetStockByWarehouseAndProduct")
                .where()
                .eq("warehouse.id",warehouseId)
                .eq("product.id", productId)
                .findOne();
    }

    /*public void createNewStockNoTransaction(Stock stock, )*/


}
