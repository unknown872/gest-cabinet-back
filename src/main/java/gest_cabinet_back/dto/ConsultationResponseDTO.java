package gest_cabinet_back.dto;

import gest_cabinet_back.model.StatutConsultation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationResponseDTO {
    private Long id;
    private LocalDateTime dateHeure;
    private String motif;
    private String observations;
    private String diagnostic;
    private Double tension;
    private Double poids;
    private Double temperature;
    private StatutConsultation statut;

    private Long rendezVousId;
    private String nomMedecin;
    private String prenomMedecin;
}
