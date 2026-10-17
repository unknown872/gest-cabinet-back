package gest_cabinet_back.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "secretaire")
@Getter
@Setter
@NoArgsConstructor
public class Secretaire extends Utilisateur {
    @Column(unique = true)
    private String matricule;
}
