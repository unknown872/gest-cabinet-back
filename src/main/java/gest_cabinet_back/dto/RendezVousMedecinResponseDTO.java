package gest_cabinet_back.dto;

import gest_cabinet_back.model.Genre;
import gest_cabinet_back.model.StatutRdv;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RendezVousMedecinResponseDTO {
    private Long id;
    private LocalDateTime dateHeure;
    private String motif;
    private String notes;
    private StatutRdv statut;

    // Informations du médecin du RDV
    private Long medecinId;
    private String nomMedecin;
    private String prenomMedecin;

    // Informations du patient
    private Long patientId;
    private String codePatient;
    private String nomPatient;
    private String prenomPatient;
    private String telephonePatient;
    private String emailPatient;
    private Genre genrePatient;
    private LocalDate dateNaissancePatient;
}
