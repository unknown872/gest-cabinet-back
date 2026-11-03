package gest_cabinet_back.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "administrateur")
@Getter
@Setter
@NoArgsConstructor 
public class Administrateur extends Utilisateur {
    @Column(unique = true)
    private String codeAdministrateur;
}

