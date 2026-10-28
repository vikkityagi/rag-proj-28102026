package com.example.rag.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.rag.service.TestService;

@RestController 
public class Test {

    private final TestService testService;

    public Test(TestService testService) {
        this.testService = testService;
    }

    @PostMapping("/test")
    public String test(@RequestParam String message) {
        return testService.ask(message);
    }
    
}
