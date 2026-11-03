package gest_cabinet_back.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Table(name = "medecin")
@Getter 
@Setter 
@NoArgsConstructor
public class Medecin extends Utilisateur {

    @Column(unique = true)
    private String codeMedecin;

    @Column(name = "signature_numerique")
    private String signatureNumerique;

    @ManyToMany
    @JoinTable(name = "medecin_specialite",
               joinColumns = @JoinColumn(name = "medecin_id"),
               inverseJoinColumns = @JoinColumn(name = "specialite_id"))
    private List<Specialite> specialites = new ArrayList<>();

    @OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL)
    private List<DisponibiliteMedecin> disponibilites = new ArrayList<>();

}
