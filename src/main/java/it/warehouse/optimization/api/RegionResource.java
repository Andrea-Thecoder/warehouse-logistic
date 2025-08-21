package it.warehouse.optimization.api;

import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.region.DetailRegionDTO;
import it.warehouse.optimization.dto.search.LocationSearchRequest;
import it.warehouse.optimization.dto.search.SearchRequest;
import it.warehouse.optimization.service.RegionService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "API Regions")
@Path("regions")
@Slf4j
public class RegionResource {


    @Inject
    RegionService regionService;


    @GET
    @Operation(
            summary = "Find Regions.",
            description = "API find all Regions on database. Can apply filter by region name or istat code"
    )
    public PagedResultDTO<DetailRegionDTO> findAllRegions(
            @BeanParam @Valid LocationSearchRequest request
    ){
        log.info("RegionResource - findAllRegions");
        return regionService.findRegions(request);
    }

}
