package gest_cabinet_back.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LignePrescription {
    private Long id;

    private String medicament;
    private String posologie;
    private String dureeTraitement;
    private String instructions;

    public LignePrescription(Long id, String medicament, String posologie, String dureeTraitement, String instructions) {
        this.id = id;
        this.medicament = medicament;
        this.posologie = posologie;
        this.dureeTraitement = dureeTraitement;
        this.instructions = instructions;
    }
}
