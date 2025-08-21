package it.warehouse.optimization.dto.region;


import it.warehouse.optimization.model.Region;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class DetailRegionDTO {

    private Long id;
    private String name;
    private String istatCode;


    public static DetailRegionDTO of (Region region){
        DetailRegionDTO dto = new DetailRegionDTO();
        dto.setId(region.getId());
        dto.setName(region.getName());
        dto.setIstatCode(region.getIstatCode());
        return dto;
    }


}
