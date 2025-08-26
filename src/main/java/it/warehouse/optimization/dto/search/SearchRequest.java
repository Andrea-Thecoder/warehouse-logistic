package it.warehouse.optimization.dto.search;

import io.ebean.ExpressionList;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public sealed class SearchRequest  extends BaseSearchRequest permits  LocationSearchRequest{

    @QueryParam("search")
    protected String search;

    public <T> void filterBuilder(ExpressionList<T> query, String searchColumn ){
        if (StringUtils.isNotBlank(search)){
            query.ilike(searchColumn,"% " + search.trim() +" %");
        }


    }

}
