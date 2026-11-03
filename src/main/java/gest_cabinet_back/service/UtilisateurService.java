package gest_cabinet_back.service;

import gest_cabinet_back.dto.UtilisateurRequestDTO;
import gest_cabinet_back.dto.UtilisateurResponseDTO;


import java.util.List;

public interface UtilisateurService {

    List<UtilisateurResponseDTO> listerTous();

    UtilisateurResponseDTO consulterParId(Long id);

    UtilisateurResponseDTO creer(UtilisateurRequestDTO dto);

    UtilisateurResponseDTO modifier(Long id, UtilisateurRequestDTO dto);

    void desactiver(Long id);

}
