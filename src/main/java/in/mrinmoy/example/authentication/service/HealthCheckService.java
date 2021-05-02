package in.mrinmoy.example.authentication.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import in.mrinmoy.example.authentication.model.Token;
import in.mrinmoy.example.authentication.repositories.TokenMongoRepository;
import in.mrinmoy.example.authentication.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;

@Component
public class HealthCheckService {

    @Autowired
    TokenMongoRepository tokenRepository;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Scheduled(cron = "* 0 0 * * SUN-SAT")
    public void tokenCleanup() {
        List<Token> expiredTokens = tokenRepository.findAll().stream().filter(token -> {
            try {
                return jwtTokenUtil.isTokenExpired(token.getToken());
            } catch (ExpiredJwtException ex) {
                return true;
            }
        }).collect(Collectors.toList());
        expiredTokens.forEach(token -> tokenRepository.delete(token));
    }
}
