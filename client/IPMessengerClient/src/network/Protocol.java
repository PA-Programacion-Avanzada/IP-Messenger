package network;

public final class Protocol {
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_RECOVER_PASSWORD = "RECOVER_PASSWORD";
    public static final String CMD_SEND_FRIEND_MSG = "SEND_FRIEND_MSG";
    public static final String CMD_SEND_TEMP_MSG = "SEND_TEMP_MSG";

    public static final String RES_OK = "OK";
    public static final String RES_ERROR = "ERROR";
    public static final String RES_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String RES_LOGIN_FAIL = "LOGIN_FAIL";
    public static final String RES_NEED_REGISTER = "NEED_REGISTER";
    public static final String RES_FRIEND_LIST = "FRIEND_LIST";
    public static final String RES_GROUP_LIST = "GROUP_LIST";
    public static final String RES_USER_LIST = "USER_LIST";
    public static final String RES_PENDING_MESSAGES = "PENDING_MESSAGES";
    public static final String RES_NEW_MESSAGE = "NEW_MESSAGE";

    private Protocol() {
    }
}
