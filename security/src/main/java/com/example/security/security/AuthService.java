package com.example.security.security;

import com.example.security.dto.LoginRequestDto;
import com.example.security.dto.LoginResponseDto;
import com.example.security.dto.SignUpRequestDto;
import com.example.security.dto.SignupResponseDto;
import com.example.security.entity.Patient;
import com.example.security.entity.User;
import com.example.security.entity.type.AuthProviderType;
import com.example.security.entity.type.RoleType;
import com.example.security.repository.PatientRepository;
import com.example.security.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUtil authUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PatientRepository patientRepository;

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(),loginRequestDto.getPassword()));
        User user = (User) authentication.getPrincipal();
        String token = authUtil.generateAccessToken(user);
        return new LoginResponseDto(user.getId(),token);
    }

    public User signUpInternal(SignUpRequestDto signupRequestDto, AuthProviderType authProviderType,String providerId){
        User user = userRepository.findByUsername(signupRequestDto.getUsername()).orElse(null);
        if(user !=null )
            throw  new IllegalArgumentException("Username already exist");
         user = User.builder().providerType(authProviderType).providerId(providerId).username(signupRequestDto.getUsername()).roles(signupRequestDto.getRoles()).build();

         if(authProviderType == AuthProviderType.EMAIL)
         {
             user.setPassword(passwordEncoder.encode(signupRequestDto.getPassword()));
         }

         user =  userRepository.save(user);

        Patient patient = Patient.builder().name(signupRequestDto.getName()).email(signupRequestDto.getUsername()).user(user).build();
        patientRepository.save(patient);

         return user;
    }

    public SignupResponseDto signup(SignUpRequestDto signupRequestDto) {
        User user = signUpInternal(signupRequestDto,AuthProviderType.EMAIL,null);
        return new SignupResponseDto(user.getId(),user.getUsername());
    }

@Transactional
    public ResponseEntity<LoginResponseDto> handleOAuth2LoginRequest(OAuth2User oAuth2User, String registrationId) {
        AuthProviderType providerType = authUtil.getProviderTypeFromRegistrationId(registrationId);
        String providerId = authUtil.determineProviderIdFromOAuth2user(oAuth2User,registrationId);
        User user = userRepository.findByProviderIdAndProviderType(providerId, providerType).orElse(null);
        String email = oAuth2User.getAttribute("email");
        User emailUser = userRepository.findByUsername(email).orElse(null);
        String name = oAuth2User.getAttribute("name");
        if(user == null && emailUser == null){
            String username = authUtil.determineUsernameFromOAuth2User(oAuth2User,registrationId,providerId);
            user= signUpInternal(new SignUpRequestDto(username,null,name,Set.of(RoleType.PATIENT)),providerType,providerId);
    }
        else if(user !=null){
            if(email !=null && !email.isBlank() && !email.equals(user.getUsername())){
               user.setUsername(email);
               userRepository.save(user);
            }
        }
        else {
            throw new BadCredentialsException("This email is already registered with provider"+email);
        }
        LoginResponseDto loginResponseDto = new LoginResponseDto(user.getId(),authUtil.generateAccessToken(user));
        return ResponseEntity.ok(loginResponseDto);
}
}
