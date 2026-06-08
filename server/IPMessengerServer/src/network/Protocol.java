package network;

public class Protocol {
    // Comandos del cliente al servidor
    public static final String CMD_LOGIN = "LOGIN";
    public static final String CMD_REGISTER = "REGISTER";
    public static final String CMD_RECOVER_PASSWORD = "RECOVER_PASSWORD";
    public static final String CMD_SEND_FRIEND_MSG = "SEND_FRIEND_MSG";
    public static final String CMD_SEND_GROUP_MSG = "SEND_GROUP_MSG";
    public static final String CMD_SEND_TEMP_MSG = "SEND_TEMP_MSG";
    public static final String CMD_GET_FRIENDS = "GET_FRIENDS";
    public static final String CMD_GET_GROUPS = "GET_GROUPS";
    public static final String CMD_GET_ALL_USERS = "GET_ALL_USERS";
    public static final String CMD_CREATE_GROUP = "CREATE_GROUP";
    public static final String CMD_INVITE_TO_GROUP = "INVITE_TO_GROUP";
    public static final String CMD_ACCEPT_GROUP_INVITE = "ACCEPT_GROUP_INVITE";
    public static final String CMD_REJECT_GROUP_INVITE = "REJECT_GROUP_INVITE";
    public static final String CMD_SEND_FRIEND_REQUEST = "SEND_FRIEND_REQUEST";
    public static final String CMD_ACCEPT_FRIEND_REQUEST = "ACCEPT_FRIEND_REQUEST";
    public static final String CMD_REJECT_FRIEND_REQUEST = "REJECT_FRIEND_REQUEST";
    public static final String CMD_GET_FRIEND_INVITES = "GET_FRIEND_INVITES";
    public static final String CMD_LEAVE_GROUP = "LEAVE_GROUP";
    public static final String CMD_GET_PENDING_MSGS = "GET_PENDING_MSGS";
    public static final String CMD_MARK_MSG_READ = "MARK_MSG_READ";
    public static final String CMD_GET_FRIEND_HISTORY = "GET_FRIEND_HISTORY";
    public static final String CMD_GET_GROUP_HISTORY = "GET_GROUP_HISTORY";
    public static final String CMD_GET_GROUP_INVITES = "GET_GROUP_INVITES";

    // Respuestas del servidor
    public static final String RES_OK = "OK";
    public static final String RES_FRIEND_INVITE_LIST = "FRIEND_INVITE_LIST";
    public static final String RES_ERROR = "ERROR";
    public static final String RES_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String RES_LOGIN_FAIL = "LOGIN_FAIL";
    public static final String RES_NEED_REGISTER = "NEED_REGISTER";
    public static final String RES_NEED_RECOVER = "NEED_RECOVER";
    public static final String RES_FRIEND_LIST = "FRIEND_LIST";
    public static final String RES_GROUP_LIST = "GROUP_LIST";
    public static final String RES_USER_LIST = "USER_LIST";
    public static final String RES_PENDING_MESSAGES = "PENDING_MESSAGES";
    public static final String RES_NEW_MESSAGE = "NEW_MESSAGE";
    public static final String RES_FRIEND_HISTORY = "FRIEND_HISTORY";
    public static final String RES_GROUP_HISTORY = "RES_GROUP_HISTORY";
    public static final String RES_GROUP_INVITE_LIST = "GROUP_INVITE_LIST";
}