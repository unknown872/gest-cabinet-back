package gest_cabinet_back.service.impl;

import gest_cabinet_back.dto.ConsultationMedecinResponseDTO;
import gest_cabinet_back.dto.ConsultationRequestDTO;
import gest_cabinet_back.dto.ConsultationResponseDTO;
import gest_cabinet_back.dto.ObservationComplementaireRequestDTO;
import gest_cabinet_back.exception.RessourceNotFoundException;
import gest_cabinet_back.model.Consultation;
import gest_cabinet_back.model.DossierMedical;
import gest_cabinet_back.model.Medecin;
import gest_cabinet_back.model.Patient;
import gest_cabinet_back.model.RendezVous;
import gest_cabinet_back.model.StatutConsultation;
import gest_cabinet_back.model.StatutRdv;
import gest_cabinet_back.repository.ConsultationRepository;
import gest_cabinet_back.repository.DossierMedicalRepository;
import gest_cabinet_back.repository.PatientRepository;
import gest_cabinet_back.repository.RendezVousRepository;
import gest_cabinet_back.service.ConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ConsultationServiceImpl implements ConsultationService {

    private static final DateTimeFormatter COMPLEMENT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ConsultationRepository consultationRepository;
    private final RendezVousRepository rendezVousRepository;
    private final DossierMedicalRepository dossierMedicalRepository;
    private final PatientRepository patientRepository;

    // ----------------------------------------------------------------
    // Actions MEDECIN
    // ----------------------------------------------------------------

    @Override
    public ConsultationMedecinResponseDTO demarrerConsultation(
            Long rendezVousId, ConsultationRequestDTO dto, Long medecinId) {
        RendezVous rdv = rendezVousRepository.findById(rendezVousId)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Rendez-vous introuvable avec l'id : " + rendezVousId));

        if (!rdv.getMedecin().getId().equals(medecinId)) {
            throw new AccessDeniedException(
                    "Accès refusé : ce rendez-vous ne vous est pas attribué.");
        }

        if (rdv.getStatut() != StatutRdv.CONFIRME) {
            throw new IllegalStateException(
                    "Impossible de démarrer une consultation : le rendez-vous doit être au statut CONFIRME (statut actuel : "
                            + rdv.getStatut() + ").");
        }

        if (consultationRepository.existsByRendezVousId(rendezVousId)) {
            throw new IllegalStateException(
                    "Une consultation existe déjà pour le rendez-vous : " + rendezVousId);
        }

        Patient patient = rdv.getPatient();
        DossierMedical dossier = obtenirOuCreerDossierMedical(patient);

        Consultation consultation = new Consultation();
        consultation.setDateHeure(LocalDateTime.now());
        consultation.setStatut(StatutConsultation.EN_COURS);
        consultation.setRendezVous(rdv);
        consultation.setDossierMedical(dossier);

        String motifInitial = (dto != null && dto.getMotif() != null && !dto.getMotif().isBlank())
                ? dto.getMotif()
                : rdv.getMotif();
        consultation.setMotif(motifInitial);

        if (dto != null) {
            consultation.setObservations(dto.getObservations());
            consultation.setDiagnostic(dto.getDiagnostic());
            consultation.setTension(dto.getTension());
            consultation.setPoids(dto.getPoids());
            consultation.setTemperature(dto.getTemperature());
        }

        return versDtoMedecin(consultationRepository.save(consultation));
    }

    @Override
    public ConsultationMedecinResponseDTO modifierConsultation(
            Long consultationId, ConsultationRequestDTO dto, Long medecinId) {
        if (dto == null) {
            throw new IllegalArgumentException("Les données de mise à jour sont obligatoires.");
        }

        Consultation consultation = trouverConsultationEtVerifierMedecin(consultationId, medecinId);

        if (consultation.getStatut() != StatutConsultation.EN_COURS) {
            throw new IllegalStateException(
                    "Impossible de modifier une consultation au statut " + consultation.getStatut()
                            + ". Utilisez l'ajout d'observation complémentaire pour une consultation clôturée.");
        }

        if (dto.getMotif() != null) {
            consultation.setMotif(dto.getMotif());
        }
        if (dto.getObservations() != null) {
            consultation.setObservations(dto.getObservations());
        }
        if (dto.getDiagnostic() != null) {
            consultation.setDiagnostic(dto.getDiagnostic());
        }
        if (dto.getTension() != null) {
            consultation.setTension(dto.getTension());
        }
        if (dto.getPoids() != null) {
            consultation.setPoids(dto.getPoids());
        }
        if (dto.getTemperature() != null) {
            consultation.setTemperature(dto.getTemperature());
        }

        return versDtoMedecin(consultationRepository.save(consultation));
    }

    @Override
    public ConsultationMedecinResponseDTO cloturerConsultation(Long consultationId, Long medecinId) {
        Consultation consultation = trouverConsultationEtVerifierMedecin(consultationId, medecinId);

        if (consultation.getStatut() != StatutConsultation.EN_COURS) {
            throw new IllegalStateException(
                    "Impossible de clôturer une consultation au statut " + consultation.getStatut() + ".");
        }

        consultation.setStatut(StatutConsultation.TERMINEE);

        RendezVous rdv = consultation.getRendezVous();
        rdv.setStatut(StatutRdv.HONORE_PRESENT);
        rendezVousRepository.save(rdv);

        return versDtoMedecin(consultationRepository.save(consultation));
    }

    @Override
    public ConsultationMedecinResponseDTO annulerConsultation(Long consultationId, Long medecinId) {
        Consultation consultation = trouverConsultationEtVerifierMedecin(consultationId, medecinId);

        if (consultation.getStatut() != StatutConsultation.EN_COURS) {
            throw new IllegalStateException(
                    "Impossible d'annuler une consultation au statut " + consultation.getStatut() + ".");
        }

        consultation.setStatut(StatutConsultation.ANNULEE);
        return versDtoMedecin(consultationRepository.save(consultation));
    }

    @Override
    public ConsultationMedecinResponseDTO ajouterObservationComplementaire(
            Long consultationId, ObservationComplementaireRequestDTO dto, Long medecinId) {
        if (dto == null || dto.getObservation() == null || dto.getObservation().isBlank()) {
            throw new IllegalArgumentException("L'observation complémentaire ne peut pas être vide.");
        }

        Consultation consultation = trouverConsultationEtVerifierMedecin(consultationId, medecinId);

        if (consultation.getStatut() != StatutConsultation.TERMINEE) {
            throw new IllegalStateException(
                    "L'ajout d'observation complémentaire s'applique uniquement aux consultations clôturées.");
        }

        Medecin medecin = consultation.getRendezVous().getMedecin();
        String entete = String.format(
                "[Complément le %s - Dr %s %s] : %s",
                LocalDateTime.now().format(COMPLEMENT_DATE_FORMAT),
                medecin.getNom(),
                medecin.getPrenom(),
                dto.getObservation().trim()
        );

        String existant = consultation.getObservations();
        String misAJour = (existant == null || existant.isBlank())
                ? entete
                : existant + "\n\n" + entete;

        consultation.setObservations(misAJour);
        return versDtoMedecin(consultationRepository.save(consultation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationMedecinResponseDTO> listerConsultationsDuMedecin(
            Long medecinId, StatutConsultation statut) {
        List<Consultation> consultations = (statut != null)
                ? consultationRepository.findByRendezVousMedecinIdAndStatutOrderByDateHeureDesc(medecinId, statut)
                : consultationRepository.findByRendezVousMedecinIdOrderByDateHeureDesc(medecinId);

        return consultations.stream()
                .map(this::versDtoMedecin)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationMedecinResponseDTO consulterConsultationMedecin(Long consultationId, Long medecinId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Consultation introuvable avec l'id : " + consultationId));

        Long patientId = consultation.getDossierMedical().getPatient().getId();
        boolean estProprietaire = consultation.getRendezVous().getMedecin().getId().equals(medecinId);
        boolean priseEnCharge = estProprietaire
                || rendezVousRepository.existsByPatientIdAndMedecinId(patientId, medecinId);

        if (!priseEnCharge) {
            throw new AccessDeniedException(
                    "Accès refusé : aucune relation de prise en charge avec ce patient.");
        }

        return versDtoMedecin(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationMedecinResponseDTO> listerConsultationsParPatient(Long patientId, Long medecinId) {
        if (!patientRepository.existsById(patientId)) {
            throw new RessourceNotFoundException("Patient introuvable avec l'id : " + patientId);
        }

        if (!rendezVousRepository.existsByPatientIdAndMedecinId(patientId, medecinId)) {
            throw new AccessDeniedException(
                    "Accès refusé : aucune relation de prise en charge avec ce patient.");
        }

        return consultationRepository.findByDossierMedicalPatientIdOrderByDateHeureDesc(patientId)
                .stream()
                .map(this::versDtoMedecin)
                .toList();
    }

    // ----------------------------------------------------------------
    // Actions PATIENT
    // ----------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationResponseDTO> listerMesConsultations(Long patientId) {
        return consultationRepository.findByDossierMedicalPatientIdOrderByDateHeureDesc(patientId)
                .stream()
                .map(this::versDtoPatient)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationResponseDTO consulterMaConsultation(Long consultationId, Long patientId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Consultation introuvable avec l'id : " + consultationId));

        if (!consultation.getDossierMedical().getPatient().getId().equals(patientId)) {
            throw new AccessDeniedException(
                    "Accès refusé : cette consultation ne vous appartient pas.");
        }

        return versDtoPatient(consultation);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private Consultation trouverConsultationEtVerifierMedecin(Long consultationId, Long medecinId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "Consultation introuvable avec l'id : " + consultationId));

        if (!consultation.getRendezVous().getMedecin().getId().equals(medecinId)) {
            throw new AccessDeniedException(
                    "Accès refusé : vous n'êtes pas le médecin réalisant cette consultation.");
        }

        return consultation;
    }

    private DossierMedical obtenirOuCreerDossierMedical(Patient patient) {
        return dossierMedicalRepository.findByPatientId(patient.getId())
                .orElseGet(() -> {
                    DossierMedical dossier = new DossierMedical();
                    dossier.setPatient(patient);
                    dossier.setDateCreation(LocalDateTime.now());
                    dossier.setNumeroDossier("DM-" + patient.getId() + "-"
                            + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
                    return dossierMedicalRepository.save(dossier);
                });
    }

    private ConsultationMedecinResponseDTO versDtoMedecin(Consultation consultation) {
        RendezVous rdv = consultation.getRendezVous();
        DossierMedical dossier = consultation.getDossierMedical();
        Patient patient = dossier.getPatient();

        return new ConsultationMedecinResponseDTO(
                consultation.getId(),
                consultation.getDateHeure(),
                consultation.getMotif(),
                consultation.getObservations(),
                consultation.getDiagnostic(),
                consultation.getTension(),
                consultation.getPoids(),
                consultation.getTemperature(),
                consultation.getStatut(),
                rdv.getId(),
                dossier.getId(),
                dossier.getNumeroDossier(),
                patient.getId(),
                patient.getCodePatient(),
                patient.getNom(),
                patient.getPrenom(),
                patient.getTelephone(),
                patient.getGenre(),
                patient.getDateNaissance()
        );
    }

    private ConsultationResponseDTO versDtoPatient(Consultation consultation) {
        RendezVous rdv = consultation.getRendezVous();
        Medecin medecin = rdv.getMedecin();

        return new ConsultationResponseDTO(
                consultation.getId(),
                consultation.getDateHeure(),
                consultation.getMotif(),
                consultation.getObservations(),
                consultation.getDiagnostic(),
                consultation.getTension(),
                consultation.getPoids(),
                consultation.getTemperature(),
                consultation.getStatut(),
                rdv.getId(),
                medecin.getNom(),
                medecin.getPrenom()
        );
    }
}
