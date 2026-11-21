package gest_cabinet_back.service.impl;

import gest_cabinet_back.dto.RendezVousMedecinResponseDTO;
import gest_cabinet_back.dto.RendezVousRequestDTO;
import gest_cabinet_back.dto.RendezVousResponseDTO;
import gest_cabinet_back.exception.BadRequestException;
import gest_cabinet_back.exception.ConflictException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RendezVousServiceImplTest {

    @Mock
    private RendezVousRepository rendezVousRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private MedecinRepository medecinRepository;
    @Mock
    private DisponibiliteMedecinRepository disponibiliteRepository;

    @InjectMocks
    private RendezVousServiceImpl rendezVousService;

    private Medecin medecin;
    private Patient patient;
    private DisponibiliteMedecin disponibilite;

    @BeforeEach
    void setUp() {
        medecin = new Medecin();
        medecin.setId(1L);
        medecin.setNom("Traore");
        medecin.setPrenom("Youssou");

        patient = new Patient();
        patient.setId(2L);
        patient.setCodePatient("PAT-100");
        patient.setNom("Diagne");
        patient.setPrenom("Mamadou");

        disponibilite = new DisponibiliteMedecin();
        disponibilite.setId(10L);
        disponibilite.setMedecin(medecin);
        disponibilite.setDateJour(LocalDate.now().plusDays(2));
        disponibilite.setHeureDebut(LocalTime.of(9, 0));
        disponibilite.setHeureFin(LocalTime.of(12, 0));
        disponibilite.setDureeCreneauMinutes(30);
    }

    // ================================================================
    // Test RG04 : Créneau futur & limite à 90 jours
    // ================================================================

    @Test
    void demanderRdv_RG04_refuseDatePassee() {
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(medecinRepository.findById(1L)).thenReturn(Optional.of(medecin));

        RendezVousRequestDTO dto = new RendezVousRequestDTO();
        dto.setMedecinId(1L);
        dto.setDateHeure(LocalDateTime.now().minusHours(2));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> rendezVousService.demanderRdv(dto, 2L));
        assertTrue(ex.getMessage().contains("futur"));
    }

    @Test
    void demanderRdv_RG04_refuseAuDelaDe90Jours() {
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(medecinRepository.findById(1L)).thenReturn(Optional.of(medecin));

        RendezVousRequestDTO dto = new RendezVousRequestDTO();
        dto.setMedecinId(1L);
        dto.setDateHeure(LocalDateTime.now().plusDays(95));

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> rendezVousService.demanderRdv(dto, 2L));
        assertTrue(ex.getMessage().contains("90 jours"));
    }

    // ================================================================
    // Test RG05 : Max 3 RDV à venir simultanément
    // ================================================================

    @Test
    void demanderRdv_RG05_refuseSiDeja3RdvAVenir() {
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(medecinRepository.findById(1L)).thenReturn(Optional.of(medecin));

        // 0 absence
        when(rendezVousRepository.countByPatientIdAndStatutAndDateHeureBetween(
                eq(2L), eq(StatutRdv.ABSENT), any(), any())).thenReturn(0L);

        // Déjà 3 RDV à venir
        when(rendezVousRepository.countByPatientIdAndDateHeureAfterAndStatutIn(
                eq(2L), any(LocalDateTime.class), anyList())).thenReturn(3L);

        RendezVousRequestDTO dto = new RendezVousRequestDTO();
        dto.setMedecinId(1L);
        dto.setDateHeure(LocalDateTime.now().plusDays(5).withHour(10).withMinute(0));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> rendezVousService.demanderRdv(dto, 2L));
        assertTrue(ex.getMessage().contains("limite maximale autorisée : 3"));
    }

    // ================================================================
    // Test RG07 : Blocage prise en ligne si >= 3 absences non justifiées
    // ================================================================

    @Test
    void demanderRdv_RG07_bloqueSiTroisAbsences() {
        when(patientRepository.findById(2L)).thenReturn(Optional.of(patient));
        when(medecinRepository.findById(1L)).thenReturn(Optional.of(medecin));

        // 3 absences sur les 12 derniers mois
        when(rendezVousRepository.countByPatientIdAndStatutAndDateHeureBetween(
                eq(2L), eq(StatutRdv.ABSENT), any(), any())).thenReturn(3L);

        RendezVousRequestDTO dto = new RendezVousRequestDTO();
        dto.setMedecinId(1L);
        dto.setDateHeure(LocalDateTime.now().plusDays(2).withHour(9).withMinute(0));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> rendezVousService.demanderRdv(dto, 2L));
        assertTrue(ex.getMessage().contains("bloqué"));
    }

    // ================================================================
    // Test RG06 : Annulation et modification patient (délai de 24h)
    // ================================================================

    @Test
    void annulerRdv_RG06_refuseSiMoinsDe24Heures() {
        RendezVous rdv = new RendezVous();
        rdv.setId(50L);
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);
        rdv.setStatut(StatutRdv.PLANIFIE);
        // Prévu dans 5 heures (< 24h)
        rdv.setDateHeure(LocalDateTime.now().plusHours(5));

        when(rendezVousRepository.findByIdAndPatientId(50L, 2L)).thenReturn(Optional.of(rdv));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> rendezVousService.annulerRdv(50L, 2L));
        assertTrue(ex.getMessage().contains("24 heures"));
    }

    @Test
    void annulerRdv_RG06_succesSiPlusDe24Heures() {
        RendezVous rdv = new RendezVous();
        rdv.setId(50L);
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);
        rdv.setStatut(StatutRdv.PLANIFIE);
        // Prévu dans 48 heures (> 24h)
        rdv.setDateHeure(LocalDateTime.now().plusHours(48));

        when(rendezVousRepository.findByIdAndPatientId(50L, 2L)).thenReturn(Optional.of(rdv));
        when(rendezVousRepository.save(any(RendezVous.class))).thenAnswer(i -> i.getArgument(0));

        rendezVousService.annulerRdv(50L, 2L);

        assertEquals(StatutRdv.ANNULE, rdv.getStatut());
        verify(rendezVousRepository).save(rdv);
    }

    @Test
    void modifierMonRdv_RG06_refuseSiMoinsDe24Heures() {
        RendezVous rdv = new RendezVous();
        rdv.setId(50L);
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);
        rdv.setStatut(StatutRdv.PLANIFIE);
        rdv.setDateHeure(LocalDateTime.now().plusHours(10));

        when(rendezVousRepository.findByIdAndPatientId(50L, 2L)).thenReturn(Optional.of(rdv));

        RendezVousRequestDTO dto = new RendezVousRequestDTO();
        dto.setMotif("Nouveau motif");

        ConflictException ex = assertThrows(ConflictException.class,
                () -> rendezVousService.modifierMonRdv(50L, dto, 2L));
        assertTrue(ex.getMessage().contains("24 heures"));
    }

    // ================================================================
    // Test Actions Secrétaire : marquer présence & absence
    // ================================================================

    @Test
    void marquerPresence_secretaire_passeAStatutHonorePresent() {
        RendezVous rdv = new RendezVous();
        rdv.setId(50L);
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);
        rdv.setStatut(StatutRdv.CONFIRME);

        when(rendezVousRepository.findById(50L)).thenReturn(Optional.of(rdv));
        when(rendezVousRepository.save(any(RendezVous.class))).thenAnswer(i -> i.getArgument(0));

        RendezVousMedecinResponseDTO resp = rendezVousService.marquerPresence(50L);

        assertEquals(StatutRdv.HONORE_PRESENT, resp.getStatut());
    }

    @Test
    void marquerAbsence_secretaire_passeAStatutAbsent() {
        RendezVous rdv = new RendezVous();
        rdv.setId(50L);
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);
        rdv.setStatut(StatutRdv.CONFIRME);

        when(rendezVousRepository.findById(50L)).thenReturn(Optional.of(rdv));
        when(rendezVousRepository.save(any(RendezVous.class))).thenAnswer(i -> i.getArgument(0));

        RendezVousMedecinResponseDTO resp = rendezVousService.marquerAbsence(50L);

        assertEquals(StatutRdv.ABSENT, resp.getStatut());
    }

    @Test
    void annulerRdvSecretaire_reussitMemeAMoinsDe24Heures() {
        RendezVous rdv = new RendezVous();
        rdv.setId(50L);
        rdv.setPatient(patient);
        rdv.setMedecin(medecin);
        rdv.setStatut(StatutRdv.CONFIRME);
        rdv.setDateHeure(LocalDateTime.now().plusHours(2)); // < 24h

        when(rendezVousRepository.findById(50L)).thenReturn(Optional.of(rdv));
        when(rendezVousRepository.save(any(RendezVous.class))).thenAnswer(i -> i.getArgument(0));

        RendezVousMedecinResponseDTO resp = rendezVousService.annulerRdvSecretaire(50L, "Annulé par secrétaire");

        assertEquals(StatutRdv.ANNULE, resp.getStatut());
    }

    @Test
    void getCreneauxDisponibles_refuseMedecinInexistant() {
        when(medecinRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RessourceNotFoundException.class,
                () -> rendezVousService.getCreneauxDisponibles(999L, LocalDate.now()));
    }
}
