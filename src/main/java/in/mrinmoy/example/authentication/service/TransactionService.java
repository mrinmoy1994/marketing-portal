package in.mrinmoy.example.authentication.service;

import in.mrinmoy.example.authentication.exception.CustomException;
import in.mrinmoy.example.authentication.model.Transaction;
import in.mrinmoy.example.authentication.model.User;
import in.mrinmoy.example.authentication.model.UserType;
import in.mrinmoy.example.authentication.util.Constants;
import in.mrinmoy.example.authentication.util.ExceptionUtil;
import in.mrinmoy.example.authentication.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class TransactionService {
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

//    public Transaction performTransaction(HttpServletRequest request, Transaction transaction) throws CustomException {
//        User currentUser = jwtTokenUtil.getCurrentUser(request);
//        if(!UserType.ADMIN.name().equalsIgnoreCase(currentUser.getType())){
//            throw ExceptionUtil.getException(Constants.UNAUTHORISED_ERROR, Constants.UNAUTHORISED_REMEDIATION,
//                    HttpStatus.UNAUTHORIZED);
//        }
//        return
//    }
}
