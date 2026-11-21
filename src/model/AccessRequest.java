package model;

import java.io.Serializable;

public class AccessRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum RequestStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

    private String universityId;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private RequestStatus status;

    public AccessRequest(String universityId, String fullName,
                         String email, String phone, String password) {

        if (universityId == null || universityId.trim().isEmpty() ||
            fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {

            throw new IllegalArgumentException(
                "All registration fields are required."
            );
        }

        this.universityId = universityId.trim();
        this.fullName = fullName.trim();
        this.email = email.trim();
        this.phone = phone.trim();
        this.password = password;

        this.status = RequestStatus.PENDING;
    }

    public String getUniversityId() {
        return universityId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void approve() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved.");
        }

        status = RequestStatus.APPROVED;
    }

    public void reject() {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be rejected.");
        }

        status = RequestStatus.REJECTED;
    }
}
