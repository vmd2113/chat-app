package com.duongw.chatapp.model.dto.request.user;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;


    @NotBlank(message = "Password is required")
    @Size(min = 8)
    private String password;

    @NotBlank(message = "Confirm password is required")
    @Size(min = 8)
    private String confirmPassword;


    @NotBlank(message = "Full name is required")
    private String fullName;


}
