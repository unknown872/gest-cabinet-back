package gest_cabinet_back.controller;

import gest_cabinet_back.dto.RendezVousMedecinResponseDTO;
import gest_cabinet_back.dto.RendezVousRequestDTO;
import gest_cabinet_back.dto.RendezVousResponseDTO;
import gest_cabinet_back.security.CustomUserDetails;
import gest_cabinet_back.service.RendezVousService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rendez-vous")
@RequiredArgsConstructor
public class RendezVousController {

    private final RendezVousService rendezVousService;

    // ================================================================
    //  Actions MEDECIN
    // ================================================================

    /**
     * POST /api/rendez-vous/medecin
     * Le médecin prend un RDV pour un patient (patientId + dateHeure + motif dans le body).
     */
    @PreAuthorize("hasRole('MEDECIN')")
    @PostMapping("/medecin")
    public ResponseEntity<RendezVousMedecinResponseDTO> prendreRdvPourPatient(
            @RequestBody RendezVousRequestDTO dto,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rendezVousService.prendreRdvPourPatient(dto, medecinId));
    }

    /**
     * GET /api/rendez-vous/medecin
     * Le médecin liste uniquement les RDV CONFIRMÉS qui le concernent.
     */
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/medecin")
    public ResponseEntity<List<RendezVousMedecinResponseDTO>> listerRdvDuMedecin(Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(rendezVousService.listerRdvDuMedecin(medecinId));
    }

    /**
     * GET /api/rendez-vous/medecin/{id}
     * Le médecin consulte le détail d'un RDV CONFIRMÉ qui le concerne.
     */
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/medecin/{id}")
    public ResponseEntity<RendezVousMedecinResponseDTO> consulterRdvMedecin(
            @PathVariable Long id,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(rendezVousService.consulterRdvMedecin(id, medecinId));
    }

    // ================================================================
    //  Actions SECRETAIRE
    // ================================================================

    /**
     * GET /api/rendez-vous/secretaire
     * La secrétaire consulte tous les rendez-vous du cabinet avec les infos des patients.
     */
    @PreAuthorize("hasAnyRole('SECRETAIRE')")
    @GetMapping("/secretaire")
    public ResponseEntity<List<RendezVousMedecinResponseDTO>> listerTousLesRdv() {
        return ResponseEntity.ok(rendezVousService.listerTousLesRdv());
    }

    /**
     * GET /api/rendez-vous/secretaire/{id}
     * La secrétaire consulte le détail d'un rendez-vous avec toutes les infos du patient.
     */
    @PreAuthorize("hasAnyRole('SECRETAIRE')")
    @GetMapping("/secretaire/{id}")
    public ResponseEntity<RendezVousMedecinResponseDTO> consulterRdvSecretaire(@PathVariable Long id) {
        return ResponseEntity.ok(rendezVousService.consulterRdvSecretaire(id));
    }

    /**
     * PATCH /api/rendez-vous/{id}/confirmer
     * La secrétaire (ou le Medecin) confirme un rendez-vous (statut -> CONFIRME).
     * Permet d'ajouter optionnellement des notes de confirmation dans le corps de la requête.
     */
    @PreAuthorize("hasAnyRole('SECRETAIRE', 'MEDECIN')")
    @PatchMapping("/{id}/confirmer")
    public ResponseEntity<RendezVousMedecinResponseDTO> confirmerRdv(
            @PathVariable Long id,
            @RequestBody(required = false) RendezVousRequestDTO dto) {
        String notes = (dto != null) ? dto.getNotes() : null;
        return ResponseEntity.ok(rendezVousService.confirmerRdv(id, notes));
    }

    /**
     * PATCH /api/rendez-vous/{id}/rejeter
     * La secrétaire (ou le personnel médical) rejette un rendez-vous (statut -> ABSENT).
     * Permet d'ajouter optionnellement des notes (motif du rejet / absence) dans le corps.
     */
    @PreAuthorize("hasAnyRole('SECRETAIRE', 'MEDECIN', 'ADMINISTRATEUR')")
    @PatchMapping("/{id}/rejeter")
    public ResponseEntity<RendezVousMedecinResponseDTO> rejeterRdv(
            @PathVariable Long id,
            @RequestBody(required = false) RendezVousRequestDTO dto) {
        String notes = (dto != null) ? dto.getNotes() : null;
        return ResponseEntity.ok(rendezVousService.rejeterRdv(id, notes));
    }

    /**
     * PATCH /api/rendez-vous/{id}/notes
     * Met à jour ou ajoute des notes sur un rendez-vous (accessible aux secrétaires et médecins ).
     */ 
    @PreAuthorize("hasAnyRole('SECRETAIRE', 'MEDECIN')")
    @PatchMapping("/{id}/notes")
    public ResponseEntity<RendezVousMedecinResponseDTO> modifierNotes(
            @PathVariable Long id,
            @RequestBody RendezVousRequestDTO dto) {
        return ResponseEntity.ok(rendezVousService.modifierNotes(id, dto.getNotes()));
    }

    // ================================================================
    //  Actions PATIENT
    // ================================================================

    /**
     * POST /api/rendez-vous
     * Le patient demande un RDV (medecinId + dateHeure + motif dans le body).
     */
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<RendezVousResponseDTO> demanderRdv(
            @RequestBody RendezVousRequestDTO dto,
            Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rendezVousService.demanderRdv(dto, patientId));
    }

    /**
     * GET /api/rendez-vous
     * Le patient liste ses propres RDV.
     */
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping
    public ResponseEntity<List<RendezVousResponseDTO>> listerMesRdv(Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.ok(rendezVousService.listerMesRdv(patientId));
    }

    /**
     * GET /api/rendez-vous/{id}
     * Le patient consulte le détail d'un de ses RDV.
     */
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/{id}")
    public ResponseEntity<RendezVousResponseDTO> consulterRdvPatient(
            @PathVariable Long id,
            Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.ok(rendezVousService.consulterRdv(id, patientId));
    }

    /**
     * PATCH /api/rendez-vous/{id}/annuler
     * Le patient annule un de ses RDV.
     */
    @PreAuthorize("hasRole('PATIENT')")
    @PatchMapping("/{id}/annuler")
    public ResponseEntity<Void> annulerRdv(
            @PathVariable Long id,
            Authentication authentication) {
        Long patientId = getId(authentication);
        rendezVousService.annulerRdv(id, patientId);
        return ResponseEntity.noContent().build();
    }

    // ================================================================
    //  Helper
    // ================================================================

    private Long getId(Authentication auth) {
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUtilisateur().getId();
    }
}
