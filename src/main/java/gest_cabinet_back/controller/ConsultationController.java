package gest_cabinet_back.controller;

import gest_cabinet_back.dto.ConsultationMedecinResponseDTO;
import gest_cabinet_back.dto.ConsultationRequestDTO;
import gest_cabinet_back.dto.ConsultationResponseDTO;
import gest_cabinet_back.dto.ObservationComplementaireRequestDTO;
import gest_cabinet_back.model.StatutConsultation;
import gest_cabinet_back.security.CustomUserDetails;
import gest_cabinet_back.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
@Tag(name = "Consultations", description = "Gestion des consultations médicales (Module M4)")
public class ConsultationController {

    private final ConsultationService consultationService;

    // ================================================================
    //  Actions MEDECIN
    // ================================================================

    @Operation(summary = "Démarrer une consultation à partir d'un rendez-vous confirmé (M4.1 / RG12)")
    @PreAuthorize("hasRole('MEDECIN')")
    @PostMapping("/rendez-vous/{rendezVousId}")
    public ResponseEntity<ConsultationMedecinResponseDTO> demarrerConsultation(
            @PathVariable Long rendezVousId,
            @RequestBody(required = false) ConsultationRequestDTO dto,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(consultationService.demarrerConsultation(rendezVousId, dto, medecinId));
    }

    @Operation(summary = "Saisir ou modifier les informations cliniques d'une consultation en cours (M4.2–M4.4)")
    @PreAuthorize("hasRole('MEDECIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ConsultationMedecinResponseDTO> modifierConsultation(
            @PathVariable Long id,
            @RequestBody ConsultationRequestDTO dto,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(consultationService.modifierConsultation(id, dto, medecinId));
    }

    @Operation(summary = "Clôturer une consultation en cours et marquer le rendez-vous honoré (M4.6 / RG13)")
    @PreAuthorize("hasRole('MEDECIN')")
    @PatchMapping("/{id}/cloturer")
    public ResponseEntity<ConsultationMedecinResponseDTO> cloturerConsultation(
            @PathVariable Long id,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(consultationService.cloturerConsultation(id, medecinId));
    }

    @Operation(summary = "Annuler une consultation en cours")
    @PreAuthorize("hasRole('MEDECIN')")
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<ConsultationMedecinResponseDTO> annulerConsultation(
            @PathVariable Long id,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(consultationService.annulerConsultation(id, medecinId));
    }

    @Operation(summary = "Ajouter une observation complémentaire sur une consultation clôturée (M4.7 / RG13)")
    @PreAuthorize("hasRole('MEDECIN')")
    @PostMapping("/{id}/observations")
    public ResponseEntity<ConsultationMedecinResponseDTO> ajouterObservationComplementaire(
            @PathVariable Long id,
            @RequestBody ObservationComplementaireRequestDTO dto,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(
                consultationService.ajouterObservationComplementaire(id, dto, medecinId));
    }

    @Operation(summary = "Lister les consultations du médecin connecté (filtrable par statut)")
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/medecin")
    public ResponseEntity<List<ConsultationMedecinResponseDTO>> listerConsultationsDuMedecin(
            @RequestParam(required = false) StatutConsultation statut,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(consultationService.listerConsultationsDuMedecin(medecinId, statut));
    }

    @Operation(summary = "Consulter le détail d'une consultation (médecin avec prise en charge, RG11)")
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/medecin/{id}")
    public ResponseEntity<ConsultationMedecinResponseDTO> consulterConsultationMedecin(
            @PathVariable Long id,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(consultationService.consulterConsultationMedecin(id, medecinId));
    }

    @Operation(summary = "Lister l'historique des consultations d'un patient pris en charge (M3.4 / RG11)")
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<ConsultationMedecinResponseDTO>> listerConsultationsParPatient(
            @PathVariable Long patientId,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(consultationService.listerConsultationsParPatient(patientId, medecinId));
    }

    // ================================================================
    //  Actions PATIENT
    // ================================================================

    @Operation(summary = "Lister mes consultations (patient connecté, lecture seule)")
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping
    public ResponseEntity<List<ConsultationResponseDTO>> listerMesConsultations(
            Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.ok(consultationService.listerMesConsultations(patientId));
    }

    @Operation(summary = "Consulter le détail d'une de mes consultations (patient connecté, CA02)")
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponseDTO> consulterMaConsultation(
            @PathVariable Long id,
            Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.ok(consultationService.consulterMaConsultation(id, patientId));
    }

    // ================================================================
    //  Helper
    // ================================================================

    private Long getId(Authentication auth) {
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUtilisateur().getId();
    }
}
