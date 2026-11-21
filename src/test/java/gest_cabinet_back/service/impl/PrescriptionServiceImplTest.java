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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;
    @Mock
    private LignePrescriptionRepository lignePrescriptionRepository;
    @Mock
    private ConsultationRepository consultationRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private RendezVousRepository rendezVousRepository;
    @Mock
    private OrdonnancePdfService ordonnancePdfService;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    private Medecin medecin;
    private Patient patient;
    private RendezVous rdv;
    private DossierMedical dossier;
    private Consultation consultation;
    private Prescription prescription;

    @BeforeEach
    void setUp() {
        medecin = new Medecin();
        medecin.setId(10L);
        medecin.setNom("Diallo");
        medecin.setPrenom("Moussa");

        Specialite specialite = new Specialite();
        specialite.setId(1L);
        specialite.setCode("CARD");
        specialite.setLibelle("Cardiologie");
        medecin.setSpecialites(List.of(specialite));

        patient = new Patient();
        patient.setId(20L);
        patient.setCodePatient("PAT-001");
        patient.setNom("Ndiaye");
        patient.setPrenom("Awa");

        dossier = new DossierMedical();
        dossier.setId(30L);
        dossier.setNumeroDossier("DM-2026-001");
        dossier.setPatient(patient);

        rdv = new RendezVous();
        rdv.setId(100L);
        rdv.setMedecin(medecin);
        rdv.setPatient(patient);
        rdv.setStatut(StatutRdv.HONORE_PRESENT);

        consultation = new Consultation();
        consultation.setId(200L);
        consultation.setRendezVous(rdv);
        consultation.setDossierMedical(dossier);
        consultation.setStatut(StatutConsultation.EN_COURS);

        prescription = new Prescription();
        prescription.setId(500L);
        prescription.setConsultation(consultation);
        prescription.setStatut(StatutPrescription.BROUILLON);
        prescription.setInstructionsGenerales("Prendre après les repas");
        prescription.setDateExpiration(LocalDate.now().plusMonths(1));
    }

    @Test
    @DisplayName("Créer une prescription avec succès depuis une consultation EN_COURS (M5.1)")
    void creerPrescription_Succes() {
        when(consultationRepository.findById(200L)).thenReturn(Optional.of(consultation));
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(inv -> {
            Prescription p = inv.getArgument(0);
            p.setId(500L);
            return p;
        });

        PrescriptionCreateRequestDTO dto = new PrescriptionCreateRequestDTO();
        dto.setInstructionsGenerales("Bien s'hydrater");
        dto.setDateExpiration(LocalDate.now().plusDays(15));
        dto.setLignes(List.of(new LignePrescriptionRequestDTO("Paracétamol", "1g", "5 jours", "3 fois/jour")));

        PrescriptionResponseDTO response = prescriptionService.creerPrescription(200L, dto, 10L);

        assertNotNull(response);
        assertEquals(StatutPrescription.BROUILLON, response.getStatut());
        assertEquals("Bien s'hydrater", response.getInstructionsGenerales());
        assertEquals(1, response.getLignes().size());
        assertEquals("Paracétamol", response.getLignes().get(0).getMedicament());
        verify(prescriptionRepository).save(any(Prescription.class));
    }

    @Test
    @DisplayName("Créer prescription refusée si le médecin n'est pas celui de la consultation (RG16)")
    void creerPrescription_MedecinNonAttribue_ThrowsAccessDenied() {
        when(consultationRepository.findById(200L)).thenReturn(Optional.of(consultation));

        PrescriptionCreateRequestDTO dto = new PrescriptionCreateRequestDTO();

        assertThrows(AccessDeniedException.class, () ->
                prescriptionService.creerPrescription(200L, dto, 999L));
        verify(prescriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Créer prescription refusée si consultation non EN_COURS (RG13 / M5.1)")
    void creerPrescription_ConsultationTerminee_ThrowsIllegalState() {
        consultation.setStatut(StatutConsultation.TERMINEE);
        when(consultationRepository.findById(200L)).thenReturn(Optional.of(consultation));

        PrescriptionCreateRequestDTO dto = new PrescriptionCreateRequestDTO();

        assertThrows(IllegalStateException.class, () ->
                prescriptionService.creerPrescription(200L, dto, 10L));
    }

    @Test
    @DisplayName("Ajouter une ligne de médicament avec succès (M5.2, M5.3)")
    void ajouterLigne_Succes() {
        when(prescriptionRepository.findById(500L)).thenReturn(Optional.of(prescription));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);

        LignePrescriptionRequestDTO dto = new LignePrescriptionRequestDTO("Amoxicilline", "500mg", "7 jours", "Matin et soir");

        PrescriptionResponseDTO response = prescriptionService.ajouterLigne(500L, dto, 10L);

        assertNotNull(response);
        assertEquals(1, prescription.getLignes().size());
        assertEquals("Amoxicilline", prescription.getLignes().get(0).getMedicament());
        verify(prescriptionRepository).save(prescription);
    }

    @Test
    @DisplayName("Ajouter une ligne rejeté si médicament vide")
    void ajouterLigne_MedicamentVide_ThrowsIllegalArgument() {
        LignePrescriptionRequestDTO dto = new LignePrescriptionRequestDTO("", "500mg", "7 jours", "");

        assertThrows(IllegalArgumentException.class, () ->
                prescriptionService.ajouterLigne(500L, dto, 10L));
    }

    @Test
    @DisplayName("Valider une prescription avec au moins une ligne (M5.5 / RG17 / RG18)")
    void validerPrescription_Succes() {
        LignePrescription ligne = new LignePrescription();
        ligne.setId(1L);
        ligne.setMedicament("Ibuprofène");
        prescription.addLigne(ligne);

        when(prescriptionRepository.findById(500L)).thenReturn(Optional.of(prescription));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);

        PrescriptionResponseDTO response = prescriptionService.validerPrescription(500L, 10L);

        assertEquals(StatutPrescription.VALIDEE, response.getStatut());
        assertNotNull(response.getPdfUrl());
        assertTrue(response.getPdfUrl().contains("/pdf"));
        verify(prescriptionRepository).save(prescription);
    }

    @Test
    @DisplayName("Valider prescription rejetée si zéro ligne de médicament (RG17)")
    void validerPrescription_SansLigne_ThrowsIllegalState() {
        when(prescriptionRepository.findById(500L)).thenReturn(Optional.of(prescription));

        assertThrows(IllegalStateException.class, () ->
                prescriptionService.validerPrescription(500L, 10L));
        assertEquals(StatutPrescription.BROUILLON, prescription.getStatut());
    }

    @Test
    @DisplayName("Modification rejetée si prescription déjà validée (Immuabilité RG18)")
    void modifierPrescription_PrescriptionValidee_ThrowsIllegalState() {
        prescription.setStatut(StatutPrescription.VALIDEE);
        when(prescriptionRepository.findById(500L)).thenReturn(Optional.of(prescription));

        PrescriptionUpdateRequestDTO dto = new PrescriptionUpdateRequestDTO();
        dto.setInstructionsGenerales("Nouvelle instruction");

        assertThrows(IllegalStateException.class, () ->
                prescriptionService.modifierPrescription(500L, dto, 10L));
    }

    @Test
    @DisplayName("Ajout de ligne rejeté si prescription déjà validée (Immuabilité RG18)")
    void ajouterLigne_PrescriptionValidee_ThrowsIllegalState() {
        prescription.setStatut(StatutPrescription.VALIDEE);
        when(prescriptionRepository.findById(500L)).thenReturn(Optional.of(prescription));

        LignePrescriptionRequestDTO dto = new LignePrescriptionRequestDTO("Doliprane", "1g", "3 jours", "");

        assertThrows(IllegalStateException.class, () ->
                prescriptionService.ajouterLigne(500L, dto, 10L));
    }

    @Test
    @DisplayName("Téléchargement ordonnance PDF avec succès pour le patient (M5.8 / CA06)")
    void telechargerOrdonnancePdf_Patient_Succes() {
        prescription.setStatut(StatutPrescription.VALIDEE);
        byte[] fakePdf = "%PDF-1.4 Fake PDF Content".getBytes();

        when(prescriptionRepository.findById(500L)).thenReturn(Optional.of(prescription));
        when(ordonnancePdfService.genererPdf(prescription)).thenReturn(fakePdf);

        byte[] result = prescriptionService.telechargerOrdonnancePdf(500L, 20L, false);

        assertNotNull(result);
        assertArrayEquals(fakePdf, result);
        verify(ordonnancePdfService).genererPdf(prescription);
    }

    @Test
    @DisplayName("Téléchargement ordonnance rejeté pour un autre patient (CA02)")
    void telechargerOrdonnancePdf_AutrePatient_ThrowsAccessDenied() {
        prescription.setStatut(StatutPrescription.VALIDEE);
        when(prescriptionRepository.findById(500L)).thenReturn(Optional.of(prescription));

        assertThrows(AccessDeniedException.class, () ->
                prescriptionService.telechargerOrdonnancePdf(500L, 999L, false));
    }

    @Test
    @DisplayName("Téléchargement PDF rejeté si prescription non validée (RG18)")
    void telechargerOrdonnancePdf_PrescriptionBrouillon_ThrowsIllegalState() {
        prescription.setStatut(StatutPrescription.BROUILLON);
        when(prescriptionRepository.findById(500L)).thenReturn(Optional.of(prescription));

        assertThrows(IllegalStateException.class, () ->
                prescriptionService.telechargerOrdonnancePdf(500L, 20L, false));
    }
}
