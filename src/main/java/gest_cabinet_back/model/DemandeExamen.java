package gest_cabinet_back.model;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter
@NoArgsConstructor
public class DemandeExamen {
    private Long id;

    private String typeExamen;
    private LocalDateTime dateDemande;
    private String consignes;
    private StatutExamen statut;

    public DemandeExamen(Long id, String typeExamen, LocalDateTime dateDemande, String consignes, StatutExamen statut) {
        this.id = id;
        this.typeExamen = typeExamen;
        this.dateDemande = dateDemande;
        this.consignes = consignes;
        this.statut = statut;
    }

}
