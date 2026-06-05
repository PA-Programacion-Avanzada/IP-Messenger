package models;

import java.sql.Timestamp;

public class Group {
    private int id;
    private String name;
    private int creatorId;
<<<<<<< HEAD

    public Group() {
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
=======
    private Timestamp createdAt;

    // constructores, getters, setters
}
>>>>>>> 7882dce65d1052a24e564685c9b36b9aca0a72b2
