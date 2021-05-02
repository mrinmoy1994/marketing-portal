package in.mrinmoy.example.authentication.util;

public class Constants {
    public static final String Authorization = "Authorization";
    public static final String NEW_USER_MESSAGE = "A new user has joined recently";
    public static final String KYC_REQUEST_MESSAGE = "A new KYC review request has been raised.";
    public static final String KYC_ACCEPTED_MESSAGE = "KYC details has been approved.";
    public static final String KYC_REJECTED_MESSAGE = "KYC details has been rejected.";
    public static final String WITHDRAW_REQUEST_MESSAGE = "Cash withdrawal request details has been raised.";
    public static final String WITHDRAW_REQUEST_APPROVED_MESSAGE = "Cash withdrawal request details has been approved.";
    public static final String WITHDRAW_REQUEST_REJECTED_MESSAGE = "Cash withdrawal request details has been rejected.";
    public static final String ASSET_DOWNLOAD_MESSAGE = "A content has been downloaded.";

    // Errors
    public static final String UNAUTHORISED_ERROR = "Unauthorized access";
    public static final String UNAUTHORISED_REMEDIATION = "User does not have permission to perform this operation.";
    public static final String INVALID_USER_ID = "Invalid userId provided.";
    public static final String INVALID_USER_ID_REMEDIATION = "Please provide valid user id and retry.";
    public static final String INVALID_TOKEN = "Invalid token provided.";
    public static final String INVALID_TOKEN_REMEDIATION = "Please provide valid token and try again";
}
