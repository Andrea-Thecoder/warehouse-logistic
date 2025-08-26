package it.warehouse.optimization.dto.search;

import io.ebean.ExpressionList;
import jakarta.validation.constraints.Positive;
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

public final class CitySearchRequest  extends  BaseSearchRequest{

    @QueryParam("city")
    private String cityName;

    @QueryParam("region")
    @Positive(message = "region id must be positive")
    private Long regionId;

    public <T> void filterBuilder(ExpressionList<T> query){
        if(regionId != null && regionId > 0){
            query.eq("region.id",regionId);
        }
        if (StringUtils.isNotBlank(cityName)){
            String searchPattern = "%" + cityName.trim() + "%";
            query.or()
                    .ilike("name",searchPattern)
                    .ilike("istat_code",searchPattern)
                    .endOr();
        }
    }
}
