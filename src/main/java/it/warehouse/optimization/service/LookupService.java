package it.warehouse.optimization.service;

import io.ebean.Database;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import it.warehouse.optimization.dto.LookupDetailDTO;
import it.warehouse.optimization.dto.PagedResultDTO;
import it.warehouse.optimization.dto.search.SearchRequest;
import it.warehouse.optimization.model.CategoryType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class LookupService {


    @Inject
    Database db;

    public PagedResultDTO<LookupDetailDTO> findCategoryType(SearchRequest request) {
        log.info("findMovementType: Starting find findCategoryType type list");
        ExpressionList<CategoryType> query = db.find(CategoryType.class)
                .setLabel("FindCategoryType")
                .where();
        request.filterBuilder(query, "id");

        request.pagination(query, "");

        PagedList<CategoryType> list = query.findPagedList();
        return PagedResultDTO.of(list, LookupDetailDTO::of);
    }

}
