package com.organica.controllers;

import com.organica.services.SunskyApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("/sunsky")
public class SunskyApiController {
    @Autowired
    SunskyApiService sunskyApiService;

    @GetMapping
    public String runMyMethod() throws Exception{
        sunskyApiService.exampleFetch();
        return "Metoda byla spuštěna.";
    }
}
