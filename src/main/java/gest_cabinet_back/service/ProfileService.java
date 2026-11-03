package gest_cabinet_back.service;

import gest_cabinet_back.dto.ProfileResponseDTO;

public interface ProfileService {
    ProfileResponseDTO getProfile(String email);
}
