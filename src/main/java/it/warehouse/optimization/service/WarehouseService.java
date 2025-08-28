package it.warehouse.optimization.service;


import io.ebean.Database;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.Transaction;
import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.region.DetailRegionDTO;
import it.warehouse.optimization.dto.search.WarehouseSearchRequest;
import it.warehouse.optimization.dto.warehouse.BaseDetailWarehouseDTO;
import it.warehouse.optimization.dto.warehouse.InsertWarehouseDTO;
import it.warehouse.optimization.exception.ServiceException;
import it.warehouse.optimization.model.Product;
import it.warehouse.optimization.model.Region;
import it.warehouse.optimization.model.Warehouse;
import it.warehouse.optimization.model.enumerator.StockAction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@ApplicationScoped
@Slf4j

public class WarehouseService {

    @Inject
    Database db;

    private static final String DEFAULT_SORT ="name ASC, city.name ASC";

    public UUID createWarehouse(InsertWarehouseDTO dto){
        log.info("createWarehouse: Starting creating new warehouse record with warehouse name: {}",dto.getName());

        try(Transaction tx = db.beginTransaction()) {
            Warehouse warehouse = dto.toEntity();
            warehouse.insert(tx);
            tx.commit();
            return warehouse.getId();
        } catch(Exception e){
            log.error("createWarehouse: An error occurred while creating a new warehouse record. Error message: {}", e.getMessage());
            throw new ServiceException("An error occurred while inserting a new warehouse. Please try again later.");
        }
    }

    public PagedResultDTO<BaseDetailWarehouseDTO> findWarehouse(WarehouseSearchRequest request){
        log.info("findWarehouse: Starting find warehouse list");
        ExpressionList<Warehouse> query = db.find(Warehouse.class)
                .setLabel("FindWarehouse")
                .where();
        request.filterBuilder(query);

        request.pagination(query,DEFAULT_SORT);

        PagedList<Warehouse> list = query.findPagedList();
        return PagedResultDTO.of(list, BaseDetailWarehouseDTO::of);
    }


    public void updateWarehouseCapacityNoTransaction(UUID warehouseId, double productWeight, double productVolume, StockAction action, Transaction tx){
        Warehouse warehouse = getWarehouseByIdOrThrow(warehouseId);
        warehouse.setAvailableVolume(action.apply(productVolume));
        warehouse.setAvailableWeight(action.apply(productWeight));
        warehouse.update(tx);
    }

    public void checkWarehouseCapacity(Warehouse warehouse, Product product, int quantity) {
        double totalVolume = product.getVolume() * quantity;
        double totalWeight = product.getWeight() * quantity;
        boolean exceedVolumeCapacity = totalVolume > warehouse.getAvailableVolume();
        boolean exceedWeightCapacity = totalWeight > warehouse.getAvailableWeight();

        if (exceedVolumeCapacity) {
            log.error("checkWarehouseCapacity: Warehouse volume capacity exceeded for product {} (total volume: {}, warehouse capacity: {})",
                    product.getName(), totalVolume, warehouse.getAvailableVolume());
            throw new ServiceException("Error while adding stock: insufficient volume capacity for the selected product. Please try again.");
        }

        if (exceedWeightCapacity) {
            log.error("checkWarehouseCapacity: Warehouse weight capacity exceeded for product {} (total weight: {}, warehouse capacity: {})",
                    product.getName(), totalWeight, warehouse.getAvailableWeight());
            throw new ServiceException("Error while adding stock: insufficient weight capacity for the selected product. Please try again.");
        }

    }


    public Warehouse getWarehouseByIdOrThrow (UUID id){
        return db.find(Warehouse.class)
                .setLabel("GetWarehouseById")
                .where()
                .idEq(id)
                .findOneOrEmpty()
                .orElseThrow(()-> {
                    log.error("getWarehouseByIdOrThrow: Error warehouse with id :{} , not exist!", id);
                    return new ServiceException(" Error warehouse not exist.");
                });
    }

}
