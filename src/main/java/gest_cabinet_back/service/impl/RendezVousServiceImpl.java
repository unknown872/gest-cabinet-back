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

        // RG04 : Créneau futur et limite 90 jours
        validerDelaiReservation(dto.getDateHeure());

        // RG07 : Bloquer si >= 3 absences non justifiées sur 12 mois glissants
        verifierBlocageAbsences(patientId);

        // RG05 : Max 3 rendez-vous à venir simultanément
        verifierLimiteRdvAVenir(patientId);

        // RG03 : on ne fait plus confiance à l'heure tapée à la main — on
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
    public RendezVousResponseDTO modifierMonRdv(Long rdvId, RendezVousRequestDTO dto, Long patientId) {
        RendezVous rdv = rendezVousRepository.findByIdAndPatientId(rdvId, patientId)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Rendez-vous introuvable avec l'id : " + rdvId + " pour ce patient"));

        if (rdv.getStatut() == StatutRdv.ANNULE) {
            throw new IllegalStateException("Impossible de modifier un rendez-vous annulé.");
        }
        if (rdv.getStatut() == StatutRdv.HONORE_PRESENT) {
            throw new IllegalStateException("Impossible de modifier un rendez-vous déjà honoré.");
        }
        if (rdv.getStatut() == StatutRdv.ABSENT) {
            throw new IllegalStateException("Impossible de modifier un rendez-vous marqué absent.");
        }

        // RG06 : Le patient ne peut modifier lui-même que jusqu'à 24h avant l'heure prévue
        validerDelaiModificationAnnulationPatient(rdv.getDateHeure(), "modifier");

        if (dto.getDateHeure() != null && !dto.getDateHeure().equals(rdv.getDateHeure())) {
            // RG04 : Créneau futur et limite 90 jours
            validerDelaiReservation(dto.getDateHeure());

            Long medecinId = (dto.getMedecinId() != null) ? dto.getMedecinId() : rdv.getMedecin().getId();
            Medecin medecin = medecinRepository.findById(medecinId)
                    .orElseThrow(() -> new RessourceNotFoundException("Médecin introuvable avec l'id : " + medecinId));

            validerCreneauDisponible(medecin.getId(), dto.getDateHeure());
            rdv.setMedecin(medecin);
            rdv.setDateHeure(dto.getDateHeure());
        }

        if (dto.getMotif() != null) {
            rdv.setMotif(dto.getMotif());
        }
        if (dto.getNotes() != null) {
            rdv.setNotes(dto.getNotes());
        }

        RendezVous saved = rendezVousRepository.save(rdv);
        return verserDtoRdv(saved);
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
        if (rdv.getStatut() == StatutRdv.ABSENT) {
            throw new IllegalStateException("Impossible d'annuler un rendez-vous marqué absent.");
        }

        // RG06 : Le patient ne peut annuler lui-même que jusqu'à 24h avant l'heure prévue
        validerDelaiModificationAnnulationPatient(rdv.getDateHeure(), "annuler");

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

        // RG04 : Créneau futur et limite 90 jours
        validerDelaiReservation(dto.getDateHeure());

        // RG05 : Max 3 RDV à venir
        verifierLimiteRdvAVenir(patient.getId());

        // Même garde-fou que côté patient, pour rester cohérent (RG03).
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
    public List<RendezVousMedecinResponseDTO> listerPlanningMedecin(Long medecinId, LocalDate date) {
        LocalDate cible = (date != null) ? date : LocalDate.now();
        LocalDateTime debut = cible.atStartOfDay();
        LocalDateTime fin = cible.plusDays(1).atStartOfDay();

        return rendezVousRepository.findByMedecinIdAndDateHeureBetweenOrderByDateHeureAsc(medecinId, debut, fin)
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
    public RendezVousMedecinResponseDTO creerRdvParSecretaire(RendezVousRequestDTO dto) {
        if (dto.getMedecinId() == null) {
            throw new IllegalArgumentException("Le champ medecinId est obligatoire.");
        }
        Medecin medecin = medecinRepository.findById(dto.getMedecinId())
                .orElseThrow(() -> new RessourceNotFoundException("Médecin introuvable avec l'id : " + dto.getMedecinId()));

        if (dto.getPatientId() == null) {
            throw new IllegalArgumentException("Le champ patientId est obligatoire.");
        }
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new RessourceNotFoundException("Patient introuvable avec l'id : " + dto.getPatientId()));

        // RG04 : Créneau futur et limite 90 jours
        validerDelaiReservation(dto.getDateHeure());

        // RG05 : Max 3 RDV à venir
        verifierLimiteRdvAVenir(patient.getId());

        validerCreneauDisponible(medecin.getId(), dto.getDateHeure());

        RendezVous rdv = new RendezVous();
        rdv.setDateHeure(dto.getDateHeure());
        rdv.setMotif(dto.getMotif());
        rdv.setNotes(dto.getNotes());
        rdv.setStatut(StatutRdv.CONFIRME); // Directement confirmé par la secrétaire
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);

        return verserDtoRdvMedecin(rendezVousRepository.save(rdv));
    }

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
    public List<RendezVousMedecinResponseDTO> listerRdvDuJour(LocalDate date) {
        LocalDate cible = (date != null) ? date : LocalDate.now();
        LocalDateTime debut = cible.atStartOfDay();
        LocalDateTime fin = cible.plusDays(1).atStartOfDay();

        return rendezVousRepository.findByDateHeureBetweenOrderByDateHeureAsc(debut, fin)
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

        rdv.setStatut(StatutRdv.ANNULE);
        if (notes != null && !notes.trim().isEmpty()) {
            rdv.setNotes(notes);
        }
        RendezVous saved = rendezVousRepository.save(rdv);
        return verserDtoRdvMedecin(saved);
    }

    @Override
    public RendezVousMedecinResponseDTO marquerPresence(Long rdvId) {
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous introuvable avec l'id : " + rdvId));

        if (rdv.getStatut() == StatutRdv.ANNULE) {
            throw new IllegalStateException("Impossible de marquer présent un rendez-vous annulé.");
        }
        if (rdv.getStatut() == StatutRdv.ABSENT) {
            throw new IllegalStateException("Impossible de marquer présent un rendez-vous déjà marqué absent.");
        }

        rdv.setStatut(StatutRdv.HONORE_PRESENT);
        RendezVous saved = rendezVousRepository.save(rdv);
        return verserDtoRdvMedecin(saved);
    }

    @Override
    public RendezVousMedecinResponseDTO marquerAbsence(Long rdvId) {
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous introuvable avec l'id : " + rdvId));

        if (rdv.getStatut() == StatutRdv.ANNULE) {
            throw new IllegalStateException("Impossible de marquer absent un rendez-vous annulé.");
        }
        if (rdv.getStatut() == StatutRdv.HONORE_PRESENT) {
            throw new IllegalStateException("Impossible de marquer absent un rendez-vous déjà honoré.");
        }

        rdv.setStatut(StatutRdv.ABSENT);
        RendezVous saved = rendezVousRepository.save(rdv);
        return verserDtoRdvMedecin(saved);
        
    }

    @Override
    public RendezVousMedecinResponseDTO annulerRdvSecretaire(Long rdvId, String notes) {
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous introuvable avec l'id : " + rdvId));

        if (rdv.getStatut() == StatutRdv.ANNULE) {
            throw new IllegalStateException("Ce rendez-vous est déjà annulé.");
        }
        if (rdv.getStatut() == StatutRdv.HONORE_PRESENT) {
            throw new IllegalStateException("Impossible d'annuler un rendez-vous déjà honoré.");
        }

        rdv.setStatut(StatutRdv.ANNULE);
        if (notes != null && !notes.trim().isEmpty()) {
            rdv.setNotes(notes);
        }
        RendezVous saved = rendezVousRepository.save(rdv);
        return verserDtoRdvMedecin(saved);
    }

    @Override
    public RendezVousMedecinResponseDTO modifierRdvSecretaire(Long rdvId, RendezVousRequestDTO dto) {
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RessourceNotFoundException("Rendez-vous introuvable avec l'id : " + rdvId));

        if (rdv.getStatut() == StatutRdv.ANNULE) {
            throw new IllegalStateException("Impossible de modifier un rendez-vous annulé.");
        }
        if (rdv.getStatut() == StatutRdv.HONORE_PRESENT) {
            throw new IllegalStateException("Impossible de modifier un rendez-vous déjà honoré.");
        }

        if (dto.getDateHeure() != null && !dto.getDateHeure().equals(rdv.getDateHeure())) {
            validerDelaiReservation(dto.getDateHeure());

            Long medecinId = (dto.getMedecinId() != null) ? dto.getMedecinId() : rdv.getMedecin().getId();
            Medecin medecin = medecinRepository.findById(medecinId)
                    .orElseThrow(() -> new RessourceNotFoundException("Médecin introuvable avec l'id : " + medecinId));

            validerCreneauDisponible(medecin.getId(), dto.getDateHeure());
            rdv.setMedecin(medecin);
            rdv.setDateHeure(dto.getDateHeure());
        }

        if (dto.getMotif() != null) {
            rdv.setMotif(dto.getMotif());
        }
        if (dto.getNotes() != null) {
            rdv.setNotes(dto.getNotes());
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
    // Validations Métier RG04, RG05, RG06, RG07
    // ----------------------------------------------------------------

    /**
     * RG04 : Un rendez-vous ne peut être pris que sur un créneau futur,
     * dans une limite de 90 jours à compter de la date du jour.
     */
    private void validerDelaiReservation(LocalDateTime dateHeure) {
        if (dateHeure == null) {
            throw new IllegalArgumentException("La date et l'heure du rendez-vous sont obligatoires.");
        }
        LocalDateTime maintenant = LocalDateTime.now();
        if (dateHeure.isBefore(maintenant)) {
            throw new IllegalArgumentException("La date et l'heure du rendez-vous doivent être dans le futur.");
        }
        LocalDate dateLimite = maintenant.toLocalDate().plusDays(90);
        if (dateHeure.toLocalDate().isAfter(dateLimite)) {
            throw new IllegalArgumentException(
                    "Impossible de réserver un rendez-vous au-delà de 90 jours (date limite : " + dateLimite + ").");
        }
    }

    /**
     * RG05 : Un patient ne peut détenir plus de 3 rendez-vous à venir simultanément,
     * tous médecins confondus.
     */
    private void verifierLimiteRdvAVenir(Long patientId) {
        long rdvAVenir = rendezVousRepository.countByPatientIdAndDateHeureAfterAndStatutIn(
                patientId,
                LocalDateTime.now(),
                List.of(StatutRdv.PLANIFIE, StatutRdv.CONFIRME)
        );
        if (rdvAVenir >= 3) {
            throw new IllegalStateException(
                    "Vous avez déjà " + rdvAVenir + " rendez-vous à venir (limite maximale autorisée : 3).");
        }
    }

    /**
     * RG06 : Un patient peut annuler ou modifier lui-même un rendez-vous jusqu'à 24 heures avant l'heure prévue.
     * Au-delà, l'annulation/modification doit être effectuée par la secrétaire.
     */
    private void validerDelaiModificationAnnulationPatient(LocalDateTime dateHeureRdv, String action) {
        LocalDateTime limite = LocalDateTime.now().plusHours(24);
        if (dateHeureRdv.isBefore(limite)) {
            throw new IllegalStateException(
                    "Impossible de " + action + " ce rendez-vous à moins de 24 heures de l'heure prévue. "
                            + "Veuillez contacter le secrétariat du cabinet.");
        }
    }

    /**
     * RG07 : Un rendez-vous non honoré est marqué « Patient absent » par la secrétaire.
     * Au-delà de 3 absences non justifiées sur 12 mois glissants, la prise de rendez-vous en ligne est bloquée.
     */
    private void verifierBlocageAbsences(Long patientId) {
        LocalDateTime maintenant = LocalDateTime.now();
        LocalDateTime debut12Mois = maintenant.minusMonths(12);
        long nbAbsences = rendezVousRepository.countByPatientIdAndStatutAndDateHeureBetween(
                patientId, StatutRdv.ABSENT, debut12Mois, maintenant);

        if (nbAbsences >= 3) {
            throw new IllegalStateException(
                    "Votre compte est bloqué pour la prise de rendez-vous en ligne suite à " + nbAbsences
                            + " absences non justifiées sur les 12 derniers mois. Veuillez contacter le cabinet médical.");
        }
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