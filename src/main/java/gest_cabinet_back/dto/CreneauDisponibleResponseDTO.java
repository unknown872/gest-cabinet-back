
package gest_cabinet_back.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreneauDisponibleResponseDTO {

    private LocalDateTime heureDebut;

    private LocalDateTime heureFin;

    private boolean disponible;
}
