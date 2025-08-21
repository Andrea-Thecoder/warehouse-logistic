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

public class MovementSearchRequest extends  BaseSearchRequest{

    @QueryParam("search")
    private String search;

    @QueryParam("product")
    private String productName;

    @QueryParam("origin-warehouse")
    private String originWarehouseName;

    @QueryParam("destination-warehouse")
    private String destinationWarehouseName;

    @QueryParam("origin-city")
    private String originCity;

    @QueryParam("destination-city")
    private String destinationCity;



    public <T> void filterBuilder(ExpressionList<T> query){

        if(StringUtils.isNotBlank(originWarehouseName)){
            query.ilike("originWarehouse.name","%"+originWarehouseName.trim()+"%");
        }
        if(StringUtils.isNotBlank(destinationWarehouseName)){
            query.ilike("destinationWarehouse.name","%"+destinationWarehouseName.trim()+"%");
        }

        if(StringUtils.isNotBlank(originCity)){
            query.ilike("originWarehouse.city.name","%"+originCity.trim()+"%");
        }
        if(StringUtils.isNotBlank(destinationCity)){
            query.ilike("destinationWarehouse.city.name","%"+destinationCity.trim()+"%");
        }

        if(StringUtils.isNotBlank(productName)){
            query.ilike("product.name","%"+productName.trim()+"%");
        }

        if(StringUtils.isNotBlank(search)){
            String searchFormatted = "%"+search.trim()+"%";
            query.or()
                    .ilike("originWarehouse.name","%"+searchFormatted.trim()+"%")
                    .ilike("destinationWarehouse.name","%"+searchFormatted.trim()+"%")
                    .ilike("originWarehouse.city.name","%"+searchFormatted.trim()+"%")
                    .ilike("destinationWarehouse.city.name","%"+searchFormatted.trim()+"%")
                    .ilike("product.name","%"+searchFormatted.trim()+"%")
            .endOr();
        }







    }
}
