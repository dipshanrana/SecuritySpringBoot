package com.example.security.controller;

import com.example.security.dto.DoctorResponseDto;
import com.example.security.dto.OnboardDoctorRequestDto;
import com.example.security.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DoctorService doctorService;

    @GetMapping("/adminname")
    public String adminName(){
        return "Dipshan Ranabhat";
    }


@PostMapping("/onoBoardNewDoctor")
public ResponseEntity<DoctorResponseDto> onBoardNewDoctor(@RequestBody OnboardDoctorRequestDto onboardDoctorRequestDto){
    return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.onBoardNewDoctor(onboardDoctorRequestDto));
}

    }