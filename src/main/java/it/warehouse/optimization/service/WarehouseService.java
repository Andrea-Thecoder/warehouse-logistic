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
import it.warehouse.optimization.model.Region;
import it.warehouse.optimization.model.Warehouse;
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
