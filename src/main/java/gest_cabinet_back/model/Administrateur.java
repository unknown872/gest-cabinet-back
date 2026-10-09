package gest_cabinet_back.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor 
public class Administrateur extends Utilisateur {
    public Administrateur(Long id, String nom, String prenom, String genre, String email, String motDePasse, String telephone, boolean actif, Role role) {
        super(id, nom, prenom, genre, email, motDePasse, telephone, actif, role);
    }
}

