package models;

import java.sql.Timestamp;

public class Message {
    private int id;
    private int senderId;
    private String receiverType; // "user" o "group"
    private int receiverId;
    private String content;      // ya comprimido? mejor guardar el texto plano, compresión en tránsito
    private String status;       // "sent", "delivered", "read", "pending"
    private Timestamp timestamp;

    // getters/setters
}