package gest_cabinet_back.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DossierMedical {
    private Long id;
    private String numeroDossier;
 
    private LocalDateTime dateCreation;
 
    private String groupeSanguin;
 
    private String antecedents;
 
    private String allergies;
 
    private String traitementsEnCours;

    public DossierMedical(Long id, String numeroDossier, LocalDateTime dateCreation, String groupeSanguin, String antecedents, String allergies, String traitementsEnCours) {
        this.id = id;
        this.numeroDossier = numeroDossier;
        this.dateCreation = dateCreation;
        this.groupeSanguin = groupeSanguin;
        this.antecedents = antecedents;
        this.allergies = allergies;
        this.traitementsEnCours = traitementsEnCours;
    }
}
