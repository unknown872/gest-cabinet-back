package gest_cabinet_back.dto;

import gest_cabinet_back.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String email;
    private Role role;
}
