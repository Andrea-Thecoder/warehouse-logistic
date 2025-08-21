package it.warehouse.optimization.service;

import io.ebean.Database;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.city.BaseDetailCityDTO;
import it.warehouse.optimization.dto.region.DetailRegionDTO;
import it.warehouse.optimization.dto.search.LocationSearchRequest;
import it.warehouse.optimization.model.City;
import it.warehouse.optimization.model.Region;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class RegionService {

    @Inject
    Database db;

    private static final String DEFAULT_SORT = "name ASC, istat_code ASC";

    public PagedResultDTO<DetailRegionDTO> findRegions (LocationSearchRequest request){
        log.info("findRegions: Starting find regions list");
        ExpressionList<Region> query = db.find(Region.class)
                .setLabel("FindRegions")
                .where();
        request.filterBuilder(query);

        request.pagination(query,DEFAULT_SORT);

        PagedList<Region> list = query.findPagedList();
        return PagedResultDTO.of(list,DetailRegionDTO::of);
    }

}
