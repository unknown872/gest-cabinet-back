package gest_cabinet_back.service;

import gest_cabinet_back.dto.LoginRequestDTO;
import gest_cabinet_back.dto.LoginResponseDTO;
import gest_cabinet_back.model.Utilisateur;
import gest_cabinet_back.security.CustomUserDetails;
import gest_cabinet_back.security.JwtUtil;
import gest_cabinet_back.service.AuthService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getMotDePasse())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Email ou mot de passe incorrect.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Utilisateur utilisateur = userDetails.getUtilisateur();

        String token = jwtUtil.generateToken(userDetails);

        return new LoginResponseDTO(token, utilisateur.getEmail(), utilisateur.getRole());
    }

}