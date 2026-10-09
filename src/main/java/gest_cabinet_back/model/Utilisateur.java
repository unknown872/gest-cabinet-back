package gest_cabinet_back.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@Getter
@Setter
@NoArgsConstructor
public abstract class Utilisateur {
 
    private Long id;
 
    private String nom;
 
    private String prenom;

    private String genre;
 
    private String email;
 
    private String motDePasse;
 
    private String telephone;
 
    private boolean actif;
 
    private Role role;

    public Utilisateur(Long id, String nom, String prenom, String genre, String email, String motDePasse, String telephone, boolean actif, Role role) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.genre = genre;
        this.email = email;
        this.motDePasse = motDePasse;
        this.telephone = telephone;
        this.actif = actif;
        this.role = role;
    }
}
 