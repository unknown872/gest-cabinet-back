package gest_cabinet_back.dto;

import gest_cabinet_back.model.StatutPrescription;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponseDTO {
    private Long id;
    private Long consultationId;
    private StatutPrescription statut;
    private LocalDateTime dateEmission;
    private LocalDate dateExpiration;
    private String instructionsGenerales;
    private String pdfUrl;
    private List<LignePrescriptionResponseDTO> lignes = new ArrayList<>();

    // Médecin prescripteur
    private Long medecinId;
    private String nomMedecin;
    private String prenomMedecin;
    private String specialiteMedecin;

    // Patient
    private Long patientId;
    private String nomPatient;
    private String prenomPatient;
    private String numeroDossier;
}
