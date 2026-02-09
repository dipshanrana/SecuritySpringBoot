package com.example.security.security;

import com.example.security.entity.User;
import com.example.security.entity.type.AuthProviderType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class AuthUtil {
    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }
    public String generateAccessToken(User user) {
        return Jwts.builder().setSubject(user.getUsername()).claim("UserId",user.getId().toString()).setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis()+1000*60*10)).signWith(getSecretKey()).compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSecretKey()).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public AuthProviderType getProviderTypeFromRegistrationId(String registratinoId){
        return switch(registratinoId.toLowerCase()){
            case "google" -> AuthProviderType.GOOGLE;
            case "github" -> AuthProviderType.GITHUB;
            default -> throw  new IllegalArgumentException("Unsupported OAuth2 provider " + registratinoId);
            };
        }

        public String determineProviderIdFromOAuth2user(OAuth2User oAuth2User, String registrationId){
        String providerId = switch (registrationId.toLowerCase()){
            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> oAuth2User.getAttribute("id").toString();
            default -> {
                throw   new IllegalArgumentException("Unsupported OAuth2 provider");
            }
        };
        if(providerId == null || providerId.isBlank()){
            throw new IllegalArgumentException("Unable to determine providerId for OAuth2 login");
        }
        return providerId;
    }

    public  String determineUsernameFromOAuth2User(OAuth2User oAuth2User,String registrationId,String providerId){
        String email = oAuth2User.getAttribute("email");
        if(email !=null && !email.isBlank()){
            return email;
        }
        return switch (registrationId.toLowerCase()){
            case "google" -> oAuth2User.getAttribute("sub");
            case "github"  -> oAuth2User.getAttribute("login");
            default -> providerId;
        };
    }
}










































