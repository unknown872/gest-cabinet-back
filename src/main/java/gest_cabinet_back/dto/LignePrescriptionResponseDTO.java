package gest_cabinet_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LignePrescriptionResponseDTO {
    private Long id;
    private String medicament;
    private String posologie;
    private String dureeTraitement;
    private String instructions;
}
