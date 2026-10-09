package gest_cabinet_back.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Specialite {
    private Long id;
    private String code;
    private String libelle;
    private String description;

    public Specialite(Long id, String code, String libelle, String description) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.description = description;
    }
}
