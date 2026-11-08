package gest_cabinet_back.service;

import gest_cabinet_back.dto.RendezVousMedecinResponseDTO;
import gest_cabinet_back.dto.RendezVousRequestDTO;
import gest_cabinet_back.dto.RendezVousResponseDTO;

import java.util.List;

public interface RendezVousService {

    // --- Actions PATIENT ---
    RendezVousResponseDTO demanderRdv(RendezVousRequestDTO dto, Long patientId);
    List<RendezVousResponseDTO> listerMesRdv(Long patientId);
    RendezVousResponseDTO consulterRdv(Long rdvId, Long patientId);
    void annulerRdv(Long rdvId, Long patientId);

    // --- Actions MEDECIN ---
    RendezVousMedecinResponseDTO prendreRdvPourPatient(RendezVousRequestDTO dto, Long medecinId);
    List<RendezVousMedecinResponseDTO> listerRdvDuMedecin(Long medecinId);
    RendezVousMedecinResponseDTO consulterRdvMedecin(Long rdvId, Long medecinId);

    // --- Actions SECRETAIRE ---
    List<RendezVousMedecinResponseDTO> listerTousLesRdv();
    RendezVousMedecinResponseDTO consulterRdvSecretaire(Long rdvId);
    RendezVousMedecinResponseDTO confirmerRdv(Long rdvId);
    RendezVousMedecinResponseDTO confirmerRdv(Long rdvId, String notes);
    RendezVousMedecinResponseDTO rejeterRdv(Long rdvId);
    RendezVousMedecinResponseDTO rejeterRdv(Long rdvId, String notes);
    RendezVousMedecinResponseDTO modifierNotes(Long rdvId, String notes);
}


