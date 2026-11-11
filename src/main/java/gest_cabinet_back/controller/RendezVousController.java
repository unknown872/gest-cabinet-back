package gest_cabinet_back.controller;

import gest_cabinet_back.dto.RendezVousMedecinResponseDTO;
import gest_cabinet_back.dto.RendezVousRequestDTO;
import gest_cabinet_back.dto.RendezVousResponseDTO;
import gest_cabinet_back.security.CustomUserDetails;
import gest_cabinet_back.service.RendezVousService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rendez-vous")
@RequiredArgsConstructor
public class RendezVousController {

    private final RendezVousService rendezVousService;

    // ================================================================
    //  Créneaux disponibles (M2.3.2 / M2.2.2)
    // ================================================================

    /**
     * GET /api/rendez-vous/creneaux-disponibles?medecinId=X&date=YYYY-MM-DD
     * Renvoie la liste des créneaux (date + heure) encore libres pour ce médecin
     * ce jour-là, calculée à partir de ses disponibilités déclarées moins les
     * rendez-vous déjà pris. Le patient (ou la secrétaire) choisit une valeur
     * de cette liste et la recopie telle quelle dans dateHeure au moment de réserver.
     */
    @PreAuthorize("hasAnyRole('PATIENT', 'SECRETAIRE', 'MEDECIN')")
    @GetMapping("/creneaux-disponibles")
    public ResponseEntity<List<LocalDateTime>> getCreneauxDisponibles(
            @RequestParam Long medecinId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(rendezVousService.getCreneauxDisponibles(medecinId, date));
    }

    // ================================================================
    //  Actions MEDECIN
    // ================================================================

    @PreAuthorize("hasRole('MEDECIN')")
    @PostMapping("/medecin")
    public ResponseEntity<RendezVousMedecinResponseDTO> prendreRdvPourPatient(
            @RequestBody RendezVousRequestDTO dto,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rendezVousService.prendreRdvPourPatient(dto, medecinId));
    }

    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/medecin")
    public ResponseEntity<List<RendezVousMedecinResponseDTO>> listerRdvDuMedecin(Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(rendezVousService.listerRdvDuMedecin(medecinId));
    }

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

    @PreAuthorize("hasAnyRole('SECRETAIRE')")
    @GetMapping("/secretaire")
    public ResponseEntity<List<RendezVousMedecinResponseDTO>> listerTousLesRdv() {
        return ResponseEntity.ok(rendezVousService.listerTousLesRdv());
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE')")
    @GetMapping("/secretaire/{id}")
    public ResponseEntity<RendezVousMedecinResponseDTO> consulterRdvSecretaire(@PathVariable Long id) {
        return ResponseEntity.ok(rendezVousService.consulterRdvSecretaire(id));
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE', 'MEDECIN')")
    @PatchMapping("/{id}/confirmer")
    public ResponseEntity<RendezVousMedecinResponseDTO> confirmerRdv(
            @PathVariable Long id,
            @RequestBody(required = false) RendezVousRequestDTO dto) {
        String notes = (dto != null) ? dto.getNotes() : null;
        return ResponseEntity.ok(rendezVousService.confirmerRdv(id, notes));
    }

    @PreAuthorize("hasAnyRole('SECRETAIRE', 'MEDECIN', 'ADMINISTRATEUR')")
    @PatchMapping("/{id}/rejeter")
    public ResponseEntity<RendezVousMedecinResponseDTO> rejeterRdv(
            @PathVariable Long id,
            @RequestBody(required = false) RendezVousRequestDTO dto) {
        String notes = (dto != null) ? dto.getNotes() : null;
        return ResponseEntity.ok(rendezVousService.rejeterRdv(id, notes));
    }

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

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<RendezVousResponseDTO> demanderRdv(
            @RequestBody RendezVousRequestDTO dto,
            Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rendezVousService.demanderRdv(dto, patientId));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping
    public ResponseEntity<List<RendezVousResponseDTO>> listerMesRdv(Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.ok(rendezVousService.listerMesRdv(patientId));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/{id}")
    public ResponseEntity<RendezVousResponseDTO> consulterRdvPatient(
            @PathVariable Long id,
            Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.ok(rendezVousService.consulterRdv(id, patientId));
    }

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