package in.mrinmoy.example.authentication.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import in.mrinmoy.example.authentication.exception.CustomException;
import in.mrinmoy.example.authentication.model.StatusResponse;
import in.mrinmoy.example.authentication.service.NotificationService;
import in.mrinmoy.example.authentication.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.DELETE, RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS,
        RequestMethod.PATCH, RequestMethod.POST, RequestMethod.PUT })
@RequestMapping(value = "/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;

    @GetMapping(value = "/getAll")
    public ResponseEntity<?> getNotifications(HttpServletRequest request,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "30") int size) {
        try {
            return ResponseEntity.ok(notificationService.getNotificationsForCurrentUser(request, page, size));
        } catch (CustomException e) {
            return ExceptionUtil.getExceptionResponse(e);
        }
    }

    @GetMapping(value = "/acknowledge")
    public ResponseEntity<?> acknowledgeNotification(@RequestParam("notificationId") String notificationId) {
        notificationService.acknowledge(notificationId);
        return ResponseEntity.ok(StatusResponse.builder().status("Success").build());
    }
}
