package gest_cabinet_back.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActeMedical {
    private Long id;
    private String code;
    private String libelle;
    private String description;
    private Double tarifBase;
    private Boolean remboursable;

    public ActeMedical(Long id, String code, String libelle, String description, Double tarifBase, Boolean remboursable) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.description = description;
        this.tarifBase = tarifBase;
        this.remboursable = remboursable;
    }
}