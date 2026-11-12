package gest_cabinet_back.service.impl;

import gest_cabinet_back.dto.RendezVousMedecinResponseDTO;
import gest_cabinet_back.dto.RendezVousRequestDTO;
import gest_cabinet_back.dto.RendezVousResponseDTO;
import gest_cabinet_back.exception.RessourceNotFoundException;
import gest_cabinet_back.model.DisponibiliteMedecin;
import gest_cabinet_back.model.Medecin;
import gest_cabinet_back.model.Patient;
import gest_cabinet_back.model.RendezVous;
import gest_cabinet_back.model.StatutRdv;
import gest_cabinet_back.repository.DisponibiliteMedecinRepository;
import gest_cabinet_back.repository.MedecinRepository;
import gest_cabinet_back.repository.PatientRepository;
import gest_cabinet_back.repository.RendezVousRepository;
import gest_cabinet_back.service.RendezVousService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RendezVousServiceImpl implements RendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;
    private final DisponibiliteMedecinRepository disponibiliteRepository;

    @Override
    public RendezVousResponseDTO demanderRdv(RendezVousRequestDTO dto, Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new RessourceNotFoundException("Patient introuvable avec l'id : " + patientId));

        Medecin medecin = medecinRepository.findById(dto.getMedecinId())
                .orElseThrow(() -> new RessourceNotFoundException("Médecin introuvable avec l'id : " + dto.getMedecinId()));

        // RG03/RG04 : on ne fait plus confiance à l'heure tapée à la main — on
        // vérifie qu'elle correspond à un créneau réellement déclaré et encore libre.
        validerCreneauDisponible(medecin.getId(), dto.getDateHeure());

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

        // Même garde-fou que côté patient, pour rester cohérent (RG03/RG04).
        validerCreneauDisponible(medecinCible.getId(), dto.getDateHeure());

        RendezVous rdv = new RendezVous();
        rdv.setDateHeure(dto.getDateHeure());
        rdv.setMotif(dto.getMotif());
        rdv.setNotes(dto.getNotes());
        rdv.setStatut(StatutRdv.PLANIFIE);
        rdv.setPatient(patient);
        rdv.setMedecin(medecinCible);

        return verserDtoRdvMedecin(rendezVousRepository.save(rdv));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousMedecinResponseDTO> listerRdvDuMedecin(Long medecinId) {
        return rendezVousRepository.findByMedecinIdAndStatut(medecinId, StatutRdv.CONFIRME)
                .stream()
                .map(this::verserDtoRdvMedecin)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RendezVousMedecinResponseDTO consulterRdvMedecin(Long rdvId, Long medecinId) {
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
    // Créneaux disponibles (M2.3.2 / M2.2.2)
    // ----------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<LocalDateTime> getCreneauxDisponibles(Long medecinId, LocalDate date) {
        List<DisponibiliteMedecin> disponibilites =
                disponibiliteRepository.findByMedecinIdAndDateJourOrderByHeureDebutAsc(medecinId, date);

        if (disponibilites.isEmpty()) {
            return List.of();
        }

        Set<LocalTime> heuresDejaPrises = heuresOccupees(medecinId, date);

        List<LocalDateTime> creneauxLibres = new ArrayList<>();
        for (DisponibiliteMedecin dispo : disponibilites) {
            LocalTime curseur = dispo.getHeureDebut();
            // On avance par pas de dureeCreneauMinutes tant que le créneau entier
            // (curseur -> curseur+durée) tient dans la plage de disponibilité.
            while (!curseur.plusMinutes(dispo.getDureeCreneauMinutes()).isAfter(dispo.getHeureFin())) {
                if (!heuresDejaPrises.contains(curseur)) {
                    creneauxLibres.add(date.atTime(curseur));
                }
                curseur = curseur.plusMinutes(dispo.getDureeCreneauMinutes());
            }
        }
        return creneauxLibres;
    }

    /**
     * Vérifie que la dateHeure demandée correspond à un créneau réellement
     * déclaré par le médecin ET qu'il n'est pas déjà pris (RG03/RG04).
     * Lève IllegalArgumentException sinon.
     *
     * Limite connue : cette vérification se fait en lecture avant l'insertion,
     * elle ne protège donc pas encore totalement contre une double réservation
     * strictement simultanée (il faudrait une contrainte d'unicité en base pour
     * une garantie atomique — amélioration à prévoir, cf. nos échanges sur le
     * diagramme de séquence).
     */
    private void validerCreneauDisponible(Long medecinId, LocalDateTime dateHeure) {
        if (dateHeure == null) {
            throw new IllegalArgumentException("La date et l'heure du rendez-vous sont obligatoires.");
        }

        LocalDate date = dateHeure.toLocalDate();
        LocalTime heure = dateHeure.toLocalTime();

        List<DisponibiliteMedecin> disponibilites =
                disponibiliteRepository.findByMedecinIdAndDateJourOrderByHeureDebutAsc(medecinId, date);

        boolean correspondAUneDisponibilite = disponibilites.stream().anyMatch(dispo -> {
            LocalTime curseur = dispo.getHeureDebut();
            while (!curseur.plusMinutes(dispo.getDureeCreneauMinutes()).isAfter(dispo.getHeureFin())) {
                if (curseur.equals(heure)) {
                    return true;
                }
                curseur = curseur.plusMinutes(dispo.getDureeCreneauMinutes());
            }
            return false;
        });

        if (!correspondAUneDisponibilite) {
            throw new IllegalArgumentException(
                    "Ce créneau (" + heure + " le " + date + ") ne correspond à aucune disponibilité déclarée par ce médecin."
            );
        }

        if (heuresOccupees(medecinId, date).contains(heure)) {
            throw new IllegalArgumentException(
                    "Ce créneau (" + heure + " le " + date + ") est déjà réservé."
            );
        }
    }

    private Set<LocalTime> heuresOccupees(Long medecinId, LocalDate date) {
        LocalDateTime debutJournee = date.atStartOfDay();
        LocalDateTime finJournee = date.plusDays(1).atStartOfDay();

        List<RendezVous> rdvExistants =
                rendezVousRepository.findByMedecinIdAndDateHeureBetween(medecinId, debutJournee, finJournee);

        Set<LocalTime> heures = new HashSet<>();
        for (RendezVous rdv : rdvExistants) {
            // Un RDV annulé libère le créneau ; tous les autres statuts le gardent occupé.
            if (rdv.getStatut() != StatutRdv.ANNULE) {
                heures.add(rdv.getDateHeure().toLocalTime());
            }
        }
        return heures;
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