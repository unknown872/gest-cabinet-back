package gest_cabinet_back.dto;

import gest_cabinet_back.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfileResponseDTO {
    private String nom;
    private String prenom;
    private String email;
    private Role role;
}
