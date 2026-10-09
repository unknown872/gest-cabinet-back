package gest_cabinet_back.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public class Secretaire extends Utilisateur {
    private String matricule;

    public Secretaire(Long id, String nom, String prenom, String genre, String email, String motDePasse, String telephone, boolean actif, Role role, String matricule) {
        super(id, nom, prenom, genre, email, motDePasse, telephone, actif, role);
        this.matricule = matricule;
    }
}
