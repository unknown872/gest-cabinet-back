package gest_cabinet_back.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Facture {
    private Long id;

    private String numeroFacture;
    private LocalDateTime dateEmission;
    private Double montantTotal;
    private Double montantRegle;
    private StatutFacture statut;

    public Facture(Long id, String numeroFacture, LocalDateTime dateEmission, Double montantTotal, Double montantRegle, StatutFacture statut) {
        this.id = id;
        this.numeroFacture = numeroFacture;
        this.dateEmission = dateEmission;
        this.montantTotal = montantTotal;
        this.montantRegle = montantRegle;
        this.statut = statut;
    }
}
