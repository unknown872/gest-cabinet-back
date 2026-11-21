package gest_cabinet_back.controller;

import gest_cabinet_back.dto.*;
import gest_cabinet_back.security.CustomUserDetails;
import gest_cabinet_back.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescriptions", description = "Gestion des prescriptions médicales et ordonnances (Module M5)")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    // ================================================================
    // Actions MEDECIN
    // ================================================================

    @Operation(summary = "Créer une prescription pour une consultation en cours (M5.1 / RG16)")
    @PreAuthorize("hasRole('MEDECIN')")
    @PostMapping("/consultation/{consultationId}")
    public ResponseEntity<PrescriptionResponseDTO> creerPrescription(
            @PathVariable Long consultationId,
            @RequestBody(required = false) PrescriptionCreateRequestDTO dto,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.creerPrescription(consultationId, dto, medecinId));
    }

    @Operation(summary = "Modifier les informations générales d'une prescription non validée (M5.2, M5.4)")
    @PreAuthorize("hasRole('MEDECIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PrescriptionResponseDTO> modifierPrescription(
            @PathVariable Long id,
            @RequestBody PrescriptionUpdateRequestDTO dto,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(prescriptionService.modifierPrescription(id, dto, medecinId));
    }

    @Operation(summary = "Ajouter une ligne de médicament à une prescription non validée (M5.2, M5.3)")
    @PreAuthorize("hasRole('MEDECIN')")
    @PostMapping("/{id}/lignes")
    public ResponseEntity<PrescriptionResponseDTO> ajouterLigne(
            @PathVariable Long id,
            @RequestBody LignePrescriptionRequestDTO dto,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.ajouterLigne(id, dto, medecinId));
    }

    @Operation(summary = "Modifier une ligne de médicament d'une prescription non validée (M5.2, M5.3)")
    @PreAuthorize("hasRole('MEDECIN')")
    @PutMapping("/{id}/lignes/{ligneId}")
    public ResponseEntity<PrescriptionResponseDTO> modifierLigne(
            @PathVariable Long id,
            @PathVariable Long ligneId,
            @RequestBody LignePrescriptionRequestDTO dto,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(prescriptionService.modifierLigne(id, ligneId, dto, medecinId));
    }

    @Operation(summary = "Supprimer une ligne de médicament d'une prescription non validée (M5.2)")
    @PreAuthorize("hasRole('MEDECIN')")
    @DeleteMapping("/{id}/lignes/{ligneId}")
    public ResponseEntity<PrescriptionResponseDTO> supprimerLigne(
            @PathVariable Long id,
            @PathVariable Long ligneId,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(prescriptionService.supprimerLigne(id, ligneId, medecinId));
    }

    @Operation(summary = "Valider la prescription, la rendant immuable et générant l'ordonnance PDF (M5.5 / RG17 / RG18)")
    @PreAuthorize("hasRole('MEDECIN')")
    @PatchMapping("/{id}/valider")
    public ResponseEntity<PrescriptionResponseDTO> validerPrescription(
            @PathVariable Long id,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(prescriptionService.validerPrescription(id, medecinId));
    }

    @Operation(summary = "Annuler une prescription en cours (non encore validée)")
    @PreAuthorize("hasRole('MEDECIN')")
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<PrescriptionResponseDTO> annulerPrescription(
            @PathVariable Long id,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(prescriptionService.annulerPrescription(id, medecinId));
    }

    @Operation(summary = "Consulter le détail d'une prescription par un médecin (RG11 prise en charge)")
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PrescriptionResponseDTO> consulterPrescriptionMedecin(
            @PathVariable Long id,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(prescriptionService.consulterPrescriptionMedecin(id, medecinId));
    }

    @Operation(summary = "Lister les prescriptions d'une consultation (Médecin)")
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/consultation/{consultationId}")
    public ResponseEntity<List<PrescriptionResponseDTO>> listerPrescriptionsParConsultation(
            @PathVariable Long consultationId,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(prescriptionService.listerPrescriptionsParConsultation(consultationId, medecinId));
    }

    @Operation(summary = "Consulter l'historique des prescriptions d'un patient (M5.7 / RG11)")
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionResponseDTO>> listerPrescriptionsParPatient(
            @PathVariable Long patientId,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(prescriptionService.listerPrescriptionsParPatient(patientId, medecinId));
    }

    @Operation(summary = "Lister toutes mes prescriptions créées en tant que médecin")
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/medecin")
    public ResponseEntity<List<PrescriptionResponseDTO>> listerPrescriptionsDuMedecin(
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(prescriptionService.listerPrescriptionsDuMedecin(medecinId));
    }

    // ================================================================
    // Actions PATIENT
    // ================================================================

    @Operation(summary = "Lister mes ordonnances validées (Patient connecté, M5.8 / M8.1.3)")
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/mes-prescriptions")
    public ResponseEntity<List<PrescriptionResponseDTO>> listerMesPrescriptions(
            Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.ok(prescriptionService.listerMesPrescriptions(patientId));
    }

    @Operation(summary = "Consulter le détail d'une de mes prescriptions (Patient connecté, M5.8)")
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/mes-prescriptions/{id}")
    public ResponseEntity<PrescriptionResponseDTO> consulterMaPrescription(
            @PathVariable Long id,
            Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.ok(prescriptionService.consulterMaPrescription(id, patientId));
    }

    // ================================================================
    // Téléchargement Ordonnance PDF (Médecin & Patient autorisés)
    // ================================================================

    @Operation(summary = "Télécharger l'ordonnance médicale au format PDF (M5.6 / M5.8 / CA06)")
    @PreAuthorize("hasAnyRole('MEDECIN', 'PATIENT')")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> telechargerOrdonnancePdf(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = getId(authentication);
        boolean isMedecin = isMedecin(authentication);

        byte[] pdfBytes = prescriptionService.telechargerOrdonnancePdf(id, userId, isMedecin);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "ordonnance_" + id + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    // ================================================================
    // Helpers
    // ================================================================

    private Long getId(Authentication auth) {
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUtilisateur().getId();
    }

    private boolean isMedecin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MEDECIN"));
    }
}
