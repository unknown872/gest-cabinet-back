package gest_cabinet_back.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
public class Patient extends Utilisateur {

    @Column(unique = true)
    private String codePatient;

    @Column(unique = true)
    private String numSecuSocial;

    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private DossierMedical dossierMedical;

}
