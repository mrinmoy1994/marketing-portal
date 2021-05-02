package in.mrinmoy.example.authentication.util;

import static in.mrinmoy.example.authentication.util.Constants.Authorization;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import in.mrinmoy.example.authentication.exception.CustomException;
import in.mrinmoy.example.authentication.model.User;
import in.mrinmoy.example.authentication.repositories.UserMongoRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenUtil implements Serializable {
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;
    private static final long serialVersionUID = -2550185165626007488L;
    @Autowired
    private UserMongoRepository userRepository;
    @Value("${jwt.secret}")
    private String secret;

    // retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public User getUserDetailsFromToken(String token) throws CustomException {
        String userName = getClaimFromToken(token, Claims::getSubject);
        return userRepository.findByUsername(userName).orElseThrow(
            () -> ExceptionUtil.getException("Invalid userId provided.", "Please provide valid user id and retry.", HttpStatus.BAD_REQUEST));
    }

    // retrieve expiration date from jwt token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // for retrieveing any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    // check if the token has expired
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // generate token for user
    public String generateToken(User userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", userDetails.getType());
        return doGenerateToken(claims, userDetails.getUsername());
    }

    // while creating the token -
    // 1. Define claims of the token, like Issuer, Expiration, Subject, and the ID
    // 2. Sign the JWT using the HS512 algorithm and secret key.
    // 3. According to JWS Compact
    // Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    // compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
            .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    // validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public void cancelToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        claims.setExpiration(new Date());
        Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().setExpiration(new Date(System.currentTimeMillis()));
    }

    public User getCurrentUser(HttpServletRequest request) throws CustomException {
        final String requestTokenHeader = request.getHeader(Authorization);
        String username = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            try {
                username = getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                throw ExceptionUtil.getException("Unable to get JWT Token", "Add JWT token as Bearer Token for authentication",
                    HttpStatus.BAD_REQUEST);
            } catch (ExpiredJwtException e) {
                throw ExceptionUtil.getException("JWT Token has expired", "Re-login to get new login token", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw ExceptionUtil.getException("JWT Token does not begin with Bearer String", "Add JWT token as Bearer Token for authentication",
                HttpStatus.UNAUTHORIZED);
        }

        User currentUser = userRepository.findByUsername(username).orElseThrow(
            () -> ExceptionUtil.getException("Invalid userId provided.", "Please provide valid user id and retry.", HttpStatus.BAD_REQUEST));
        if (Objects.nonNull(currentUser)) {
            return currentUser;
        }
        throw ExceptionUtil.getException("User not available", "Please re-login and try again.", HttpStatus.NOT_FOUND);
    }
}
