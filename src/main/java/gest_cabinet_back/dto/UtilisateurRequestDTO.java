package gest_cabinet_back.dto;


import gest_cabinet_back.model.Genre;
import gest_cabinet_back.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
public class UtilisateurRequestDTO {

    @NotBlank(message = "Le nom est obligatoire.")
    private String nom;
    @NotBlank(message = "Le prénom est obligatoire.")
    private String prenom;
    @NotBlank(message = "L'adresse email est obligatoire.")
    @Email(message = "L'adresse email n'est pas valide.")
    private String email;
    @NotBlank(message = "Le mot de passe est obligatoire.")
    private String motDePasse;
    @NotBlank(message = "Le numéro de téléphone est obligatoire.")
    private String telephone;
    private Role role;
    @NotNull(message = "Le genre est obligatoire.")
    private Genre genre;
    @NotBlank(message = "L'adresse est obligatoire.")
    private String adresse;
    @NotNull(message = "La date de naissance est obligatoire.")
    private LocalDate dateNaissance;

    //Medecin
    private String rpps;
    private String signatureNumerique;

    // Secretaire
    private String matricule;

    // Patient
    private String numSecuSocial;

}
