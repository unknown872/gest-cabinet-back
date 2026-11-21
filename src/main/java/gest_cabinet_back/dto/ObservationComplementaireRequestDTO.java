package gest_cabinet_back.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ObservationComplementaireRequestDTO {
    @NotBlank(message = "L'observation complémentaire ne peut pas être vide.")
    private String observation;
}
