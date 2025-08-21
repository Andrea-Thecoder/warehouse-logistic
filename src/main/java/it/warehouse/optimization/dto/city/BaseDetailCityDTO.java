package it.warehouse.optimization.dto.city;

import it.warehouse.optimization.model.City;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class BaseDetailCityDTO {

    private Long id;
    private String name;
    private String code;

    public static BaseDetailCityDTO of(City city){
        BaseDetailCityDTO dto = new BaseDetailCityDTO();
        dto.setId(city.getId());
        dto.setName(city.getName());
        dto.setCode(city.getIstatCode());
        return dto;
    }


}
