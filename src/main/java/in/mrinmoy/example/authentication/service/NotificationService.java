package in.mrinmoy.example.authentication.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import in.mrinmoy.example.authentication.util.ExceptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import in.mrinmoy.example.authentication.exception.CustomException;
import in.mrinmoy.example.authentication.model.Notification;
import in.mrinmoy.example.authentication.model.PageDetails;
import in.mrinmoy.example.authentication.model.User;
import in.mrinmoy.example.authentication.model.UserType;
import in.mrinmoy.example.authentication.repositories.NotificationRepository;
import in.mrinmoy.example.authentication.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;

@Service
@Slf4j
public class NotificationService {
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Autowired
    private NotificationRepository notificationRepository;

    public void add(Notification notification, boolean allowOverwrite) {
        if (!allowOverwrite) {
            log.info("Adding new notification.");
            notificationRepository.save(notification);
            log.info("A New notification has been added.");
            return;
        }

        log.info("Updating existing notification.");
        Notification temp = Notification.builder().type(notification.getType()).userId(notification.getUserId()).build();
        Example<Notification> notificationExample = Example.of(temp);
        List<Notification> notificationList = new ArrayList<>(this.notificationRepository.findAll(notificationExample));
        if (CollectionUtils.isEmpty(notificationList)) {
            notificationRepository.save(notification);
        } else {
            notificationList.forEach(item -> {
                item.setAcknowledged(false);
                item.setUpdateTime(Instant.now().toString());
                item.setAlertTitle(notification.getAlertTitle());
                notificationRepository.save(item);
            });
        }
        log.info("Existing notification has been updated.");
    }

    public void acknowledge(String notificationId) {
        log.info("Fetching details...");
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setAcknowledged(true);
            notificationRepository.save(notification);
        });
    }

    public PageDetails getNotificationsForCurrentUser(HttpServletRequest request, int page, int size) throws CustomException {
        User user = jwtTokenUtil.getCurrentUser(request);
        Notification temp;
        if (UserType.ADMIN.name().equalsIgnoreCase(user.getType()))
            temp = Notification.builder().ownerType(user.getType()).build();
        else
            temp = Notification.builder().userId(user.getId()).ownerType(user.getType()).build();
        log.info("example : {}", temp.toString());
        Example<Notification> notificationExample = Example.of(temp);
        List<Notification> notificationList = new ArrayList<>();

        Sort.Order sortOrder = new Sort.Order(Sort.Direction.DESC, "updateTime");
        Pageable paging = PageRequest.of(page, size, Sort.by(sortOrder));

        Page<Notification> notificationPage = notificationRepository.findAll(notificationExample, paging);
        notificationList.addAll(notificationPage.getContent());
        return PageDetails.builder()
            .currentPage(notificationPage.getNumber())
            .totalItems(notificationPage.getTotalElements())
            .totalPages(notificationPage.getTotalPages())
            .content(notificationList).build();
    }

    public Notification getById(String notificationId) throws CustomException {
        log.info("id : {}", notificationId);
        notificationRepository.findAll().forEach(notification -> log.info("id : {}", notification.getId()));
        List<Notification> notificationList = notificationRepository.findAll().stream().filter(notification -> notification.getId().equalsIgnoreCase(notificationId)).collect(Collectors.toList());
        if (notificationList.isEmpty())
            throw ExceptionUtil.getException("Invalid notification ID provided", "Provide valid notification ID", HttpStatus.NOT_FOUND);
        return notificationList.get(0);
    }
}
