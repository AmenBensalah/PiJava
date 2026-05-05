package edu.esportify.services;

import edu.esportify.entities.User;
import edu.esportify.entities.UserRole;
import edu.esportify.interfaces.IService;
import edu.esportify.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IService<User> {
    private static final String PRIMARY_TABLE = "user";
    private static final String FALLBACK_TABLE = "app_user";
    private static final List<User> LOCAL_DATA = new ArrayList<>();
    private static int nextId = 1;
    private static boolean seedingInProgress;
    private static boolean seeded;

    public UserService() {
        try {
            ensureSeedUsers();
        } catch (RuntimeException e) {
            System.out.println("Initialisation UserService en mode degrade: " + e.getMessage());
        }
    }

    @Override
    public void addEntity(User user) {
        if (!hasConnection()) {
            if (user.getId() <= 0) {
                user.setId(nextId++);
            }
            LOCAL_DATA.removeIf(item -> item.getId() == user.getId());
            LOCAL_DATA.add(user);
            return;
        }
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(buildInsertSql());
            bindWriteUser(pst, user);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout utilisateur: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEntity(User user) {
        if (!hasConnection()) {
            LOCAL_DATA.removeIf(item -> item.getId() == user.getId());
            return;
        }
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement("DELETE FROM " + resolveUserTable() + " WHERE id = ?");
            pst.setInt(1, user.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression utilisateur: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateEntity(int id, User user) {
        if (!hasConnection()) {
            user.setId(id);
            LOCAL_DATA.removeIf(item -> item.getId() == id);
            LOCAL_DATA.add(user);
            return;
        }
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(buildUpdateSql());
            bindWriteUser(pst, user);
            pst.setInt(PRIMARY_TABLE.equals(resolveUserTable()) ? 6 : 9, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise a jour utilisateur: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> getData() {
        ensureSeedUsers();
        return getDataInternal();
    }

    public User findByUsernameOrEmail(String value) {
        String normalized = normalize(value);
        return getDataInternal().stream()
                .filter(user -> normalized.equalsIgnoreCase(normalize(user.getUsername()))
                        || normalized.equalsIgnoreCase(normalize(user.getEmail())))
                .findFirst()
                .orElse(null);
    }

    public boolean usernameExists(String username) {
        String normalized = normalize(username);
        return getDataInternal().stream().anyMatch(user -> normalized.equalsIgnoreCase(normalize(user.getUsername())));
    }

    public boolean emailExists(String email) {
        String normalized = normalize(email);
        return getDataInternal().stream().anyMatch(user -> normalized.equalsIgnoreCase(normalize(user.getEmail())));
    }

    public User findByUsername(String username) {
        String normalized = normalize(username);
        return getDataInternal().stream()
                .filter(user -> normalized.equalsIgnoreCase(normalize(user.getUsername())))
                .findFirst()
                .orElse(null);
    }

    public String generateUniqueUsername(String seed) {
        String base = normalize(seed).toLowerCase().replaceAll("[^a-z0-9]", "");
        if (base.isBlank()) {
            base = "player";
        }
        String candidate = base;
        int suffix = 1;
        while (usernameExists(candidate)) {
            suffix++;
            candidate = base + suffix;
        }
        return candidate;
    }

    public boolean updateRoleByUsernameOrEmail(String usernameOrEmail, UserRole role) {
        String normalized = normalize(usernameOrEmail);
        if (normalized.isBlank() || role == null) {
            return false;
        }
        if (!hasConnection()) {
            for (User user : LOCAL_DATA) {
                if (normalized.equalsIgnoreCase(normalize(user.getUsername()))
                        || normalized.equalsIgnoreCase(normalize(user.getEmail()))) {
                    user.setRole(role);
                    return true;
                }
            }
            return false;
        }

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(buildRoleUpdateSql());
            pst.setString(1, PRIMARY_TABLE.equals(resolveUserTable()) ? toDatabaseRole(role) : role.name());
            pst.setString(2, normalized);
            pst.setString(3, normalized);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur mise a jour role utilisateur: " + e.getMessage(), e);
        }
    }

    private List<User> getDataInternal() {
        if (!hasConnection()) {
            return new ArrayList<>(LOCAL_DATA);
        }
        List<User> data = new ArrayList<>();
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM " + resolveUserTable() + " ORDER BY id DESC");
            while (rs.next()) {
                data.add(mapUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture utilisateurs: " + e.getMessage(), e);
        }
        return data;
    }

    private void ensureSeedUsers() {
        if (seeded || seedingInProgress) {
            return;
        }
        seedingInProgress = true;
        try {
            if (hasConnection()) {
                if (findExistingCount() > 0) {
                    seeded = true;
                    return;
                }
            } else if (!LOCAL_DATA.isEmpty()) {
                seeded = true;
                return;
            }

            createSeedUser("admin", "Admin", "admin@esportify.gg", "00000000", "admin123", UserRole.ADMIN);
            createSeedUser("manager", "Manager", "manager@esportify.gg", "00000000", "manager123", UserRole.MANAGER);
            createSeedUser("user", "User", "user@esportify.gg", "00000000", "user123", UserRole.USER);
            seeded = true;
        } finally {
            seedingInProgress = false;
        }
    }

    private int findExistingCount() {
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + resolveUserTable());
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture utilisateurs: " + e.getMessage(), e);
        }
    }

    private void createSeedUser(String username, String firstName, String email, String phoneNumber, String password, UserRole role) {
        if (usernameExists(username) || emailExists(email)) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setEmail(email);
        user.setPhoneNumber(phoneNumber);
        user.setPassword(password);
        user.setRole(role);
        addEntity(user);
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(firstNonBlank(readIfPresent(rs, "username"), readIfPresent(rs, "pseudo"), extractNameFromEmail(rs.getString("email"))));
        user.setFirstName(firstNonBlank(readIfPresent(rs, "first_name"), readIfPresent(rs, "nom"), user.getUsername()));
        user.setEmail(rs.getString("email"));
        user.setPhoneNumber(readIfPresent(rs, "phone_number"));
        user.setPassword(rs.getString("password"));
        user.setRole(UserRole.fromValue(rs.getString("role")));
        user.setActive(!hasColumn(rs, "is_active") || rs.getBoolean("is_active"));
        if (hasColumn(rs, "created_at")) {
            Timestamp timestamp = rs.getTimestamp("created_at");
            if (timestamp != null) {
                user.setCreatedAt(timestamp.toLocalDateTime());
            }
        }
        return user;
    }

    private boolean hasConnection() {
        return MyConnection.getInstance().getCnx() != null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String resolveUserTable() {
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery("SHOW TABLES LIKE '" + PRIMARY_TABLE + "'");
            if (rs.next()) {
                return PRIMARY_TABLE;
            }
        } catch (SQLException ignored) {
        }
        return FALLBACK_TABLE;
    }

    private String buildInsertSql() {
        if (PRIMARY_TABLE.equals(resolveUserTable())) {
            return "INSERT INTO user (email, password, nom, role, pseudo) VALUES (?, ?, ?, ?, ?)";
        }
        return "INSERT INTO app_user (username, first_name, email, phone_number, password, role, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private String buildUpdateSql() {
        if (PRIMARY_TABLE.equals(resolveUserTable())) {
            return "UPDATE user SET email = ?, password = ?, nom = ?, role = ?, pseudo = ? WHERE id = ?";
        }
        return "UPDATE app_user SET username = ?, first_name = ?, email = ?, phone_number = ?, password = ?, role = ?, is_active = ?, created_at = ? WHERE id = ?";
    }

    private String buildRoleUpdateSql() {
        if (PRIMARY_TABLE.equals(resolveUserTable())) {
            return "UPDATE user SET role = ? WHERE LOWER(COALESCE(pseudo, '')) = LOWER(?) OR LOWER(email) = LOWER(?)";
        }
        return "UPDATE app_user SET role = ? WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)";
    }

    private void bindWriteUser(PreparedStatement pst, User user) throws SQLException {
        if (PRIMARY_TABLE.equals(resolveUserTable())) {
            pst.setString(1, user.getEmail());
            pst.setString(2, user.getPassword());
            pst.setString(3, firstNonBlank(user.getFirstName(), user.getUsername(), "User"));
            pst.setString(4, toDatabaseRole(user.getRole()));
            pst.setString(5, firstNonBlank(user.getUsername(), user.getFirstName(), extractNameFromEmail(user.getEmail())));
            return;
        }
        pst.setString(1, user.getUsername());
        pst.setString(2, user.getFirstName());
        pst.setString(3, user.getEmail());
        pst.setString(4, user.getPhoneNumber());
        pst.setString(5, user.getPassword());
        pst.setString(6, user.getRole().name());
        pst.setBoolean(7, user.isActive());
        pst.setTimestamp(8, Timestamp.valueOf(ensureCreatedAt(user)));
    }

    private LocalDateTime ensureCreatedAt(User user) {
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        return user.getCreatedAt();
    }

    private String toDatabaseRole(UserRole role) {
        return switch (role == null ? UserRole.USER : role) {
            case ADMIN -> "ROLE_ADMIN";
            case MANAGER -> "ROLE_MANAGER";
            case USER -> "ROLE_JOUEUR";
        };
    }

    private String readIfPresent(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException ignored) {
            return null;
        }
    }

    private boolean hasColumn(ResultSet rs, String column) {
        try {
            rs.findColumn(column);
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String extractNameFromEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }
}
