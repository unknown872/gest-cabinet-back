package gest_cabinet_back.dto;


import gest_cabinet_back.model.Genre;
import gest_cabinet_back.model.Role;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
public class UtilisateurRequestDTO {

    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private String telephone;
    private Role role;
    private Genre genre;
    private String adresse;
    private LocalDate dateNaissance;

    //Medecin
    private String rpps;
    private String signatureNumerique;

    // Secretaire
    private String matricule;

    // Patient
    private String numSecuSocial;

}
