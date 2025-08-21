package it.warehouse.optimization.utils;

import io.ebean.ExpressionList;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class GenericUtils {

    public static String cleanerString(String stringToClean, String regex){
        if(stringToClean == null && regex == null) return null;
        if(StringUtils.isBlank(stringToClean) || regex == null) return stringToClean;
        return stringToClean.replaceAll(regex,"");
    }

    public static <T>void betweenQueryBuilder(ExpressionList<T> query, Double minValue, Double maxValue,String columnName){
        if (minValue != null && maxValue != null) {
            query.between(columnName, minValue, maxValue);
        } else if (minValue != null) {
            query.ge(columnName, minValue);
        } else if (maxValue != null) {
            query.le(columnName, maxValue);
        }
    }


}
