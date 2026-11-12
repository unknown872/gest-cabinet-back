package gest_cabinet_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRequestDTO {
    private String motif;
    private String observations;
    private String diagnostic;
    private Double tension;
    private Double poids;
    private Double temperature;
}
