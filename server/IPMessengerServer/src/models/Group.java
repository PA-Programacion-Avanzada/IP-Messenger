package models;

import java.sql.Timestamp;

public class Group {
    private int id;
    private String name;
    private int creatorId;

    public Group(int aInt, String string, int aInt1, Timestamp timestamp) {
    }

    public Group(int id, String name, int creatorId) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
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
}