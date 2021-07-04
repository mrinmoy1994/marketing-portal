package in.mrinmoy.example.authentication.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import in.mrinmoy.example.authentication.exception.CustomException;
import in.mrinmoy.example.authentication.model.ContentRecord;
import in.mrinmoy.example.authentication.model.ContentRegistry;
import in.mrinmoy.example.authentication.model.ContentType;
import in.mrinmoy.example.authentication.model.Image;
import in.mrinmoy.example.authentication.model.ImageResponse;
import in.mrinmoy.example.authentication.model.Notification;
import in.mrinmoy.example.authentication.model.NotificationType;
import in.mrinmoy.example.authentication.model.StatusResponse;
import in.mrinmoy.example.authentication.model.User;
import in.mrinmoy.example.authentication.model.UserType;
import in.mrinmoy.example.authentication.repositories.ContentRegistryRepository;
import in.mrinmoy.example.authentication.repositories.ContentRepository;
import in.mrinmoy.example.authentication.repositories.ImageRepository;
import in.mrinmoy.example.authentication.repositories.UserRepository;
import in.mrinmoy.example.authentication.util.Constants;
import in.mrinmoy.example.authentication.util.ExceptionUtil;
import in.mrinmoy.example.authentication.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ContentService {

    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContentRepository contentRepository;
    @Autowired
    private ContentRegistryRepository contentRegistryRepository;
    @Autowired
    private NotificationService notificationService;

    public ImageResponse upload(HttpServletRequest request, MultipartFile imgFile, String subType) throws CustomException {
        try {
            User user = jwtTokenUtil.getCurrentUser(request);
            if (!UserType.ADMIN.name().equalsIgnoreCase(user.getType())) {
                throw ExceptionUtil.getException(Constants.UNAUTHORISED_ERROR, Constants.UNAUTHORISED_REMEDIATION,
                    HttpStatus.UNAUTHORIZED);
            }
            Image dbImage = new Image();
            dbImage.setName(imgFile.getOriginalFilename());
            dbImage.setContent(new Binary(BsonBinarySubType.BINARY, imgFile.getBytes()));
            dbImage.setExtension(imgFile.getContentType());
            dbImage.setType(ContentType.IMAGE.name());
            dbImage.setUpdatedTime(Instant.now().toString());
            dbImage.setSubType(subType);
            return new ImageResponse(imageRepository.save(dbImage));

        } catch (Exception e) {
            log.error("Failed to upload image : {}", imgFile.getName(), e);
            throw ExceptionUtil.getException(e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public byte[] retrieveContent(HttpServletRequest request, String referralCode, String contentId) throws CustomException {
        User temp = User.builder().referralCode(referralCode).build();
        Example<User> userExample = Example.of(temp);
        List<User> userList = this.userRepository.findAll(userExample);
        if(CollectionUtils.isEmpty(userList))
            throw ExceptionUtil.getException(Constants.UNAUTHORISED_ERROR, Constants.UNAUTHORISED_REMEDIATION,
                    HttpStatus.UNAUTHORIZED);
        userList.forEach(user -> {
            String clientIp = fetchClientIp(request);
            ContentRecord contentRecord = ContentRecord.builder()
                    .contentId(contentId)
                    .clientIp(clientIp)
                    .userId(user.getId()).build();
            Example<ContentRecord> recordExample = Example.of(contentRecord);
            List<ContentRecord> contentRecords = contentRepository.findAll(recordExample);
            if(CollectionUtils.isEmpty(contentRecords)){
                contentRecord.setSeenTime(Instant.now().toString());
                contentRepository.save(contentRecord);
            }
        });
        
        Image image = imageRepository.findById(contentId).orElseThrow(
                () -> ExceptionUtil.getException("Wrong image id provided", "Please provide a valid image id and try again.", HttpStatus.NOT_FOUND));
        if(!ContentType.IMAGE.name().equalsIgnoreCase(image.getType()))
            throw ExceptionUtil.getException("Wrong image id provided", "Please provide a valid image id and try again.", HttpStatus.NOT_FOUND);
        return image.getContent().getData();
    }

    private String fetchClientIp(HttpServletRequest request) {
        String ip = Optional.ofNullable(request.getHeader("X-FORWARDED-FOR")).orElse(request.getRemoteAddr());
        if (ip.equals("0:0:0:0:0:0:0:1"))
            ip = "127.0.0.1";
        return ip;
    }

    public byte[] retrieveContent(String imageId) throws CustomException {
        Image image = imageRepository.findById(imageId).orElseThrow(
                () -> ExceptionUtil.getException("Wrong image id provided", "Please provide a valid image id and try again.", HttpStatus.NOT_FOUND));
        if(!ContentType.IMAGE.name().equalsIgnoreCase(image.getType()))
            throw ExceptionUtil.getException("Wrong image id provided", "Please provide a valid image id and try again.", HttpStatus.NOT_FOUND);
        return image.getContent().getData();
    }

    public List<ImageResponse> retrieveAllContents(int page, int size) {
        Sort.Order sortOrder = new Sort.Order(Sort.Direction.DESC, "updatedTime");
        Pageable paging = PageRequest.of(page, size, Sort.by(sortOrder));

        Image image = Image.builder().type(ContentType.IMAGE.name()).build();
        Example<Image> imageExample = Example.of(image);
        List<Image> imageList = imageRepository.findAll(imageExample, paging).getContent();
        List<ImageResponse> imageResponseList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(imageList))
            imageList.forEach(item -> imageResponseList.add(new ImageResponse(item)));
        return imageResponseList;
    }

    public void deleteContent(HttpServletRequest request, String contentId) throws CustomException {
        User currentUser = jwtTokenUtil.getCurrentUser(request);
        if (!UserType.ADMIN.name().equalsIgnoreCase(currentUser.getType())) {
            throw ExceptionUtil.getException(Constants.UNAUTHORISED_ERROR, Constants.UNAUTHORISED_REMEDIATION,
                    HttpStatus.UNAUTHORIZED);
        }

        Image image = imageRepository.findById(contentId).orElseThrow(
                () -> ExceptionUtil.getException("Wrong image id provided", "Please provide a valid image id and try again.", HttpStatus.NOT_FOUND));
        if(!ContentType.IMAGE.name().equalsIgnoreCase(image.getType()))
            throw ExceptionUtil.getException("Wrong image id provided", "Please provide a valid image id and try again.", HttpStatus.NOT_FOUND);

        imageRepository.delete(image);
    }

    public StatusResponse downloadContent(HttpServletRequest request, String imageId) throws CustomException {
        User currentUser = jwtTokenUtil.getCurrentUser(request);
        Image image = imageRepository.findById(imageId).orElseThrow(() -> ExceptionUtil.getException("Wrong image id provided", "Please provide a valid image id and try again.", HttpStatus.NOT_FOUND));
        contentRegistryRepository.save(ContentRegistry.builder()
                .contentId(imageId)
                .contentName(image.getName())
                .downloadTime(Instant.now().toString())
                .userId(currentUser.getId()).build());
        notificationService.add(Notification.builder()
                .id(UUID.randomUUID().toString())
                .alertTitle(Constants.ASSET_DOWNLOAD_MESSAGE)
                .type(NotificationType.ASSET_DOWNLOAD.name())
                .ownerType(UserType.ADMIN.name())
                .userId(currentUser.getId())
                .acknowledged(false)
                .updateTime(Instant.now().toString()).build(), false);

        return StatusResponse.builder().status("Content download request has been registered.").build();
    }
}
