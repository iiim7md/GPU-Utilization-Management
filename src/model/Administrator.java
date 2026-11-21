package model;

public class Administrator extends User {

    private static final long serialVersionUID = 1L;

    public Administrator() {
        super("admin", "Administrator", "gpu2026");
    }

    @Override
    public boolean isAdmin() {
        return true;
    }
}
