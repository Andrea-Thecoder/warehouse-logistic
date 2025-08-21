package it.warehouse.optimization.api;


import it.warehouse.optimization.dto.LookupDetailDTO;
import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.search.SearchRequest;
import it.warehouse.optimization.service.LookupService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "API Lookup")
@Path("lookup")
@Slf4j

public class LookupResource {

    @Inject
    LookupService lookupService;

    @GET
    @Path("category-type")
    @Operation(
            summary = "Find Movement type.",
            description = "API find all Movement type."
    )
    public PagedResultDTO<LookupDetailDTO> findCategoryType (
            @BeanParam @Valid SearchRequest request
    ){
        log.info("LookupResource - findCategoryType");
        return lookupService.findCategoryType(request);
    }

}
