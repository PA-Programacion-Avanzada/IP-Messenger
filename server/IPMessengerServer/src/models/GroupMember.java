package models;

import java.sql.Timestamp;

public class GroupMember {
    private int id;
    private String name;
    private int creatorId;
    private Timestamp createdAt;

    public GroupMember() {
    }

    // Constructor con todos los parámetros
    public GroupMember(int id, String name, int creatorId, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}