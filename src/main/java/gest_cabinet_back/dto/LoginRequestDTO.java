package gest_cabinet_back.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
    @NotBlank(message = "L'adresse email est obligatoire.")
    @Email(message = "L'adresse email n'est pas valide.")
    private String email;
    @NotBlank(message = "Le mot de passe est obligatoire.")
    private String motDePasse;
}
