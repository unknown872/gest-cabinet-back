package gest_cabinet_back.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter 
@Setter 
@NoArgsConstructor 
public class Medecin extends Utilisateur {

    private String rpps;
    private String signatureNumerique;

    public Medecin(Long id, String nom, String prenom, String genre, String email, String motDePasse, String telephone, boolean actif, Role role, String rpps, String signatureNumerique) {
        super(id, nom, prenom, genre, email, motDePasse, telephone, actif, role);
        this.rpps = rpps;
        this.signatureNumerique = signatureNumerique;
    }

}
