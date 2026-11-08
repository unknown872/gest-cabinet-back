package gest_cabinet_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RendezVousRequestDTO {
    private Long medecinId;
    private LocalDateTime dateHeure;
    private String motif;
    private String notes;

    // Utilisé uniquement par le MEDECIN pour prendre un RDV au nom d'un patient
    private Long patientId;
}
