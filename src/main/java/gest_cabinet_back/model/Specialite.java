package gest_cabinet_back.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "specialite")
@Getter
@Setter
@NoArgsConstructor
public class Specialite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    @Column(unique = true)
    private String libelle;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "specialites")
    private List<Medecin> medecins = new ArrayList<>();

}
