package it.warehouse.optimization.dto.search;

import io.ebean.ExpressionList;
import it.warehouse.optimization.utils.GenericUtils;
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

public final class WarehouseSearchRequest  extends  BaseSearchRequest{

    @QueryParam("name")
    private String warehouseName;

    @QueryParam("city")
    private String cityName;

    @QueryParam("min-volume")
    @Positive(message = "min volume capacity must be a positive value")
    private Double minVolumeCapacity;

    @QueryParam("max-volume")
    @Positive(message = "max volume capacity must be a positive value")
    private Double maxVolumeCapacity;

    @QueryParam("min-weight")
    @Positive(message = "min weight capacity must be a positive value")
    private Double minWeightCapacity;

    @QueryParam("max-weight")
    @Positive(message = "max weight capacity must be a positive value")
    private Double maxWeightCapacity;

    public <T> void filterBuilder(ExpressionList<T> query){
        if(StringUtils.isNotBlank(warehouseName)){
            query.ilike("name","%"+warehouseName.trim()+"%");
        }
        if(StringUtils.isNotBlank(cityName)){
            String searchPattern = "%"+cityName.trim()+"%";
            query.or()
                    .ilike("city.name",searchPattern)
                    .ilike("city.istat_code",searchPattern)
                    .endOr();
        }
        GenericUtils.betweenQueryBuilder(query,minVolumeCapacity,maxVolumeCapacity,"volumeCapacity");
        GenericUtils.betweenQueryBuilder(query,minWeightCapacity,maxWeightCapacity,"weightCapacity");
    }


}
