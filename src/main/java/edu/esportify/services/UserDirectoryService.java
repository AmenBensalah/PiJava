package edu.esportify.services;

import edu.esportify.entities.User;
import edu.esportify.entities.UserProfile;
import edu.esportify.navigation.AppSession;
import edu.esportify.tools.MyConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class UserDirectoryService {
    private final MyConnection myConnection;

    public UserDirectoryService() {
        this(MyConnection.getInstance());
    }

    public UserDirectoryService(MyConnection myConnection) {
        this.myConnection = myConnection;
    }

    public List<UserProfile> getUsers() {
        List<UserProfile> users = readUsers("user");
        if (!users.isEmpty()) {
            return users;
        }
        users = readUsers("app_user");
        if (!users.isEmpty()) {
            return users;
        }
        users = readUsers("users");
        if (!users.isEmpty()) {
            return users;
        }
        return fallbackUsers();
    }

    public Map<Integer, UserProfile> getUsersById() {
        Map<Integer, UserProfile> usersById = new LinkedHashMap<>();
        for (UserProfile user : getUsers()) {
            usersById.put(user.getId(), user);
        }
        return usersById;
    }

    public UserProfile resolveCurrentUser() {
        User sessionUser = AppSession.getInstance().getCurrentUser();
        if (sessionUser != null) {
            String displayName = firstNonBlank(
                    sessionUser.getUsername(),
                    sessionUser.getFirstName(),
                    extractNameFromEmail(sessionUser.getEmail()),
                    "User " + sessionUser.getId()
            );
            String role = sessionUser.getRole() == null ? "USER" : sessionUser.getRole().name();
            return new UserProfile(
                    sessionUser.getId(),
                    displayName,
                    firstNonBlank(sessionUser.getEmail(), ""),
                    role,
                    extractAvatar(displayName)
            );
        }
        return getUsers().stream().findFirst().orElse(new UserProfile(1, "Vous", "user@esportify.local", "USER", "V"));
    }

    public String resolveDisplayName(Integer authorId) {
        if (authorId == null) {
            return "System";
        }
        return Optional.ofNullable(getUsersById().get(authorId))
                .map(UserProfile::getDisplayName)
                .orElse("Auteur #" + authorId);
    }

    private List<UserProfile> readUsers(String tableName) {
        String sql = buildUsersQuery(tableName);
        List<UserProfile> users = new ArrayList<>();
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String displayName = rs.getString("display_name");
                users.add(new UserProfile(
                        id,
                        displayName == null || displayName.isBlank() ? "User " + id : displayName,
                        rs.getString("email"),
                        rs.getString("role"),
                        extractAvatar(displayName)
                ));
            }
        } catch (SQLException ignored) {
            return List.of();
        }
        return users;
    }

    private String buildUsersQuery(String tableName) {
        if ("app_user".equalsIgnoreCase(tableName)) {
            return "SELECT id, COALESCE(NULLIF(username, ''), NULLIF(first_name, ''), SUBSTRING_INDEX(email, '@', 1), CONCAT('User ', id)) AS display_name, "
                    + "COALESCE(email, '') AS email, COALESCE(role, 'USER') AS role FROM app_user ORDER BY id";
        }
        return "SELECT id, COALESCE(NULLIF(pseudo, ''), NULLIF(nom, ''), SUBSTRING_INDEX(email, '@', 1), CONCAT('User ', id)) AS display_name, "
                + "COALESCE(email, '') AS email, COALESCE(role, 'USER') AS role FROM " + tableName + " ORDER BY id";
    }

    private List<UserProfile> fallbackUsers() {
        return List.of(
                new UserProfile(1, "ilyes", "ilyes@esportify.local", "USER", "I"),
                new UserProfile(2, "sarra", "sarra@esportify.local", "USER", "S"),
                new UserProfile(3, "admin", "admin@esportify.local", "ADMIN", "A")
        );
    }

    private String extractAvatar(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return "U";
        }
        return displayName.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    private String extractNameFromEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        int at = email.indexOf('@');
        return at <= 0 ? email : email.substring(0, at);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private Connection getConnection() {
        return myConnection.getCnx();
    }
}
