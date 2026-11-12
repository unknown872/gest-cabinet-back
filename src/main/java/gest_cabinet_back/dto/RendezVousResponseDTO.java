
package gest_cabinet_back.dto;

import gest_cabinet_back.model.StatutRdv;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RendezVousResponseDTO {

    private Long id;

    private LocalDateTime dateHeure;

    private String motif;

    private String notes;

    private StatutRdv statut;

    private String nomMedecin;

    private String prenomMedecin;
}
