package com.duongw.chatapp.validation;

import com.duongw.chatapp.model.dto.request.user.UserLoginRequest;
import com.duongw.chatapp.model.dto.request.user.UserRegisterRequest;

public class UserValidator {

    private final String USERNAME_PATTERN = "^[a-zA-Z0-9]{3,20}$";


    public void validateDuplicateInfo(String username, String email, Long excludeUserId){



    }

    public void validateRegisterUser(UserRegisterRequest userRegisterRequest){

    }


    public void validateLoginUser(UserLoginRequest userLoginRequest){

    }


    public void updateUserInformation(){

    }



}
