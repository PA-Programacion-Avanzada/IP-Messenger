package models;

import java.sql.Timestamp;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String status; // "online", "offline"
    private Timestamp lastSeen;

    public User() {}

    public User(int id, String username, String passwordHash, String status, Timestamp lastSeen) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.status = status;
        this.lastSeen = lastSeen;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getLastSeen() { return lastSeen; }
    public void setLastSeen(Timestamp lastSeen) { this.lastSeen = lastSeen; }
}