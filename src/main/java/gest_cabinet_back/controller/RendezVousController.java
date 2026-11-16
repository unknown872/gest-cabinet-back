package gest_cabinet_back.controller;

import gest_cabinet_back.dto.RendezVousMedecinResponseDTO;
import gest_cabinet_back.dto.RendezVousRequestDTO;
import gest_cabinet_back.dto.RendezVousResponseDTO;
import gest_cabinet_back.security.CustomUserDetails;
import gest_cabinet_back.service.RendezVousService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Rendez-vous", description = "Gestion des rendez-vous et des créneaux (Module M2)")
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
    @Operation(summary = "Consulter les créneaux disponibles d'un médecin pour une date donnée (M2.3.2 / M2.2.2)")
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

    @Operation(summary = "Prendre un rendez-vous pour un patient (médecin)")
    @PreAuthorize("hasRole('MEDECIN')")
    @PostMapping("/medecin")
    public ResponseEntity<RendezVousMedecinResponseDTO> prendreRdvPourPatient(
            @RequestBody RendezVousRequestDTO dto,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rendezVousService.prendreRdvPourPatient(dto, medecinId));
    }

    @Operation(summary = "Lister les rendez-vous confirmés du médecin connecté")
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/medecin")
    public ResponseEntity<List<RendezVousMedecinResponseDTO>> listerRdvDuMedecin(Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(rendezVousService.listerRdvDuMedecin(medecinId));
    }

    @Operation(summary = "Consulter le planning des rendez-vous d'une journée pour le médecin connecté (M2.1.2)")
    @PreAuthorize("hasRole('MEDECIN')")
    @GetMapping("/medecin/planning")
    public ResponseEntity<List<RendezVousMedecinResponseDTO>> listerPlanningMedecin(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        Long medecinId = getId(authentication);
        return ResponseEntity.ok(rendezVousService.listerPlanningMedecin(medecinId, date));
    }

    @Operation(summary = "Consulter le détail d'un rendez-vous confirmé du médecin connecté")
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

    @Operation(summary = "Créer et confirmer directement un rendez-vous pour un patient (M2.2.2)")
    @PreAuthorize("hasAnyRole('SECRETAIRE')")
    @PostMapping("/secretaire")
    public ResponseEntity<RendezVousMedecinResponseDTO> creerRdvParSecretaire(
            @RequestBody RendezVousRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rendezVousService.creerRdvParSecretaire(dto));
    }

    @Operation(summary = "Lister tous les rendez-vous du cabinet (planning consolidé)")
    @PreAuthorize("hasAnyRole('SECRETAIRE')")
    @GetMapping("/secretaire")
    public ResponseEntity<List<RendezVousMedecinResponseDTO>> listerTousLesRdv() {
        return ResponseEntity.ok(rendezVousService.listerTousLesRdv());
    }

    @Operation(summary = "Consulter la liste des rendez-vous d'une journée / file d'attente (M2.2.5)")
    @PreAuthorize("hasAnyRole('SECRETAIRE')")
    @GetMapping("/secretaire/jour")
    public ResponseEntity<List<RendezVousMedecinResponseDTO>> listerRdvDuJour(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(rendezVousService.listerRdvDuJour(date));
    }

    @Operation(summary = "Consulter le détail d'un rendez-vous (vue secrétaire)")
    @PreAuthorize("hasAnyRole('SECRETAIRE')")
    @GetMapping("/secretaire/{id}")
    public ResponseEntity<RendezVousMedecinResponseDTO> consulterRdvSecretaire(@PathVariable Long id) {
        return ResponseEntity.ok(rendezVousService.consulterRdvSecretaire(id));
    }

    @Operation(summary = "Confirmer un rendez-vous planifié")
    @PreAuthorize("hasAnyRole('SECRETAIRE', 'MEDECIN')")
    @PatchMapping("/{id}/confirmer")
    public ResponseEntity<RendezVousMedecinResponseDTO> confirmerRdv(
            @PathVariable Long id,
            @RequestBody(required = false) RendezVousRequestDTO dto) {
        String notes = (dto != null) ? dto.getNotes() : null;
        return ResponseEntity.ok(rendezVousService.confirmerRdv(id, notes));
    }

    @Operation(summary = "Marquer le patient comme présent à son arrivée au cabinet (M2.2.4)")
    @PreAuthorize("hasAnyRole('SECRETAIRE', 'MEDECIN')")
    @PatchMapping("/{id}/marquer-presence")
    public ResponseEntity<RendezVousMedecinResponseDTO> marquerPresence(@PathVariable Long id) {
        return ResponseEntity.ok(rendezVousService.marquerPresence(id));
    }

    @Operation(summary = "Marquer le patient comme absent (non présenté) (M2.2.4 / RG07)")
    @PreAuthorize("hasAnyRole('SECRETAIRE', 'MEDECIN')")
    @PatchMapping("/{id}/marquer-absence")
    public ResponseEntity<RendezVousMedecinResponseDTO> marquerAbsence(@PathVariable Long id) {
        return ResponseEntity.ok(rendezVousService.marquerAbsence(id));
    }

    @Operation(summary = "Annuler un rendez-vous par la secrétaire ou le Medecin (sans restriction 24h, RG06)")
    @PreAuthorize("hasAnyRole('SECRETAIRE','MEDECIN')")
    @PatchMapping("/secretaire/{id}/annuler")
    public ResponseEntity<RendezVousMedecinResponseDTO> annulerRdvSecretaire(
            @PathVariable Long id,
            @RequestBody(required = false) RendezVousRequestDTO dto) {
        String notes = (dto != null) ? dto.getNotes() : null;
        return ResponseEntity.ok(rendezVousService.annulerRdvSecretaire(id, notes));
    }

    @Operation(summary = "Modifier ou reporter un rendez-vous par la secrétaire (M2.2.3)")
    @PreAuthorize("hasAnyRole('SECRETAIRE')")
    @PutMapping("/secretaire/{id}")
    public ResponseEntity<RendezVousMedecinResponseDTO> modifierRdvSecretaire(
            @PathVariable Long id,
            @RequestBody RendezVousRequestDTO dto) {
        return ResponseEntity.ok(rendezVousService.modifierRdvSecretaire(id, dto));
    }

    @Operation(summary = "Modifier les notes administratives ou internes d'un rendez-vous")
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

    @Operation(summary = "Demander / réserver un créneau de rendez-vous en ligne (M2.3.3 / RG03, RG04, RG05, RG07)")
    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<RendezVousResponseDTO> demanderRdv(
            @RequestBody RendezVousRequestDTO dto,
            Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rendezVousService.demanderRdv(dto, patientId));
    }

    @Operation(summary = "Lister mes rendezvous (patient connecté, lecture seule)")
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping
    public ResponseEntity<List<RendezVousResponseDTO>> listerMesRdv(Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.ok(rendezVousService.listerMesRdv(patientId));
    }

    @Operation(summary = "Consulter le détail d'un de mes rendez-vous (patient connecté)")
    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/{id}")
    public ResponseEntity<RendezVousResponseDTO> consulterRdvPatient(
            @PathVariable Long id,
            Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.ok(rendezVousService.consulterRdv(id, patientId));
    }

    @Operation(summary = "Modifier ou reporter mon rendez-vous (patient connecté, jusqu'à 24h avant, RG06)")
    @PreAuthorize("hasRole('PATIENT')")
    @PutMapping("/{id}")
    public ResponseEntity<RendezVousResponseDTO> modifierMonRdv(
            @PathVariable Long id,
            @RequestBody RendezVousRequestDTO dto,
            Authentication authentication) {
        Long patientId = getId(authentication);
        return ResponseEntity.ok(rendezVousService.modifierMonRdv(id, dto, patientId));
    }

    @Operation(summary = "Annuler mon rendez-vous (patient connecté, jusqu'à 24h avant, RG06)")
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
