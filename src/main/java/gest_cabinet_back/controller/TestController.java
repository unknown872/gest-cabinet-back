package gest_cabinet_back.controller;

import gest_cabinet_back.dto.UtilisateurResponseDTO;
import gest_cabinet_back.service.UtilisateurService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final UtilisateurService utilisateurService;

    public TestController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping("/test1")
    public List<UtilisateurResponseDTO> getAllUsers() {
        return utilisateurService.listerTous();
    }
}
