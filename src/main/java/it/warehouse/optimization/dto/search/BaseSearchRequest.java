package it.warehouse.optimization.dto.search;

import io.ebean.ExpressionList;
import jakarta.validation.constraints.Min;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter

public class BaseSearchRequest {
    @QueryParam("page")
    @DefaultValue("1")
    @Min(1)
    protected int page;

    @QueryParam("size")
    @DefaultValue("20")
    @Min(1)
    protected int size;

    @QueryParam("sort")
    protected String sort;

    @QueryParam("descending")
    protected boolean descending;


    public <T> void pagination(ExpressionList<T> query,String defaultSort){
        int rows = this.getSize();
        int offset = (this.getPage() - 1) * rows;
        String order = this.getSort();
        String direction = descending ? " asc" : " desc";
        if(StringUtils.isNotBlank(order)){
            query.orderBy(order + direction);
        }else if (StringUtils.isNotBlank(defaultSort)) {
            query.orderBy(defaultSort);
        } else query.orderById(true);
        query.setFirstRow(offset).setMaxRows(rows);
    }

    public <T> void pagination(ExpressionList<T> query){
        int rows = this.getSize();
        int offset = (this.getPage() - 1) * rows;
        query.setFirstRow(offset).setMaxRows(rows);
    }


}
