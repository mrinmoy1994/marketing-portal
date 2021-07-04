package in.mrinmoy.example.authentication.service;

import static in.mrinmoy.example.authentication.util.Constants.Authorization;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import in.mrinmoy.example.authentication.exception.CustomException;
import in.mrinmoy.example.authentication.model.ContentType;
import in.mrinmoy.example.authentication.model.Image;
import in.mrinmoy.example.authentication.model.JwtRequest;
import in.mrinmoy.example.authentication.model.KYCApprovalRequest;
import in.mrinmoy.example.authentication.model.KYCDetails;
import in.mrinmoy.example.authentication.model.LoggedInUser;
import in.mrinmoy.example.authentication.model.Notification;
import in.mrinmoy.example.authentication.model.NotificationType;
import in.mrinmoy.example.authentication.model.PageDetails;
import in.mrinmoy.example.authentication.model.PasswordChangeRequest;
import in.mrinmoy.example.authentication.model.StatusResponse;
import in.mrinmoy.example.authentication.model.Token;
import in.mrinmoy.example.authentication.model.User;
import in.mrinmoy.example.authentication.model.UserResponse;
import in.mrinmoy.example.authentication.model.UserType;
import in.mrinmoy.example.authentication.repositories.ImageRepository;
import in.mrinmoy.example.authentication.repositories.KycRepository;
import in.mrinmoy.example.authentication.repositories.TokenRepository;
import in.mrinmoy.example.authentication.repositories.UserRepository;
import in.mrinmoy.example.authentication.util.Constants;
import in.mrinmoy.example.authentication.util.ExceptionUtil;
import in.mrinmoy.example.authentication.util.JwtTokenUtil;
import in.mrinmoy.example.authentication.util.PasswordUtill;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserRepository userRepository;
    @Value("${jwt.secret}")
    private String secret;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private KycRepository kycRepository;
    @Autowired
    private NotificationService notificationService;

    @SneakyThrows
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findByUsername(username).orElseThrow(
            () -> ExceptionUtil.getException(Constants.INVALID_USER_ID, Constants.INVALID_USER_ID_REMEDIATION, HttpStatus.BAD_REQUEST));
        user.setPassword(this.bCryptPasswordEncoder.encode(user.getPassword()));
        return new LoggedInUser(user);
    }

    public String generateReferralCode(JwtRequest user) throws CustomException {
        if (Objects.isNull(user) || Objects.isNull(user.getUsername()) || Objects.isNull(user.getPassword())) {
            throw ExceptionUtil.getException("\"username\" and \"password\" are required.", "Provide valid request body", HttpStatus.BAD_REQUEST);
        }
        User userDetails = userRepository.findByUsername(user.getUsername()).orElseThrow(
            () -> ExceptionUtil.getException(Constants.INVALID_USER_ID, Constants.INVALID_USER_ID_REMEDIATION, HttpStatus.BAD_REQUEST));
        if (Objects.isNull(userDetails)) {
            throw ExceptionUtil.getException("username is invalid.", "Provide valid username", HttpStatus.UNAUTHORIZED);
        }

        if (!Objects.equals(PasswordUtill.encrypt(user.getPassword(), secret), userDetails.getPassword())) {
            throw ExceptionUtil.getException("username and password combination is invalid.", "Provide valid credentials", HttpStatus.UNAUTHORIZED);
        }
        String token = jwtTokenUtil.generateToken(userDetails);
        tokenRepository.insert(Token.builder().userId(userDetails.getId()).token(token).build());
        return token;
    }

    public User addUser(User user) throws CustomException {
        if (Objects.isNull(user) || StringUtils.isEmpty(user.getMailId()) || StringUtils.isEmpty(user.getUsername())
            || StringUtils.isEmpty(user.getPassword()) || StringUtils.isEmpty(user.getType())) {
            throw ExceptionUtil.getException("Invalid user details",
                "Provide valid request body. Username, mailId, password and type can not be null/empty", HttpStatus.BAD_REQUEST);
        }

        User temp = User.builder().mailId(user.getMailId()).build();
        Example<User> userExample = Example.of(temp);
        List<User> userList = new ArrayList<>(this.userRepository.findAll(userExample));

        temp = User.builder().username(user.getUsername()).build();
        userExample = Example.of(temp);
        userList.addAll(this.userRepository.findAll(userExample));

        if (!userList.isEmpty()) {
            throw ExceptionUtil.getException("Duplicate entry found", "Provide valid request body. Username, mailId have to be unique",
                HttpStatus.BAD_REQUEST);
        }

        user.setPassword(PasswordUtill.encrypt(user.getPassword(), secret));
        if (UserType.ADMIN.name().equalsIgnoreCase(user.getType()))
            user.setType(UserType.ADMIN.name());
        else {
            user.setType(UserType.CUSTOMER.name());
            user.setReferralCode(generateReferralCode());
        }
        user.setBalance(0D);
        user = this.userRepository.insert(user);
        notificationService.add(Notification.builder()
                .id(UUID.randomUUID().toString())
            .userId(user.getId())
            .acknowledged(false)
            .type(NotificationType.NEW_USER.name())
            .alertTitle(Constants.NEW_USER_MESSAGE)
            .ownerType(UserType.ADMIN.name()).build(), false);
        return user;
    }

    public void logout(HttpServletRequest request) throws CustomException {
        final String requestTokenHeader = request.getHeader(Authorization);
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            log.info("token expiration : {}", jwtTokenUtil.getExpirationDateFromToken(jwtToken));
            String userName = jwtTokenUtil.getUsernameFromToken(jwtToken);

            User userDetails = userRepository.findByUsername(userName).orElseThrow(
                () -> ExceptionUtil.getException(Constants.INVALID_USER_ID, Constants.INVALID_USER_ID_REMEDIATION, HttpStatus.BAD_REQUEST));
            if (Objects.isNull(userDetails)) {
                throw ExceptionUtil.getException(Constants.INVALID_TOKEN, Constants.INVALID_TOKEN_REMEDIATION, HttpStatus.UNAUTHORIZED);
            }
            Token token = tokenRepository.findByToken(jwtToken).orElseThrow(
                () -> ExceptionUtil.getException(Constants.INVALID_TOKEN, Constants.INVALID_TOKEN_REMEDIATION, HttpStatus.BAD_REQUEST));
            if (Objects.nonNull(token))
                tokenRepository.delete(token);
        } else {
            throw ExceptionUtil.getException("JWT Token does not begin with Bearer String", "Add JWT token as Bearer Token for authentication",
                HttpStatus.UNAUTHORIZED);
        }
    }

    public String refreshToken(HttpServletRequest request) throws CustomException {
        final String requestTokenHeader = request.getHeader(Authorization);
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);
            log.info("token expiration : {}", jwtTokenUtil.getExpirationDateFromToken(jwtToken));
            String userName = jwtTokenUtil.getUsernameFromToken(jwtToken);
            User userDetails = userRepository.findByUsername(userName).orElseThrow(
                () -> ExceptionUtil.getException(Constants.INVALID_USER_ID, Constants.INVALID_USER_ID_REMEDIATION, HttpStatus.BAD_REQUEST));
            if (Objects.isNull(userDetails)) {
                throw ExceptionUtil.getException(Constants.INVALID_TOKEN, Constants.INVALID_TOKEN_REMEDIATION, HttpStatus.UNAUTHORIZED);
            }
            Token token = tokenRepository.findByToken(jwtToken).orElseThrow(
                () -> ExceptionUtil.getException(Constants.INVALID_TOKEN, Constants.INVALID_TOKEN_REMEDIATION, HttpStatus.BAD_REQUEST));
            String newToken = jwtTokenUtil.generateToken(userDetails);
//            MongoClient mongoClient = null;
//            mongoClient.getDatabase("").getCollection("").find(BsonDocument::new);
            if (Objects.nonNull(token))
                tokenRepository.delete(token);
            tokenRepository.insert(Token.builder().userId(userDetails.getId()).token(newToken).build());
            return newToken;
        } else {
            throw ExceptionUtil.getException("JWT Token does not begin with Bearer String", "Add JWT token as Bearer Token for authentication",
                HttpStatus.UNAUTHORIZED);
        }
    }

    public void updatePassword(HttpServletRequest request, PasswordChangeRequest passwordChangeRequest) throws CustomException {
        User userDetails = jwtTokenUtil.getCurrentUser(request);
        if (Objects.isNull(userDetails)) {
            throw ExceptionUtil.getException("Invalid token", "Please re-login and retry the operation", HttpStatus.UNAUTHORIZED);
        }
        String encryptedPassword = PasswordUtill.encrypt(passwordChangeRequest.getOldPassword(), secret);
        if (!userDetails.getPassword().equals(encryptedPassword))
            throw ExceptionUtil.getException("Wrong password provided", "Please provide correct password", HttpStatus.UNAUTHORIZED);
        encryptedPassword = PasswordUtill.encrypt(passwordChangeRequest.getNewPassword(), secret);
        userDetails.setPassword(encryptedPassword);
        userRepository.insert(userDetails);
    }

    public KYCDetails addKyc(HttpServletRequest request, KYCDetails kycDetails, MultipartFile pan, MultipartFile aadhar, MultipartFile bank)
        throws CustomException {
        User userDetails = jwtTokenUtil.getCurrentUser(request);
        if (Objects.isNull(userDetails)) {
            throw ExceptionUtil.getException("User not available", "Please re-login and try again.", HttpStatus.NOT_FOUND);
        }

        if (Objects.isNull(kycDetails.getAddress()) && Objects.isNull(kycDetails.getBankDetails())
            && Objects.isNull(kycDetails.getPanNo())) {
            throw ExceptionUtil.getException("Required parameters are missing.",
                "Address, PAN no and Bank details are mandatory, please provide these details and try again", HttpStatus.BAD_REQUEST);
        }

        KYCDetails currentKYCDetails = getKycDetails(String.valueOf(userDetails.getId())).orElse(null);
        Notification notification = Notification.builder()
                .id(UUID.randomUUID().toString())
            .alertTitle(Constants.KYC_REQUEST_MESSAGE)
            .type(NotificationType.KYC_REQUEST.name())
            .userId(userDetails.getId())
            .acknowledged(false)
            .updateTime(Instant.now().toString())
            .ownerType(UserType.ADMIN.name()).build();
        if (Objects.isNull(currentKYCDetails)) {
            kycDetails.setAadharImageId(saveImage(aadhar, "AADHAR", userDetails.getId()));
            kycDetails.setBankImageId(saveImage(bank, "BANK", userDetails.getId()));
            kycDetails.setPanImageId(saveImage(pan, "PAN", userDetails.getId()));
            kycDetails.setUserId(userDetails.getId());
            currentKYCDetails = kycRepository.save(kycDetails);
            notificationService.add(notification, false);
        } else {
            if (Objects.nonNull(aadhar)) {
                currentKYCDetails.setAadharImageId(saveImage(aadhar, ContentType.AADHAR.name(), userDetails.getId()));
                currentKYCDetails.setAadharApproved(false);
                currentKYCDetails.setAadharNo(kycDetails.getAadharNo());
                currentKYCDetails.setAddress(kycDetails.getAddress());
            }
            if (Objects.nonNull(bank)) {
                currentKYCDetails.setBankImageId(saveImage(bank, ContentType.BANK.name(), userDetails.getId()));
                currentKYCDetails.setBankDetailsApproved(false);
                currentKYCDetails.setBankDetails(kycDetails.getBankDetails());
            }
            if (Objects.nonNull(pan)) {
                currentKYCDetails.setPanImageId(saveImage(pan, ContentType.PAN.name(), userDetails.getId()));
                currentKYCDetails.setPanApproved(false);
                currentKYCDetails.setBankDetails(kycDetails.getBankDetails());
            }
            currentKYCDetails = kycRepository.save(currentKYCDetails);
            notificationService.add(notification, true);
        }
        return currentKYCDetails;
    }

    public String saveImage(MultipartFile imgFile, String type, String userId) throws CustomException {
        try {
            Image dbImage = new Image();
            dbImage.setName(imgFile.getOriginalFilename());
            dbImage.setContent(new Binary(BsonBinarySubType.BINARY, imgFile.getBytes()));
            dbImage.setExtension(imgFile.getContentType());
            dbImage.setType(type);
            dbImage.setUpdatedTime(Instant.now().toString());
            dbImage.setUserId(userId);
            return imageRepository.insert(dbImage).getId();

        } catch (Exception e) {
            log.error("Failed to save image : {}", imgFile.getName(), e);
            throw ExceptionUtil.getException(e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public byte[] retrieveImage(HttpServletRequest request, String imageId) throws CustomException {
        Image image = imageRepository.findById(imageId).orElseThrow(
            () -> ExceptionUtil.getException("Wrong image id provided", "Please provide a valid image id and try again.", HttpStatus.NOT_FOUND));
        User currentUser = jwtTokenUtil.getCurrentUser(request);
        if (!"ADMIN".equalsIgnoreCase(currentUser.getType()) && !currentUser.getId().equals(image.getUserId())) {
            throw ExceptionUtil.getException("User is no authorised for this operation", null, HttpStatus.UNAUTHORIZED);
        }
        return image.getContent().getData();
    }

    public Optional<KYCDetails> getKycDetails(String userId) throws CustomException {
//        if (checkRequired) {
//            User currentUser = jwtTokenUtil.getCurrentUser(request);
//
//            if (UserType.CUSTOMER.name().equalsIgnoreCase(currentUser.getType())) {
//                log.error("required type : ADMIN, provided : {}", currentUser.getType());
//                throw ExceptionUtil.getException(Constants.UNAUTHORISED_ERROR, Constants.UNAUTHORISED_REMEDIATION,
//                    HttpStatus.UNAUTHORIZED);
//            }
//        }
        userRepository.findById(userId).orElseThrow(
            () -> ExceptionUtil.getException(Constants.INVALID_USER_ID, Constants.INVALID_USER_ID_REMEDIATION, HttpStatus.BAD_REQUEST));
        return kycRepository.findByUserId(userId);
    }

    public PageDetails getAllUser(int page, int size, String sortBy, String order) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (Sort.Direction.DESC.name().equalsIgnoreCase(order))
            direction = Sort.Direction.DESC;
        Sort.Order sortOrder = new Sort.Order(direction, sortBy);
        Pageable paging = PageRequest.of(page, size, Sort.by(sortOrder));

        Page<User> userPage = userRepository.findAll(paging);
        List<User> userList = new ArrayList<>(userPage.getContent());
        List<UserResponse> userResponseList = new ArrayList<>(userList.size());
        userList.forEach(user -> userResponseList.add(new UserResponse(user)));
        return PageDetails.builder()
            .currentPage(userPage.getNumber())
            .totalItems(userPage.getTotalElements())
            .totalPages(userPage.getTotalPages())
            .content(userResponseList).build();
    }

    public StatusResponse approveKYC(KYCApprovalRequest approvalRequest) throws CustomException {
        String message = "";
        KYCDetails kycDetails = getKycDetails(approvalRequest.getUserId()).orElseThrow(
            () -> ExceptionUtil.getException("KYC details is not uploaded for the requested user", "Please ask user to upload the KYC details first.",
                HttpStatus.NOT_FOUND));

        kycDetails.setPanApproved(approvalRequest.isPanVerified());
        kycDetails.setBankDetailsApproved(approvalRequest.isBankDetailsVerified());
        kycDetails.setAadharApproved(approvalRequest.isAddressVerified());
        kycDetails = kycRepository.insert(kycDetails);

        if (kycDetails.isPanApproved() && kycDetails.isBankDetailsApproved() && kycDetails.isAadharApproved()) {
            User user = userRepository.findById(approvalRequest.getUserId()).orElse(null);
            if (Objects.nonNull(user)) {
                user.setVerified(true);
                userRepository.insert(user);
                notificationService.add(Notification.builder()
                        .id(UUID.randomUUID().toString())
                    .ownerType(UserType.CUSTOMER.name())
                    .updateTime(Instant.now().toString())
                    .acknowledged(false)
                    .userId(user.getId())
                    .type(NotificationType.KYC_ACCEPTED.name())
                    .alertTitle(Constants.KYC_ACCEPTED_MESSAGE).build(), true);
            }
            message = "KYC details are approved.";
        } else {
            message = "KYC approval rejected for : ";
            String errorList = "";
            if (!kycDetails.isPanApproved())
                errorList += ", PAN details";
            if (!kycDetails.isBankDetailsApproved())
                errorList += ", Bank details";
            if (!kycDetails.isAadharApproved())
                errorList += ", Aadhar details";
            message += errorList.substring(1);
            notificationService.add(Notification.builder()
                .ownerType(UserType.CUSTOMER.name())
                .updateTime(Instant.now().toString())
                .acknowledged(false)
                .userId(approvalRequest.getUserId())
                .type(NotificationType.KYC_REJECTED.name())
                .alertTitle(Constants.KYC_REJECTED_MESSAGE).build(), true);
        }
        return StatusResponse.builder().status(message).build();
    }


    public String generateReferralCode() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
