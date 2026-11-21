package model;

public class Student extends User {

    private static final long serialVersionUID = 1L;

    private String email;
    private String phone;
    private TimeAllowance allowance;

    public Student(String universityId, String fullName, String email, String phone, String password, int initialMinutes) {

        super(universityId, fullName, password);

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        this.email = email;
        this.phone = phone;
        this.allowance = new TimeAllowance(initialMinutes);
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public TimeAllowance getAllowance() {
        return allowance;
    }

    @Override
    public boolean isAdmin() {
        return false;
    }
}
