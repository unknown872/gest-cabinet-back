package gest_cabinet_back.service.impl;

import gest_cabinet_back.dto.RendezVousMedecinResponseDTO;
import gest_cabinet_back.dto.RendezVousRequestDTO;
import gest_cabinet_back.dto.RendezVousResponseDTO;
import gest_cabinet_back.exception.RessourceNotFoundException;
import gest_cabinet_back.model.Medecin;
import gest_cabinet_back.model.Patient;
import gest_cabinet_back.model.RendezVous;
import gest_cabinet_back.model.StatutRdv;
import gest_cabinet_back.repository.MedecinRepository;
import gest_cabinet_back.repository.PatientRepository;
import gest_cabinet_back.repository.RendezVousRepository;
import gest_cabinet_back.service.RendezVousService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RendezVousServiceImpl implements RendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;

    @Override
    public RendezVousResponseDTO demanderRdv(RendezVousRequestDTO dto, Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RessourceNotFoundException("Patient introuvable avec l'id : " + patientId));

        Medecin medecin = medecinRepository.findById(dto.getMedecinId())
                .orElseThrow(() -> new RessourceNotFoundException("Médecin introuvable avec l'id : " + dto.getMedecinId()));

        RendezVous rdv = new RendezVous();
        rdv.setDateHeure(dto.getDateHeure());
        rdv.setMotif(dto.getMotif());
        rdv.setNotes(dto.getNotes());
        rdv.setStatut(StatutRdv.PLANIFIE);
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);

        RendezVous saved = rendezVousRepository.save(rdv);
        return verserDtoRdv(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousResponseDTO> listerMesRdv(Long patientId) {
        return rendezVousRepository.findByPatientId(patientId)
                .stream()
                .map(this::verserDtoRdv)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RendezVousResponseDTO consulterRdv(Long rdvId, Long patientId) {
        RendezVous rdv = rendezVousRepository.findByIdAndPatientId(rdvId, patientId)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Rendez-vous introuvable avec l'id : " + rdvId + " pour ce patient"));
        return verserDtoRdv(rdv);
    }

    @Override
    public void annulerRdv(Long rdvId, Long patientId) {
        RendezVous rdv = rendezVousRepository.findByIdAndPatientId(rdvId, patientId)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Rendez-vous introuvable avec l'id : " + rdvId + " pour ce patient"));

        if (rdv.getStatut() == StatutRdv.ANNULE) {
            throw new IllegalStateException("Ce rendez-vous est déjà annulé.");
        }
        if (rdv.getStatut() == StatutRdv.HONORE_PRESENT) {
            throw new IllegalStateException("Impossible d'annuler un rendez-vous déjà honoré.");
        }

        rdv.setStatut(StatutRdv.ANNULE);
        rendezVousRepository.save(rdv);
    }

    // ----------------------------------------------------------------
    // Actions MEDECIN
    // ----------------------------------------------------------------

    @Override
    public RendezVousMedecinResponseDTO prendreRdvPourPatient(RendezVousRequestDTO dto, Long medecinConnecteId) {
        // Le médecin cible du RDV est celui du DTO (peut être différent du médecin connecté)
        if (dto.getMedecinId() == null) {
            throw new IllegalArgumentException("Le champ medecinId est obligatoire pour prendre un rendez-vous.");
        }
        Medecin medecinCible = medecinRepository.findById(dto.getMedecinId())
                .orElseThrow(() -> new RessourceNotFoundException("Médecin introuvable avec l'id : " + dto.getMedecinId()));

        if (dto.getPatientId() == null) {
            throw new IllegalArgumentException("Le champ patientId est obligatoire.");
        }
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RessourceNotFoundException("Patient introuvable avec l'id : " + dto.getPatientId()));

        RendezVous rdv = new RendezVous();
        rdv.setDateHeure(dto.getDateHeure());
        rdv.setMotif(dto.getMotif());
        rdv.setNotes(dto.getNotes());
        rdv.setStatut(StatutRdv.PLANIFIE);
        rdv.setPatient(patient);
        rdv.setMedecin(medecinCible); // ← médecin du RDV, pas forcément celui connecté

        return verserDtoRdvMedecin(rendezVousRepository.save(rdv));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousMedecinResponseDTO> listerRdvDuMedecin(Long medecinId) {
        // Le médecin ne voit que les rendez-vous confirmés
        return rendezVousRepository.findByMedecinIdAndStatut(medecinId, StatutRdv.CONFIRME)
                .stream()
                .map(this::verserDtoRdvMedecin)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RendezVousMedecinResponseDTO consulterRdvMedecin(Long rdvId, Long medecinId) {
        // Le médecin ne consulte que les rendez-vous confirmés le concernant
        RendezVous rdv = rendezVousRepository.findByIdAndMedecinIdAndStatut(rdvId, medecinId, StatutRdv.CONFIRME)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Rendez-vous confirmé introuvable avec l'id : " + rdvId + " pour ce médecin"));
        return verserDtoRdvMedecin(rdv);
    }

    // ----------------------------------------------------------------
    // Actions SECRETAIRE
    // ----------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousMedecinResponseDTO> listerTousLesRdv() {
        return rendezVousRepository.findAll()
                .stream()
                .map(this::verserDtoRdvMedecin)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RendezVousMedecinResponseDTO consulterRdvSecretaire(Long rdvId) {
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous introuvable avec l'id : " + rdvId));
        return verserDtoRdvMedecin(rdv);
    }

    @Override
    public RendezVousMedecinResponseDTO confirmerRdv(Long rdvId) {
        return confirmerRdv(rdvId, null);
    }

    @Override
    public RendezVousMedecinResponseDTO confirmerRdv(Long rdvId, String notes) {
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous introuvable avec l'id : " + rdvId));

        if (rdv.getStatut() == StatutRdv.ANNULE) {
            throw new IllegalStateException("Impossible de confirmer un rendez-vous annulé.");
        }
        if (rdv.getStatut() == StatutRdv.HONORE_PRESENT) {
            throw new IllegalStateException("Le rendez-vous a déjà été honoré.");
        }

        rdv.setStatut(StatutRdv.CONFIRME);
        if (notes != null && !notes.trim().isEmpty()) {
            rdv.setNotes(notes);
        }
        RendezVous saved = rendezVousRepository.save(rdv);
        return verserDtoRdvMedecin(saved);
    }

    @Override
    public RendezVousMedecinResponseDTO rejeterRdv(Long rdvId) {
        return rejeterRdv(rdvId, null);
    }

    @Override
    public RendezVousMedecinResponseDTO rejeterRdv(Long rdvId, String notes) {
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous introuvable avec l'id : " + rdvId));

        if (rdv.getStatut() == StatutRdv.ANNULE) {
            throw new IllegalStateException("Impossible de rejeter un rendez-vous déjà annulé.");
        }
        if (rdv.getStatut() == StatutRdv.HONORE_PRESENT) {
            throw new IllegalStateException("Impossible de rejeter un rendez-vous déjà honoré.");
        }

        rdv.setStatut(StatutRdv.ABSENT);
        if (notes != null && !notes.trim().isEmpty()) {
            rdv.setNotes(notes);
        }
        RendezVous saved = rendezVousRepository.save(rdv);
        return verserDtoRdvMedecin(saved);
    }

    @Override
    public RendezVousMedecinResponseDTO modifierNotes(Long rdvId, String notes) {
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous introuvable avec l'id : " + rdvId));

        rdv.setNotes(notes);
        RendezVous saved = rendezVousRepository.save(rdv);
        return verserDtoRdvMedecin(saved);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private RendezVousResponseDTO verserDtoRdv(RendezVous rdv) {
        return new RendezVousResponseDTO(
                rdv.getId(),
                rdv.getDateHeure(),
                rdv.getMotif(),
                rdv.getNotes(),
                rdv.getStatut(),
                rdv.getMedecin().getNom(),
                rdv.getMedecin().getPrenom()
        );
    }

    private RendezVousMedecinResponseDTO verserDtoRdvMedecin(RendezVous rdv) {
        Patient patient = rdv.getPatient();
        Medecin medecin = rdv.getMedecin();

        return new RendezVousMedecinResponseDTO(
                rdv.getId(),
                rdv.getDateHeure(),
                rdv.getMotif(),
                rdv.getNotes(),
                rdv.getStatut(),
                medecin.getId(),
                medecin.getNom(),
                medecin.getPrenom(),
                patient.getId(),
                patient.getCodePatient(),
                patient.getNom(),
                patient.getPrenom(),
                patient.getTelephone(),
                patient.getEmail(),
                patient.getGenre(),
                patient.getDateNaissance()
        );
    }
}

