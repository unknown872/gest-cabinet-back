package gest_cabinet_back.controller;

import gest_cabinet_back.dto.LoginRequestDTO;
import gest_cabinet_back.dto.UtilisateurRequestDTO;
import gest_cabinet_back.dto.UtilisateurResponseDTO;
import gest_cabinet_back.model.Role;
import gest_cabinet_back.service.AuthService;
import gest_cabinet_back.service.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UtilisateurService utilisateurService;

    public AuthController(AuthService authService, UtilisateurService utilisateurService){
        this.authService = authService;
        this.utilisateurService = utilisateurService;
    }

    //POST /api/auth/login
    @PostMapping("/register")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public UtilisateurResponseDTO register(@Valid @RequestBody UtilisateurRequestDTO dto) {
        dto.setRole(Role.PATIENT);
        return utilisateurService.creer(dto);
    }

    //POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<gest_cabinet_back.dto.LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }




}
