package gest_cabinet_back.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LigneFacture {
    private Long id;

    private String designation;
    private Integer quantite;
    private Double prixUnitaire;
    private Double totalLigne;

    public LigneFacture(Long id, String designation, Integer quantite, Double prixUnitaire, Double totalLigne) {
        this.id = id;
        this.designation = designation;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.totalLigne = totalLigne;
    }
}
