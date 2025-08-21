package it.warehouse.optimization.dto;


import it.warehouse.optimization.model.BasicType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class LookupDetailDTO {

    private String id;
    private String description;

    public static <T extends BasicType> LookupDetailDTO of (T lookupType){
        LookupDetailDTO dto = new LookupDetailDTO();
        dto.setId(lookupType.getId());
        dto.setDescription(lookupType.getDescription());
        return dto;
    }
}
