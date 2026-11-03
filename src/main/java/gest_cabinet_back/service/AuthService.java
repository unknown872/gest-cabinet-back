package gest_cabinet_back.service;

import gest_cabinet_back.dto.LoginRequestDTO;
import gest_cabinet_back.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO dto);
}
