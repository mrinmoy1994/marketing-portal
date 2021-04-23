package in.mrinmoy.example.authentication.controller;

import in.mrinmoy.example.authentication.exception.CustomException;
import in.mrinmoy.example.authentication.model.JwtRequest;
import in.mrinmoy.example.authentication.model.JwtResponse;
import in.mrinmoy.example.authentication.model.KYCDetails;
import in.mrinmoy.example.authentication.model.PasswordChangeRequest;
import in.mrinmoy.example.authentication.model.StatusResponse;
import in.mrinmoy.example.authentication.model.User;
import in.mrinmoy.example.authentication.model.UserResponse;
import in.mrinmoy.example.authentication.repositories.ImageRepository;
import in.mrinmoy.example.authentication.repositories.UserMongoRepository;
import in.mrinmoy.example.authentication.service.FileService;
import in.mrinmoy.example.authentication.service.UserService;
import in.mrinmoy.example.authentication.util.ExceptionUtil;
import in.mrinmoy.example.authentication.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.DELETE, RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.PATCH, RequestMethod.POST, RequestMethod.PUT})
public class UserController {
    @Autowired
    private UserMongoRepository userRepository;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private FileService fileService;

    @Value("${jwt.secret}")
    private String secret;

    @PostMapping(value = "/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) {
        try {
            String token = userService.generateToken(authenticationRequest);
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (CustomException e) {
            log.error("Exception occur while logging in: ", e);
            return ExceptionUtil.getExceptionResponse(e);
        }
    }

    @PostMapping(value = "/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            return ResponseEntity.ok(new UserResponse(userService.addUser(user)));
        } catch (CustomException e) {
            log.error("Exception occur while registering user: ", e);
            return ExceptionUtil.getExceptionResponse(e);
        }
    }

    @GetMapping(value = "/getUsers")
    public ResponseEntity<?> getUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(UserResponse::new).collect(Collectors.toList()));
    }

    @GetMapping(value = "/getCurrentUser")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            User currentUser = userService.getCurrentUser(request);
            if (Objects.nonNull(currentUser)) {
                return ResponseEntity.ok(new UserResponse(currentUser));
            }
            throw ExceptionUtil.getException("User not available", "Please re-login and try again.", HttpStatus.NOT_FOUND);
        } catch (CustomException e) {
            log.error("Exception occur while fetching user data: ", e);
            return ExceptionUtil.getExceptionResponse(e);
        }
    }

    @GetMapping(value = "/log-out")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            userService.logout(request);
            return ResponseEntity.ok(StatusResponse.builder().status("LOGGED OUT").build());
        } catch (CustomException e) {
            log.error("Exception occur while logout : ", e);
            return ExceptionUtil.getExceptionResponse(e);
        }
    }

    @GetMapping(value = "/refreshToken")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String token = userService.refreshToken(request);
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (CustomException e) {
            log.error("Exception occur while refreshing token : ", e);
            return ExceptionUtil.getExceptionResponse(e);
        }
    }

    @PostMapping(value = "/updatePassword")
    public ResponseEntity<?> updatePassword(HttpServletRequest request, @RequestBody PasswordChangeRequest passwordChangeRequest) {
        try {
            userService.updatePassword(request, passwordChangeRequest);
            return ResponseEntity.ok(StatusResponse.builder().status("PASSWORD CHANGED").build());
        } catch (CustomException e) {
            log.error("Exception occur while registering user: ", e);
            return ExceptionUtil.getExceptionResponse(e);
        }
    }

    @PostMapping(value = "/updateKYCDetails", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE})
    public ResponseEntity<?> updateKYCDetails(HttpServletRequest request, @RequestPart("kycDetails") KYCDetails kycDetails, @RequestPart("pan") MultipartFile pan, @RequestPart("aadhar") MultipartFile aadhar, @RequestPart("bank") MultipartFile bank) {
        try {
            return ResponseEntity.ok(userService.updateKyc(request, kycDetails, pan, aadhar, bank));
        } catch (CustomException e) {
            log.error("Exception occur while uploading KYC details: ", e);
            return ExceptionUtil.getExceptionResponse(e);
        }
    }

    @GetMapping(value = "/getKYCDetails")
    public ResponseEntity<?> getKYCDetails(HttpServletRequest request, @RequestParam("userId") String userId) {
        try {
            return ResponseEntity.ok(userService.getKycDetails(request, userId, true).orElseThrow(
                    () -> ExceptionUtil.getException("KYC is not updated for the given user.", "Please upload KYC details for the given user first and try again", HttpStatus.BAD_REQUEST)
            ));
        } catch (CustomException e) {
            log.error("Exception occur while fetching KYC details: ", e);
            return ExceptionUtil.getExceptionResponse(e);
        }
    }

    @GetMapping(value = "/image/{imageId}")
    public ResponseEntity<?>  downloadImage(HttpServletRequest request, @PathVariable String imageId, Model model) {
        try {
            return ResponseEntity.ok(userService.retrieveImage(request, imageId, model));
        } catch (CustomException e) {
            log.error("Exception occur while fetching KYC details: ", e);
            return ExceptionUtil.getExceptionResponse(e);
        }
    }
}
