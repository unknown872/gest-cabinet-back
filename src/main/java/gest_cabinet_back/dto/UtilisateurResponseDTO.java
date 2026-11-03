package gest_cabinet_back.dto;

import gest_cabinet_back.model.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class UtilisateurResponseDTO {

    private Long id;
    private String code;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private boolean actif;
    private Role role;

}
