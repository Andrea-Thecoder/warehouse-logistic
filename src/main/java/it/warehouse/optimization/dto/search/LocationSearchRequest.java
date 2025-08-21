package it.warehouse.optimization.dto.search;

import io.ebean.ExpressionList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor

public class LocationSearchRequest extends  SearchRequest{

    public <T> void filterBuilder(ExpressionList<T> query){
        if (StringUtils.isNotBlank(search)){
            String searchPattern = "%" + search.trim() + "%";
            query.or()
                    .ilike("name",searchPattern)
                    .ilike("istat_code",searchPattern)
                    .endOr();
        }
    }
}
