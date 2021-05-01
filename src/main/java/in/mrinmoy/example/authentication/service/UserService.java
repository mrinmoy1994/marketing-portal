package in.mrinmoy.example.authentication.service;

import in.mrinmoy.example.authentication.exception.CustomException;
import in.mrinmoy.example.authentication.model.Image;
import in.mrinmoy.example.authentication.model.JwtRequest;
import in.mrinmoy.example.authentication.model.KYCDetails;
import in.mrinmoy.example.authentication.model.LoggedInUser;
import in.mrinmoy.example.authentication.model.PasswordChangeRequest;
import in.mrinmoy.example.authentication.model.Token;
import in.mrinmoy.example.authentication.model.User;
import in.mrinmoy.example.authentication.repositories.ImageRepository;
import in.mrinmoy.example.authentication.repositories.KycMongoRepository;
import in.mrinmoy.example.authentication.repositories.TokenMongoRepository;
import in.mrinmoy.example.authentication.repositories.UserMongoRepository;
import in.mrinmoy.example.authentication.util.ExceptionUtil;
import in.mrinmoy.example.authentication.util.JwtTokenUtil;
import in.mrinmoy.example.authentication.util.PasswordUtill;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static in.mrinmoy.example.authentication.util.Constants.Authorization;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserMongoRepository userRepository;
    @Value("${jwt.secret}")
    private String secret;
    @Autowired
    private TokenMongoRepository tokenRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private KycMongoRepository kycRepository;

    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findByUsername(username).orElseThrow(() -> ExceptionUtil.getException("Invalid userId provided.", "Please provide valid user id and retry.", HttpStatus.BAD_REQUEST));
        user.setPassword(this.bCryptPasswordEncoder.encode(user.getPassword()));
        UserDetails userDetails = new LoggedInUser(user);
        return userDetails;
    }

    public User getCurrentUser(HttpServletRequest request) throws CustomException {
        final String requestTokenHeader = request.getHeader(Authorization);
        String username = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                throw ExceptionUtil.getException("Unable to get JWT Token", "Add JWT token as Bearer Token for authentication", HttpStatus.BAD_REQUEST);
            } catch (ExpiredJwtException e) {
                throw ExceptionUtil.getException("JWT Token has expired", "Re-login to get new login token", HttpStatus.UNAUTHORIZED);
            }
        } else {
            throw ExceptionUtil.getException("JWT Token does not begin with Bearer String", "Add JWT token as Bearer Token for authentication", HttpStatus.UNAUTHORIZED);
        }

        User currentUser = userRepository.findByUsername(username).orElseThrow(() -> ExceptionUtil.getException("Invalid userId provided.", "Please provide valid user id and retry.", HttpStatus.BAD_REQUEST));
        if (Objects.nonNull(currentUser)) {
            return currentUser;
        }
        throw ExceptionUtil.getException("User not available", "Please re-login and try again.", HttpStatus.NOT_FOUND);
    }

    public String generateToken(JwtRequest user) throws CustomException {
        if (Objects.isNull(user) || Objects.isNull(user.getUsername()) || Objects.isNull(user.getPassword())) {
            throw ExceptionUtil.getException("\"username\" and \"password\" are required.", "Provide valid request body", HttpStatus.BAD_REQUEST);
        }
        User userDetails = userRepository.findByUsername(user.getUsername()).orElseThrow(() -> ExceptionUtil.getException("Invalid userId provided.", "Please provide valid user id and retry.", HttpStatus.BAD_REQUEST));
        if (Objects.isNull(userDetails)) {
            throw ExceptionUtil.getException("username is invalid.", "Provide valid username", HttpStatus.UNAUTHORIZED);
        }

        if (!Objects.equals(PasswordUtill.encrypt(user.getPassword(), secret), userDetails.getPassword())) {
            throw ExceptionUtil.getException("username and password combination is invalid.", "Provide valid credentials", HttpStatus.UNAUTHORIZED);
        }
        String token = jwtTokenUtil.generateToken(userDetails);
        tokenRepository.save(Token.builder().userId(userDetails.getId()).token(token).build());
        return token;
    }

    public User addUser(User user) throws CustomException {
        if (Objects.isNull(user) || StringUtils.isEmpty(user.getMailId()) || StringUtils.isEmpty(user.getUsername())
                || StringUtils.isEmpty(user.getPassword()) || StringUtils.isEmpty(user.getType())) {
            throw ExceptionUtil.getException("Invalid user details", "Provide valid request body. Username, mailId, password and type can not be null/empty", HttpStatus.BAD_REQUEST);
        }

        User temp = User.builder().mailId(user.getMailId()).build();
        Example<User> userExample = Example.of(temp);
        List<User> userList = new ArrayList<>(this.userRepository.findAll(userExample));

        temp = User.builder().username(user.getUsername()).build();
        userExample = Example.of(temp);
        userList.addAll(this.userRepository.findAll(userExample));

        if (!userList.isEmpty()) {
            throw ExceptionUtil.getException("Duplicate entry found", "Provide valid request body. Username, mailId have to be unique", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(PasswordUtill.encrypt(user.getPassword(), secret));
        //user.getName().setUser(user);
        user.setBalance(0D);
        return this.userRepository.save(user);
    }

    public void logout(HttpServletRequest request) throws CustomException {
        final String requestTokenHeader = request.getHeader(Authorization);
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            log.info("token expiration : {}", jwtTokenUtil.getExpirationDateFromToken(jwtToken));
            String userName = jwtTokenUtil.getUsernameFromToken(jwtToken);

            User userDetails = userRepository.findByUsername(userName).orElseThrow(() -> ExceptionUtil.getException("Invalid userId provided.", "Please provide valid user id and retry.", HttpStatus.BAD_REQUEST));
            if (Objects.isNull(userDetails)) {
                throw ExceptionUtil.getException("Invalid token provided.", "Provide valid token", HttpStatus.UNAUTHORIZED);
            }
            Token token = tokenRepository.findByToken(jwtToken).orElseThrow(() -> ExceptionUtil.getException("Invalid token provided.", "Please provide valid token and try again", HttpStatus.BAD_REQUEST));
            if (Objects.nonNull(token))
                tokenRepository.delete(token);
        } else {
            throw ExceptionUtil.getException("JWT Token does not begin with Bearer String", "Add JWT token as Bearer Token for authentication", HttpStatus.UNAUTHORIZED);
        }
    }

    public String refreshToken(HttpServletRequest request) throws CustomException {
        final String requestTokenHeader = request.getHeader(Authorization);
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            log.info("token expiration : {}", jwtTokenUtil.getExpirationDateFromToken(jwtToken));
            String userName = jwtTokenUtil.getUsernameFromToken(jwtToken);
            User userDetails = userRepository.findByUsername(userName).orElseThrow(() -> ExceptionUtil.getException("Invalid userId provided.", "Please provide valid user id and retry.", HttpStatus.BAD_REQUEST));
            if (Objects.isNull(userDetails)) {
                throw ExceptionUtil.getException("Invalid token provided.", "Provide valid token", HttpStatus.UNAUTHORIZED);
            }
            Token token = tokenRepository.findByToken(jwtToken).orElseThrow(() -> ExceptionUtil.getException("Invalid token provided.", "Please provide valid token and try again", HttpStatus.BAD_REQUEST));
            String newToken = jwtTokenUtil.generateToken(userDetails);
            if (Objects.nonNull(token))
                tokenRepository.delete(token);
            tokenRepository.save(Token.builder().userId(userDetails.getId()).token(newToken).build());
            return newToken;
        } else {
            throw ExceptionUtil.getException("JWT Token does not begin with Bearer String", "Add JWT token as Bearer Token for authentication", HttpStatus.UNAUTHORIZED);
        }
    }

    public void updatePassword(HttpServletRequest request, PasswordChangeRequest passwordChangeRequest) throws CustomException {
        User userDetails = getCurrentUser(request);
        if (Objects.isNull(userDetails)) {
            throw ExceptionUtil.getException("Invalid token", "Please re-login and retry the operation", HttpStatus.UNAUTHORIZED);
        }
        String encryptedPassword = PasswordUtill.encrypt(passwordChangeRequest.getOldPassword(), secret);
        if (!userDetails.getPassword().equals(encryptedPassword))
            throw ExceptionUtil.getException("Wrong password provided", "Please provide correct password", HttpStatus.UNAUTHORIZED);
        encryptedPassword = PasswordUtill.encrypt(passwordChangeRequest.getNewPassword(), secret);
        userDetails.setPassword(encryptedPassword);
        userRepository.save(userDetails);
    }

    public KYCDetails updateKyc(HttpServletRequest request, KYCDetails kycDetails, MultipartFile pan, MultipartFile aadhar, MultipartFile bank) throws CustomException {
        User userDetails = getCurrentUser(request);
        if (Objects.isNull(userDetails)) {
            throw ExceptionUtil.getException("User not available", "Please re-login and try again.", HttpStatus.NOT_FOUND);
        }

        if (Objects.isNull(kycDetails.getAddress()) || Objects.isNull(kycDetails.getBankDetails())
                || Objects.isNull(kycDetails.getPanNo())) {
            throw ExceptionUtil.getException("Required parameters are missing.", "Address, PAN no and Bank details are mandatory, please provide these details and try again", HttpStatus.BAD_REQUEST);
        }

        getKycDetails(request, String.valueOf(userDetails.getId()), false)
                .ifPresent(kyc -> {
                    kycRepository.delete(kyc);
                    imageRepository.findAllById(Arrays.asList(kyc.getPanImageId(), kyc.getAadharImageId(), kyc.getBankImageId()))
                            .forEach(image -> imageRepository.delete(image));
                });

        kycDetails.setAadharImageId(saveImage(aadhar, "AADHAR", userDetails.getId()));
        kycDetails.setBankImageId(saveImage(bank, "BANK", userDetails.getId()));
        kycDetails.setPanImageId(saveImage(pan, "PAN", userDetails.getId()));
        kycDetails.setUserId(userDetails.getId());
        return kycRepository.save(kycDetails);
    }

    public String saveImage(MultipartFile imgFile, String type, String userId) throws CustomException {
        try {
            Image dbImage = new Image();
            dbImage.setName(imgFile.getOriginalFilename());
            dbImage.setImage(new Binary(BsonBinarySubType.BINARY, imgFile.getBytes()));
            dbImage.setExtension(imgFile.getContentType());
            dbImage.setType(type);
            dbImage.setUpdatedTime(Instant.now().getEpochSecond());
            dbImage.setUserId(userId);
            return imageRepository.save(dbImage).getId();

        } catch (Exception e) {
            log.error("Failed to save image : {}", imgFile.getName(), e);
            throw ExceptionUtil.getException(e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public byte[] retrieveImage(HttpServletRequest request, String imageId) throws CustomException {
        Image image = imageRepository.findById(imageId).orElseThrow(() -> ExceptionUtil.getException("Wrong image id provided", "Please provide a valid image id and try again.", HttpStatus.NOT_FOUND));
        User currentUser = getCurrentUser(request);
        if (!"ADMIN".equalsIgnoreCase(currentUser.getType()) && !currentUser.getId().equals(image.getUserId())) {
            throw ExceptionUtil.getException("User is no authorised for this operation", null, HttpStatus.UNAUTHORIZED);
        }
        return image.getImage().getData();
    }

    public Optional<KYCDetails> getKycDetails(HttpServletRequest request, String userId, boolean checkRequired) throws CustomException {
        if (checkRequired) {
            User currentUser = getCurrentUser(request);

            if (!"ADMIN".equalsIgnoreCase(currentUser.getType())) {
                log.error("required type : ADMIN, provided : {}", currentUser.getType());
                throw ExceptionUtil.getException("Unauthorized access", "User does not have permission to perform this operation.", HttpStatus.UNAUTHORIZED);
            }
        }
        userRepository.findById(userId).orElseThrow(() -> ExceptionUtil.getException("Invalid userId provided.", "Please provide valid user id and retry.", HttpStatus.BAD_REQUEST));
        return kycRepository.findByUserId(userId);
    }
}
