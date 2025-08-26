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

public sealed class StockSearchRequest extends  BaseSearchRequest permits AdvancedStockSearchRequest  {


    @QueryParam("product")
    protected String productName;

    @QueryParam("warehouse")
    protected String warehouseName;

    public <T> void filterBuilder(ExpressionList<T> query){
        if(StringUtils.isNotBlank(productName)){
            query.ilike("product.name","%"+productName.trim()+"%");
        }
        if(StringUtils.isNotBlank(warehouseName)){
            query.ilike("warehouse.name","%"+warehouseName.trim()+"%");
        }
    }

}
