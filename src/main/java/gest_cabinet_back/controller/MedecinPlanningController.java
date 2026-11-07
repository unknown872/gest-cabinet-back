package gest_cabinet_back.controller;

import gest_cabinet_back.dto.DisponibiliteRequestDTO;
import gest_cabinet_back.dto.DisponibiliteResponseDTO;
import gest_cabinet_back.service.PlanningService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medecin/disponibilites")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MEDECIN')")
public class MedecinPlanningController {

    private final PlanningService planningService;

    @Operation(summary = "Ajouter une plage de disponibilité pour le médecin connecté (c115)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DisponibiliteResponseDTO definirDisponibilite(
            Authentication authentication,
            @RequestBody DisponibiliteRequestDTO dto
    ) {
        return planningService.definirDisponibilite(authentication.getName(), dto);
    }

    @Operation(summary = "Lister les plages de disponibilité du médecin connecté (c115)")
    @GetMapping
    public List<DisponibiliteResponseDTO> listerMesDisponibilites(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return planningService.listerMesDisponibilites(authentication.getName(), date);
    }

    @Operation(summary = "Modifier une plage de disponibilité du médecin connecté (c115)")
    @PutMapping("/{id}")
    public DisponibiliteResponseDTO modifierDisponibilite(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody DisponibiliteRequestDTO dto
    ) {
        return planningService.modifierDisponibilite(authentication.getName(), id, dto);
    }

    @Operation(summary = "Supprimer une plage de disponibilité du médecin connecté (c115)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerDisponibilite(
            Authentication authentication,
            @PathVariable Long id
    ) {
        planningService.supprimerDisponibilite(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

}
