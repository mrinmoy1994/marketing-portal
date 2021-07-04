package in.mrinmoy.example.authentication.controller;

import in.mrinmoy.example.authentication.exception.CustomException;
import in.mrinmoy.example.authentication.model.JwtRequest;
import in.mrinmoy.example.authentication.model.JwtResponse;
import in.mrinmoy.example.authentication.model.Transaction;
import in.mrinmoy.example.authentication.service.TransactionService;
import in.mrinmoy.example.authentication.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = { RequestMethod.DELETE, RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS,
        RequestMethod.PATCH, RequestMethod.POST, RequestMethod.PUT })
@RequestMapping(value = "/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

//    @PostMapping(value = "/create")
//    public ResponseEntity<?> performTransaction(HttpServletRequest request, @RequestBody Transaction transaction) {
//        try {
//            return ResponseEntity.ok(transactionService.performTransaction(request, transaction));
//        } catch (CustomException e) {
//            log.error("Exception occur while logging in: ", e);
//            return ExceptionUtil.getExceptionResponse(e);
//        }
//    }
}
