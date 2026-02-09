package com.example.security.service;

import com.example.security.dto.DoctorResponseDto;
import com.example.security.dto.OnboardDoctorRequestDto;
import com.example.security.entity.Doctor;
import com.example.security.entity.User;
import com.example.security.entity.type.RoleType;
import com.example.security.repository.DoctorRepository;
import com.example.security.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final ModelMapper modelMapper;


    @Transactional
    public DoctorResponseDto onBoardNewDoctor(OnboardDoctorRequestDto onboardDoctorRequestDto) {
      User user = userRepository.findById(onboardDoctorRequestDto.getUserId()).orElseThrow();

      if (doctorRepository.existsById(onboardDoctorRequestDto.getUserId())){
          throw new IllegalArgumentException("Already a doctor");
      }

        Doctor doctor  = Doctor.builder().name(onboardDoctorRequestDto.getName())
                .specialization(onboardDoctorRequestDto.getSpecialization())
                .user(user)
                .build();

      user.getRoles().add(RoleType.DOCTOR);
      return modelMapper.map(doctorRepository.save(doctor),DoctorResponseDto.class);
    }
}
