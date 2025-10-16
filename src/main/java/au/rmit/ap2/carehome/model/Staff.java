package au.rmit.ap2.carehome.model;

import java.io.Serializable;
import java.util.UUID;

public abstract class Staff implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String id = UUID.randomUUID().toString();
    private final Role role;
    private String name, username, password;

    protected Staff(Role role, String name, String username, String password) {
        this.role = role; this.name = name; this.username = username; this.password = password;
    }
    public String getId() { return id; }
    public Role getRole() { return role; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public boolean checkPassword(String pw) { return password != null && password.equals(pw); }
    public void setPassword(String pw) { this.password = pw; }
}
