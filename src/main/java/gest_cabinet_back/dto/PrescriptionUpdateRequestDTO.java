package gest_cabinet_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionUpdateRequestDTO {
    private String instructionsGenerales;
    private LocalDate dateExpiration;
}
