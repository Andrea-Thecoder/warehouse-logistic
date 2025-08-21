package it.warehouse.optimization.dto.search;


import io.ebean.ExpressionList;
import io.quarkus.rest.client.reactive.ClientQueryParams;
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

public class ProductSearchRequest extends  BaseSearchRequest{

    @QueryParam("name")
    private String productName;

    @QueryParam("category")
    private String categoryId;

    public <T> void filterBuilder(ExpressionList<T> query){
        if(StringUtils.isNotBlank(productName)){
            query.ilike("name","%"+productName.trim()+"%");
        }
        if(StringUtils.isNotBlank(categoryId)){
            query.ieq("category.id",productName.trim());
        }
    }

}
