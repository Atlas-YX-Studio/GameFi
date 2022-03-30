package com.bixin.gameFi.common.controller;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

@ControllerAdvice
@Order(10000)
public class GlobalControllerAdvice {

    @InitBinder
    public void setDisallowedFields(WebDataBinder webDataBinder) {
        String[] disallowedFields = {"class.*", "Class.*", "*.class.*", "*.Class.*"};
        webDataBinder.setDisallowedFields(disallowedFields);
    }
}