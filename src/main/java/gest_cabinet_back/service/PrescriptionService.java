package gest_cabinet_back.service;

import gest_cabinet_back.dto.*;
import gest_cabinet_back.model.StatutPrescription;

import java.util.List;

public interface PrescriptionService {

    // --- Actions MEDECIN ---

    PrescriptionResponseDTO creerPrescription(Long consultationId, PrescriptionCreateRequestDTO dto, Long medecinId);

    PrescriptionResponseDTO modifierPrescription(Long prescriptionId, PrescriptionUpdateRequestDTO dto, Long medecinId);

    PrescriptionResponseDTO ajouterLigne(Long prescriptionId, LignePrescriptionRequestDTO dto, Long medecinId);

    PrescriptionResponseDTO modifierLigne(Long prescriptionId, Long ligneId, LignePrescriptionRequestDTO dto, Long medecinId);

    PrescriptionResponseDTO supprimerLigne(Long prescriptionId, Long ligneId, Long medecinId);

    PrescriptionResponseDTO validerPrescription(Long prescriptionId, Long medecinId);

    PrescriptionResponseDTO annulerPrescription(Long prescriptionId, Long medecinId);

    PrescriptionResponseDTO consulterPrescriptionMedecin(Long prescriptionId, Long medecinId);

    List<PrescriptionResponseDTO> listerPrescriptionsParConsultation(Long consultationId, Long medecinId);

    List<PrescriptionResponseDTO> listerPrescriptionsParPatient(Long patientId, Long medecinId);

    List<PrescriptionResponseDTO> listerPrescriptionsDuMedecin(Long medecinId);

    // --- Actions PATIENT ---

    List<PrescriptionResponseDTO> listerMesPrescriptions(Long patientId);

    PrescriptionResponseDTO consulterMaPrescription(Long prescriptionId, Long patientId);

    // --- Téléchargement Ordonnance PDF ---

    byte[] telechargerOrdonnancePdf(Long prescriptionId, Long utilisateurId, boolean isMedecin);
}
