package it.warehouse.optimization.dto.search;

import io.ebean.ExpressionList;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

@Getter
@Setter

public final class AdvancedStockSearchRequest extends StockSearchRequest {

    @QueryParam("category")
    private String categoryName;


    @Override
    public <T> void filterBuilder(ExpressionList<T> query){
        if(StringUtils.isNotBlank(productName)){
            query.ilike("product.name","%"+productName.trim()+"%");
        }
        if(StringUtils.isNotBlank(warehouseName)){
            query.ilike("warehouse.name","%"+warehouseName.trim()+"%");
        }
        if(StringUtils.isNotBlank(categoryName)){
            query.eq("product.category", categoryName.toUpperCase(Locale.ROOT));
        }
    }
}
