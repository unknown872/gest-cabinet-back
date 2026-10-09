package gest_cabinet_back.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Reglement {
    private Long id;

    private LocalDateTime dateReglement;
    private Float montant;
    private ModePaiement modePaiement;
    private String reference;

    public Reglement(Long id, LocalDateTime dateReglement, Float montant, ModePaiement modePaiement, String reference) {
        this.id = id;
        this.dateReglement = dateReglement;
        this.montant = montant;
        this.modePaiement = modePaiement;
        this.reference = reference;
    }
    
}
