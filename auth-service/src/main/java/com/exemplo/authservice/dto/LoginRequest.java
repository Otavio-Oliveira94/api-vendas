package com.exemplo.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@AllArgsConstructor 
@NoArgsConstructor 
public class LoginRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String senha;

}
