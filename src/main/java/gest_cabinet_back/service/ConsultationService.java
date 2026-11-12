package gest_cabinet_back.service;

import gest_cabinet_back.dto.ConsultationMedecinResponseDTO;
import gest_cabinet_back.dto.ConsultationRequestDTO;
import gest_cabinet_back.dto.ConsultationResponseDTO;
import gest_cabinet_back.dto.ObservationComplementaireRequestDTO;
import gest_cabinet_back.model.StatutConsultation;

import java.util.List;

public interface ConsultationService {

    // --- Actions MEDECIN ---

    ConsultationMedecinResponseDTO demarrerConsultation(Long rendezVousId, ConsultationRequestDTO dto, Long medecinId);

    ConsultationMedecinResponseDTO modifierConsultation(Long consultationId, ConsultationRequestDTO dto, Long medecinId);

    ConsultationMedecinResponseDTO cloturerConsultation(Long consultationId, Long medecinId);

    ConsultationMedecinResponseDTO annulerConsultation(Long consultationId, Long medecinId);

    ConsultationMedecinResponseDTO ajouterObservationComplementaire(
            Long consultationId, ObservationComplementaireRequestDTO dto, Long medecinId);

    List<ConsultationMedecinResponseDTO> listerConsultationsDuMedecin(Long medecinId, StatutConsultation statut);

    ConsultationMedecinResponseDTO consulterConsultationMedecin(Long consultationId, Long medecinId);

    List<ConsultationMedecinResponseDTO> listerConsultationsParPatient(Long patientId, Long medecinId);

    // --- Actions PATIENT ---

    List<ConsultationResponseDTO> listerMesConsultations(Long patientId);

    ConsultationResponseDTO consulterMaConsultation(Long consultationId, Long patientId);
}
