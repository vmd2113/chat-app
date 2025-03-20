package com.duongw.chatapp.controller;


import com.duongw.chatapp.config.AppConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(AppConstant.API_BASE_PATH + "/users")
public class UserController {
}
