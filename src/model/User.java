package model;

import java.io.Serializable;

public abstract class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String universityId;
    private String fullName;
    private String password;

    public User(String universityId, String fullName, String password) {
        this.universityId = universityId;
        this.fullName = fullName;
        this.password = password;
    }

    public String getUniversityId() {
        return universityId;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean checkPassword(String enteredPassword) {
        return password.equals(enteredPassword);
    }

    public abstract boolean isAdmin();
}
