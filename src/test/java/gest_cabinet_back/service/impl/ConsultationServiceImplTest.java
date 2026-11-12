package gest_cabinet_back.service.impl;

import gest_cabinet_back.dto.ConsultationMedecinResponseDTO;
import gest_cabinet_back.dto.ConsultationRequestDTO;
import gest_cabinet_back.dto.ConsultationResponseDTO;
import gest_cabinet_back.dto.ObservationComplementaireRequestDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceImplTest {

    @Mock
    private ConsultationRepository consultationRepository;
    @Mock
    private RendezVousRepository rendezVousRepository;
    @Mock
    private DossierMedicalRepository dossierMedicalRepository;
    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private ConsultationServiceImpl consultationService;

    private Medecin medecin;
    private Patient patient;
    private RendezVous rdv;
    private DossierMedical dossier;

    @BeforeEach
    void setUp() {
        medecin = new Medecin();
        medecin.setId(10L);
        medecin.setNom("Diallo");
        medecin.setPrenom("Moussa");

        patient = new Patient();
        patient.setId(20L);
        patient.setCodePatient("PAT-001");
        patient.setNom("Ndiaye");
        patient.setPrenom("Awa");

        rdv = new RendezVous();
        rdv.setId(100L);
        rdv.setDateHeure(LocalDateTime.now());
        rdv.setMotif("Douleurs abdominales");
        rdv.setStatut(StatutRdv.CONFIRME);
        rdv.setMedecin(medecin);
        rdv.setPatient(patient);

        dossier = new DossierMedical();
        dossier.setId(30L);
        dossier.setNumeroDossier("DM-20-ABC123");
        dossier.setPatient(patient);
    }

    @Test
    void demarrerConsultation_succes_creeConsultationEnCours() {
        when(rendezVousRepository.findById(100L)).thenReturn(Optional.of(rdv));
        when(consultationRepository.existsByRendezVousId(100L)).thenReturn(false);
        when(dossierMedicalRepository.findByPatientId(20L)).thenReturn(Optional.of(dossier));
        when(consultationRepository.save(any(Consultation.class))).thenAnswer(inv -> {
            Consultation c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        ConsultationRequestDTO dto = new ConsultationRequestDTO(
                null, "Fièvre depuis 2 jours", null, 12.5, 65.0, 38.4);

        ConsultationMedecinResponseDTO result = consultationService.demarrerConsultation(100L, dto, 10L);

        assertEquals(1L, result.getId());
        assertEquals(StatutConsultation.EN_COURS, result.getStatut());
        assertEquals("Douleurs abdominales", result.getMotif());
        assertEquals(38.4, result.getTemperature());
        assertEquals(30L, result.getDossierMedicalId());
    }

    @Test
    void demarrerConsultation_refuseSiRdvNonConfirme() {
        rdv.setStatut(StatutRdv.PLANIFIE);
        when(rendezVousRepository.findById(100L)).thenReturn(Optional.of(rdv));

        assertThrows(IllegalStateException.class,
                () -> consultationService.demarrerConsultation(100L, null, 10L));
    }

    @Test
    void demarrerConsultation_refuseSiAutreMedecin() {
        when(rendezVousRepository.findById(100L)).thenReturn(Optional.of(rdv));

        assertThrows(AccessDeniedException.class,
                () -> consultationService.demarrerConsultation(100L, null, 999L));
    }

    @Test
    void modifierConsultation_refuseSiCloturee() {
        Consultation consultation = new Consultation();
        consultation.setId(1L);
        consultation.setStatut(StatutConsultation.TERMINEE);
        consultation.setRendezVous(rdv);
        consultation.setDossierMedical(dossier);

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        ConsultationRequestDTO dto = new ConsultationRequestDTO(
                "Nouveau motif", "Obs", "Diag", null, null, null);

        assertThrows(IllegalStateException.class,
                () -> consultationService.modifierConsultation(1L, dto, 10L));
    }

    @Test
    void cloturerConsultation_passeTermineeEtRdvHonore() {
        Consultation consultation = new Consultation();
        consultation.setId(1L);
        consultation.setStatut(StatutConsultation.EN_COURS);
        consultation.setRendezVous(rdv);
        consultation.setDossierMedical(dossier);

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(consultationRepository.save(any(Consultation.class))).thenAnswer(inv -> inv.getArgument(0));

        ConsultationMedecinResponseDTO result = consultationService.cloturerConsultation(1L, 10L);

        assertEquals(StatutConsultation.TERMINEE, result.getStatut());
        assertEquals(StatutRdv.HONORE_PRESENT, rdv.getStatut());
        verify(rendezVousRepository).save(rdv);
    }

    @Test
    void ajouterObservationComplementaire_preserveHistoriqueSurConsultationTerminee() {
        Consultation consultation = new Consultation();
        consultation.setId(1L);
        consultation.setStatut(StatutConsultation.TERMINEE);
        consultation.setObservations("Observation initiale");
        consultation.setRendezVous(rdv);
        consultation.setDossierMedical(dossier);

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(consultationRepository.save(any(Consultation.class))).thenAnswer(inv -> inv.getArgument(0));

        ConsultationMedecinResponseDTO result = consultationService.ajouterObservationComplementaire(
                1L, new ObservationComplementaireRequestDTO("Précision post-bilan"), 10L);

        assertTrue(result.getObservations().startsWith("Observation initiale"));
        assertTrue(result.getObservations().contains("Précision post-bilan"));
        assertTrue(result.getObservations().contains("Dr Diallo Moussa"));
    }

    @Test
    void consulterMaConsultation_refuseAutrePatient() {
        Consultation consultation = new Consultation();
        consultation.setId(1L);
        consultation.setRendezVous(rdv);
        consultation.setDossierMedical(dossier);

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        assertThrows(AccessDeniedException.class,
                () -> consultationService.consulterMaConsultation(1L, 999L));

        ConsultationResponseDTO own = consultationService.consulterMaConsultation(1L, 20L);
        assertEquals(1L, own.getId());
        assertEquals("Diallo", own.getNomMedecin());
    }

    @Test
    void listerConsultationsParPatient_refuseSansPriseEnCharge() {
        when(patientRepository.existsById(20L)).thenReturn(true);
        when(rendezVousRepository.existsByPatientIdAndMedecinId(20L, 999L)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> consultationService.listerConsultationsParPatient(20L, 999L));
    }
}
