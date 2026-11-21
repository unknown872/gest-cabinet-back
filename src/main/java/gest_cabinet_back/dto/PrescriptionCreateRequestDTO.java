package gest_cabinet_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionCreateRequestDTO {
    private String instructionsGenerales;
    private LocalDate dateExpiration;
    private List<LignePrescriptionRequestDTO> lignes;
}
