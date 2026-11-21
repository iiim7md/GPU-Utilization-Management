package logic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.AccessRequest;
import model.AccessRequest.RequestStatus;
import model.Administrator;
import model.AppState;
import model.Student;
import model.TimeGrant;
import model.User;

public class AccountService {

    private final AppState state;
    private final Administrator admin;

    public AccountService(AppState state) {
        if (state == null) {
            throw new IllegalArgumentException("AppState cannot be null.");
        }

        this.state = state;
        this.admin = new Administrator();
    }

    public void register(String id, String name,
                         String email, String phone,
                         String password) {

        requireText(id, "University ID");
        requireText(name, "Full name");
        requireText(email, "Email");
        requireText(phone, "Phone");
        requireText(password, "Password");

        id = id.trim();

        if (!id.matches("\\d+")) {
            throw new IllegalArgumentException(
                "University ID must contain only digits."
            );
        }

        if (!email.trim().matches(
                "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Invalid email address.");
        }

        if (!phone.trim().matches("\\+?\\d{7,15}")) {
            throw new IllegalArgumentException("Invalid phone number.");
        }

        if (state.getStudents().containsKey(id) ||
            state.getRequests().containsKey(id)) {

            throw new IllegalArgumentException("University ID already exists.");
        }

        AccessRequest request = new AccessRequest(id, name, email, phone, password);

        state.getRequests().put(id, request);
    }

    public Student approve(User actor, String id,
                           int initialMinutes) {

        requireAdmin(actor);
        AccessRequest request = findRequest(id);

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request has already been processed.");
        }

        if (state.getStudents().containsKey(id.trim())) {
            throw new IllegalStateException("Student already exists.");
        }

        if (initialMinutes <= 0) {
            throw new IllegalArgumentException("Initial minutes must be positive.");
        }

        Student student = new Student(
            request.getUniversityId(),
            request.getFullName(),
            request.getEmail(),
            request.getPhone(),
            request.getPassword(),
            initialMinutes
        );

        request.approve();

        state.getStudents().put(
            request.getUniversityId(), student
        );

        return student;
    }

    public Student approve(User actor, String id) {
        return approve(actor, id, 100);
    }

    public void reject(User actor, String id) {
        requireAdmin(actor);

        AccessRequest request = findRequest(id);
        request.reject();
    }

    public User signIn(String username, String password) {

        requireText(username, "Username");
        requireText(password, "Password");

        username = username.trim();

        if (username.equals("admin")) {
            if (admin.checkPassword(password)) {
                return admin;
            }

            throw new IllegalArgumentException("Incorrect administrator password.");
        }

        Student student = state.getStudents().get(username);

        if (student != null) {
            if (student.checkPassword(password)) {
                return student;
            }

            throw new IllegalArgumentException("Incorrect password.");
        }

        AccessRequest request = state.getRequests().get(username);

        if (request != null) {
            if (request.getStatus() == RequestStatus.PENDING) {
                throw new IllegalStateException("Your registration is still pending.");
            }

            if (request.getStatus() == RequestStatus.REJECTED) {
                throw new IllegalStateException("Your registration was rejected.");
            }
        }

        throw new IllegalArgumentException("Invalid username or password.");
    }

    public Student findStudent(User actor, String id) {
        requireAdmin(actor);
        requireText(id, "University ID");

        Student student =state.getStudents().get(id.trim());

        if (student == null) {
            throw new IllegalArgumentException("Student not found.");
        }

        return student;
    }

    public TimeGrant grantMinutes(User actor, String studentId, int minutes, String reason) {

        requireAdmin(actor);
        requireText(reason, "Reason");

        Student student = findStudent(actor, studentId);

        if (minutes <= 0) {
            throw new IllegalArgumentException("Minutes must be positive.");
        }

        LocalDateTime currentTime = state.getCurrentTime();

        if (currentTime == null) {
            throw new IllegalStateException("Simulation clock is not initialized.");
        }

        TimeGrant grant = new TimeGrant(
            studentId.trim(),
            minutes,
            reason,
            actor.getUniversityId(),
            currentTime
        );

        student.getAllowance().grantMinutes(minutes);
        state.getGrants().add(grant);

        return grant;
    }

    public List<AccessRequest> getPendingRequests(User actor) {
        requireAdmin(actor);

        List<AccessRequest> pending = new ArrayList<>();

        for (AccessRequest request :
                state.getRequests().values()) {

            if (request.getStatus() == RequestStatus.PENDING) {
                pending.add(request);
            }
        }

        return pending;
    }

    private AccessRequest findRequest(String id) {
        requireText(id, "University ID");

        AccessRequest request =
            state.getRequests().get(id.trim());

        if (request == null) {
            throw new IllegalArgumentException("Registration request not found.");
        }

        return request;
    }

    private void requireAdmin(User actor) {
        if (actor == null || !actor.isAdmin()
                || !"admin".equals(actor.getUniversityId())) {
            throw new SecurityException("Administrator permission required.");
        }
    }

    private void requireText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " is required.");
        }
    }
}
