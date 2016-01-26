package com.misfit.syncsdk.enums;

/**
 * move from .prometheus.common.enums.HTTPStatus
 */
public class HttpStatus {
    public static final int HTTP_STATUS_OK = 200;
    public static final int API_STATUS_OK = 1000;
    public static final int API_STATUS_FORCE_UPDATE = 1001;
    public static final int API_STATUS_SYSTEM_ERROR = 1100;
    public static final int API_STATUS_INVALID_PARAMS = 1101;
    public static final int API_STATUS_INTERNAL_SYSTEM_ERROR = 1102;
    public static final int API_STATUS_INVALID_TOKEN = 1103;
    public static final int API_STATUS_DUPLICATED_REQUEST = 1104;
    public static final int API_STATUS_ALREADY_DELETED = 1106;
    public static final int API_STATUS_EMAIL_EXISTS = 2500;
    public static final int HTTP_STATUS_FORCE_CLIENT_UPDATE = 210;
    public static final int HTTP_STATUS_BAD_REQUEST = 400;
    public static final int HTTP_STATUS_INVALID_TOKEN = 401;
    public static final int HTTP_STATUS_NEED_2_UPDATE_LATEST_VER = 403;
    public static final int HTTP_STATUS_UNEXPECTED_SERVER_ERROR = 500;
    public static final int HTTP_STATUS_FRIEND_REQUESTED = 305;
    public static final int HTTP_STATUS_NOT_FRIENDED_YET = 306;
    public static final int HTTP_STATUS_SERVER_DOWN = 503;
}
