package gest_cabinet_back.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class Patient extends Utilisateur {

    private String numSecuSocial;
    private LocalDate dateNaissance;

    public Patient(Long id, String nom, String prenom, String genre, String email, String motDePasse, String telephone, boolean actif, Role role, String numSecuSocial, LocalDate dateNaissance) {
        super(id, nom, prenom, genre, email, motDePasse, telephone, actif, role);
        this.numSecuSocial = numSecuSocial;
        this.dateNaissance = dateNaissance;
    }
    
}
