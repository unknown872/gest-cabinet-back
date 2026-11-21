package gest_cabinet_back.service.impl;

import gest_cabinet_back.dto.*;
import gest_cabinet_back.exception.RessourceNotFoundException;
import gest_cabinet_back.model.*;
import gest_cabinet_back.repository.ConsultationRepository;
import gest_cabinet_back.repository.LignePrescriptionRepository;
import gest_cabinet_back.repository.PatientRepository;
import gest_cabinet_back.repository.PrescriptionRepository;
import gest_cabinet_back.repository.RendezVousRepository;
import gest_cabinet_back.service.OrdonnancePdfService;
import gest_cabinet_back.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final LignePrescriptionRepository lignePrescriptionRepository;
    private final ConsultationRepository consultationRepository;
    private final PatientRepository patientRepository;
    private final RendezVousRepository rendezVousRepository;
    private final OrdonnancePdfService ordonnancePdfService;

    // ----------------------------------------------------------------
    // Actions MEDECIN
    // ----------------------------------------------------------------

    @Override
    public PrescriptionResponseDTO creerPrescription(Long consultationId, PrescriptionCreateRequestDTO dto, Long medecinId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RessourceNotFoundException("Consultation introuvable avec l'id : " + consultationId));

        // RG16 : Seul un médecin peut créer une prescription, et uniquement dans le cadre d'une consultation qu'il a lui-même réalisée
        if (!consultation.getRendezVous().getMedecin().getId().equals(medecinId)) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas le médecin ayant réalisé cette consultation (RG16).");
        }

        // M5.1 : Créer une prescription depuis une consultation en cours
        if (consultation.getStatut() != StatutConsultation.EN_COURS) {
            throw new IllegalStateException("Impossible de créer une prescription : la consultation doit être au statut EN_COURS (statut actuel : "
                    + consultation.getStatut() + ").");
        }

        Prescription prescription = new Prescription();
        prescription.setConsultation(consultation);
        prescription.setStatut(StatutPrescription.BROUILLON);
        prescription.setDateEmission(LocalDateTime.now());

        if (dto != null) {
            prescription.setInstructionsGenerales(dto.getInstructionsGenerales());
            prescription.setDateExpiration(dto.getDateExpiration());

            if (dto.getLignes() != null) {
                for (LignePrescriptionRequestDTO ligneDto : dto.getLignes()) {
                    if (ligneDto.getMedicament() != null && !ligneDto.getMedicament().isBlank()) {
                        LignePrescription ligne = new LignePrescription();
                        ligne.setMedicament(ligneDto.getMedicament().trim());
                        ligne.setPosologie(ligneDto.getPosologie());
                        ligne.setDureeTraitement(ligneDto.getDureeTraitement());
                        ligne.setInstructions(ligneDto.getInstructions());
                        prescription.addLigne(ligne);
                    }
                }
            }
        }

        Prescription saved = prescriptionRepository.save(prescription);
        return versResponseDTO(saved);
    }

    @Override
    public PrescriptionResponseDTO modifierPrescription(Long prescriptionId, PrescriptionUpdateRequestDTO dto, Long medecinId) {
        if (dto == null) {
            throw new IllegalArgumentException("Les données de mise à jour sont obligatoires.");
        }

        Prescription prescription = trouverPrescriptionEtVerifierMedecin(prescriptionId, medecinId);
        verifierPrescriptionModifiable(prescription);

        if (dto.getInstructionsGenerales() != null) {
            prescription.setInstructionsGenerales(dto.getInstructionsGenerales());
        }
        if (dto.getDateExpiration() != null) {
            prescription.setDateExpiration(dto.getDateExpiration());
        }

        return versResponseDTO(prescription);
    }

    @Override
    public PrescriptionResponseDTO ajouterLigne(Long prescriptionId, LignePrescriptionRequestDTO dto, Long medecinId) {
        if (dto == null || dto.getMedicament() == null || dto.getMedicament().isBlank()) {
            throw new IllegalArgumentException("Le nom du médicament est obligatoire pour ajouter une ligne de prescription.");
        }

        Prescription prescription = trouverPrescriptionEtVerifierMedecin(prescriptionId, medecinId);
        verifierPrescriptionModifiable(prescription);

        LignePrescription ligne = new LignePrescription();
        ligne.setMedicament(dto.getMedicament().trim());
        ligne.setPosologie(dto.getPosologie());
        ligne.setDureeTraitement(dto.getDureeTraitement());
        ligne.setInstructions(dto.getInstructions());

        prescription.addLigne(ligne);

        return versResponseDTO(prescriptionRepository.save(prescription));
    }

    @Override
    public PrescriptionResponseDTO modifierLigne(Long prescriptionId, Long ligneId, LignePrescriptionRequestDTO dto, Long medecinId) {
        if (dto == null || dto.getMedicament() == null || dto.getMedicament().isBlank()) {
            throw new IllegalArgumentException("Le nom du médicament est obligatoire.");
        }

        Prescription prescription = trouverPrescriptionEtVerifierMedecin(prescriptionId, medecinId);
        verifierPrescriptionModifiable(prescription);

        LignePrescription ligne = lignePrescriptionRepository.findById(ligneId)
                .orElseThrow(() -> new RessourceNotFoundException("Ligne de prescription introuvable avec l'id : " + ligneId));

        if (!ligne.getPrescription().getId().equals(prescriptionId)) {
            throw new IllegalArgumentException("Cette ligne n'appartient pas à la prescription spécifiée.");
        }

        ligne.setMedicament(dto.getMedicament().trim());
        ligne.setPosologie(dto.getPosologie());
        ligne.setDureeTraitement(dto.getDureeTraitement());
        ligne.setInstructions(dto.getInstructions());

        lignePrescriptionRepository.save(ligne);
        return versResponseDTO(prescription);
    }

    @Override
    public PrescriptionResponseDTO supprimerLigne(Long prescriptionId, Long ligneId, Long medecinId) {
        Prescription prescription = trouverPrescriptionEtVerifierMedecin(prescriptionId, medecinId);
        verifierPrescriptionModifiable(prescription);

        LignePrescription ligne = lignePrescriptionRepository.findById(ligneId)
                .orElseThrow(() -> new RessourceNotFoundException("Ligne de prescription introuvable avec l'id : " + ligneId));

        if (!ligne.getPrescription().getId().equals(prescriptionId)) {
            throw new IllegalArgumentException("Cette ligne n'appartient pas à la prescription spécifiée.");
        }

        prescription.removeLigne(ligne);
        lignePrescriptionRepository.delete(ligne);

        return versResponseDTO(prescription);
    }

    @Override
    public PrescriptionResponseDTO validerPrescription(Long prescriptionId, Long medecinId) {
        Prescription prescription = trouverPrescriptionEtVerifierMedecin(prescriptionId, medecinId);

        if (prescription.getStatut() == StatutPrescription.VALIDEE) {
            throw new IllegalStateException("Cette prescription est déjà validée (RG18).");
        }
        if (prescription.getStatut() == StatutPrescription.ANNULEE) {
            throw new IllegalStateException("Impossible de valider une prescription annulée.");
        }

        // RG17 : Une prescription comporte au minimum une ligne de médicament
        if (prescription.getLignes() == null || prescription.getLignes().isEmpty()) {
            throw new IllegalStateException("Une prescription doit comporter au minimum une ligne de médicament avant d'être validée (RG17).");
        }

        // RG18 : Une prescription validée devient immuable et donne lieu à la génération d'une ordonnance PDF
        prescription.setStatut(StatutPrescription.VALIDEE);
        prescription.setDateEmission(LocalDateTime.now());
        prescription.setPdfUrl("/api/prescriptions/" + prescription.getId() + "/pdf");

        Prescription saved = prescriptionRepository.save(prescription);
        return versResponseDTO(saved);
    }

    @Override
    public PrescriptionResponseDTO annulerPrescription(Long prescriptionId, Long medecinId) {
        Prescription prescription = trouverPrescriptionEtVerifierMedecin(prescriptionId, medecinId);

        if (prescription.getStatut() == StatutPrescription.VALIDEE) {
            throw new IllegalStateException("Une prescription validée devient immuable et ne peut pas être annulée (RG18).");
        }

        prescription.setStatut(StatutPrescription.ANNULEE);
        return versResponseDTO(prescriptionRepository.save(prescription));
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponseDTO consulterPrescriptionMedecin(Long prescriptionId, Long medecinId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RessourceNotFoundException("Prescription introuvable avec l'id : " + prescriptionId));

        Long patientId = prescription.getConsultation().getDossierMedical().getPatient().getId();

        // RG11 : Relation de prise en charge
        if (!rendezVousRepository.existsByPatientIdAndMedecinId(patientId, medecinId)) {
            throw new AccessDeniedException("Accès refusé : aucune relation de prise en charge avec ce patient (RG11).");
        }

        return versResponseDTO(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> listerPrescriptionsParConsultation(Long consultationId, Long medecinId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new RessourceNotFoundException("Consultation introuvable avec l'id : " + consultationId));

        Long patientId = consultation.getDossierMedical().getPatient().getId();
        if (!rendezVousRepository.existsByPatientIdAndMedecinId(patientId, medecinId)) {
            throw new AccessDeniedException("Accès refusé : aucune relation de prise en charge avec ce patient.");
        }

        return prescriptionRepository.findByConsultationId(consultationId).stream()
                .map(this::versResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> listerPrescriptionsParPatient(Long patientId, Long medecinId) {
        if (!patientRepository.existsById(patientId)) {
            throw new RessourceNotFoundException("Patient introuvable avec l'id : " + patientId);
        }

        // RG11 : Relation de prise en charge
        if (!rendezVousRepository.existsByPatientIdAndMedecinId(patientId, medecinId)) {
            throw new AccessDeniedException("Accès refusé : aucune relation de prise en charge avec ce patient (RG11).");
        }

        return prescriptionRepository.findByPatientId(patientId).stream()
                .map(this::versResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> listerPrescriptionsDuMedecin(Long medecinId) {
        return prescriptionRepository.findByMedecinId(medecinId).stream()
                .map(this::versResponseDTO)
                .toList();
    }

    // ----------------------------------------------------------------
    // Actions PATIENT
    // ----------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> listerMesPrescriptions(Long patientId) {
        // M5.8 & M8.1.3 : Lister les prescriptions validées du patient
        return prescriptionRepository.findByPatientIdAndStatut(patientId, StatutPrescription.VALIDEE).stream()
                .map(this::versResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponseDTO consulterMaPrescription(Long prescriptionId, Long patientId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RessourceNotFoundException("Prescription introuvable avec l'id : " + prescriptionId));

        if (!prescription.getConsultation().getDossierMedical().getPatient().getId().equals(patientId)) {
            throw new AccessDeniedException("Accès refusé : cette prescription ne vous appartient pas.");
        }

        return versResponseDTO(prescription);
    }

    // ----------------------------------------------------------------
    // Téléchargement Ordonnance PDF
    // ----------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public byte[] telechargerOrdonnancePdf(Long prescriptionId, Long utilisateurId, boolean isMedecin) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RessourceNotFoundException("Prescription introuvable avec l'id : " + prescriptionId));

        Long patientId = prescription.getConsultation().getDossierMedical().getPatient().getId();

        if (isMedecin) {
            if (!rendezVousRepository.existsByPatientIdAndMedecinId(patientId, utilisateurId)) {
                throw new AccessDeniedException("Accès refusé : aucune relation de prise en charge avec ce patient.");
            }
        } else {
            // Patient
            if (!patientId.equals(utilisateurId)) {
                throw new AccessDeniedException("Accès refusé : cette ordonnance ne vous appartient pas.");
            }
        }

        // RG18 : Seule une prescription validée donne lieu à une ordonnance PDF
        if (prescription.getStatut() != StatutPrescription.VALIDEE) {
            throw new IllegalStateException("L'ordonnance PDF n'est disponible que pour une prescription validée.");
        }

        return ordonnancePdfService.genererPdf(prescription);
    }

    // ----------------------------------------------------------------
    // Helpers & Mappers
    // ----------------------------------------------------------------

    private Prescription trouverPrescriptionEtVerifierMedecin(Long prescriptionId, Long medecinId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RessourceNotFoundException("Prescription introuvable avec l'id : " + prescriptionId));

        if (!prescription.getConsultation().getRendezVous().getMedecin().getId().equals(medecinId)) {
            throw new AccessDeniedException("Accès refusé : vous n'êtes pas le médecin ayant réalisé cette prescription (RG16).");
        }

        return prescription;
    }

    private void verifierPrescriptionModifiable(Prescription prescription) {
        // RG18 : Une prescription validée devient immuable
        if (prescription.getStatut() == StatutPrescription.VALIDEE) {
            throw new IllegalStateException("Une prescription validée devient immuable et ne peut plus être modifiée (RG18).");
        }
        if (prescription.getStatut() == StatutPrescription.ANNULEE) {
            throw new IllegalStateException("Impossible de modifier une prescription annulée.");
        }
        if (prescription.getConsultation().getStatut() != StatutConsultation.EN_COURS) {
            throw new IllegalStateException("Impossible de modifier la prescription : la consultation associée est clôturée ou annulée.");
        }
    }

    private PrescriptionResponseDTO versResponseDTO(Prescription p) {
        PrescriptionResponseDTO dto = new PrescriptionResponseDTO();
        dto.setId(p.getId());
        dto.setStatut(p.getStatut());
        dto.setDateEmission(p.getDateEmission());
        dto.setDateExpiration(p.getDateExpiration());
        dto.setInstructionsGenerales(p.getInstructionsGenerales());
        dto.setPdfUrl(p.getPdfUrl());

        if (p.getConsultation() != null) {
            dto.setConsultationId(p.getConsultation().getId());

            if (p.getConsultation().getRendezVous() != null && p.getConsultation().getRendezVous().getMedecin() != null) {
                Medecin medecin = p.getConsultation().getRendezVous().getMedecin();
                dto.setMedecinId(medecin.getId());
                dto.setNomMedecin(medecin.getNom());
                dto.setPrenomMedecin(medecin.getPrenom());
                if (medecin.getSpecialites() != null && !medecin.getSpecialites().isEmpty()) {
                    dto.setSpecialiteMedecin(medecin.getSpecialites().get(0).getLibelle());
                }
            }

            if (p.getConsultation().getDossierMedical() != null && p.getConsultation().getDossierMedical().getPatient() != null) {
                Patient patient = p.getConsultation().getDossierMedical().getPatient();
                dto.setPatientId(patient.getId());
                dto.setNomPatient(patient.getNom());
                dto.setPrenomPatient(patient.getPrenom());
                dto.setNumeroDossier(p.getConsultation().getDossierMedical().getNumeroDossier());
            }
        }

        if (p.getLignes() != null) {
            dto.setLignes(p.getLignes().stream()
                    .map(l -> new LignePrescriptionResponseDTO(
                            l.getId(),
                            l.getMedicament(),
                            l.getPosologie(),
                            l.getDureeTraitement(),
                            l.getInstructions()))
                    .toList());
        }

        return dto;
    }
}
