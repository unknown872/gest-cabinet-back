package gest_cabinet_back.controller;

import gest_cabinet_back.dto.ProfileResponseDTO;
import gest_cabinet_back.service.ProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileResponseDTO getProfile(
            Authentication authentication) {

        String email = authentication.getName();

        return profileService.getProfile(email);
    }
}
