package edu.ProjetPI.services;

import edu.ProjetPI.entities.User;
import edu.ProjetPI.interfaces.IService;
import edu.connexion3a77.tools.MyConnection;
import edu.ProjetPI.tools.PasswordUtils;
import edu.ProjetPI.tools.UserValidationRules;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserService implements IService<User> {

    private static volatile boolean schemaChecked = false;
    private final Connection connection;

    public UserService() {
        this.connection = MyConnection.getInstance().getCnx();
        ensureFaceDescriptorColumnExists();
    }

    @Override
    public void add(User user) {
        validate(user, false);
        ensureUniqueEmailForCreate(user.getEmail());
        String sql = "INSERT INTO user(nom, pseudo, email, password, role, face_descriptor_json) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPseudo().trim());
            ps.setString(3, UserValidationRules.normalizeEmail(user.getEmail()));
            ps.setString(4, PasswordUtils.hash(user.getPassword()));
            ps.setString(5, user.getRole());
            ps.setString(6, user.getFaceDescriptorJson());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Cet email existe deja. Veuillez en choisir un autre.");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to add user: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(User user) {
        validate(user, true);
        ensureUniqueEmailForUpdate(user.getId(), user.getEmail());
        String sqlWithPassword = "UPDATE user SET nom = ?, pseudo = ?, email = ?, password = ?, role = ?, face_descriptor_json = ? WHERE id = ?";
        String sqlWithoutPassword = "UPDATE user SET nom = ?, pseudo = ?, email = ?, role = ?, face_descriptor_json = ? WHERE id = ?";
        boolean updatePassword = user.getPassword() != null && !user.getPassword().isBlank();

        try (PreparedStatement ps = connection.prepareStatement(updatePassword ? sqlWithPassword : sqlWithoutPassword)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPseudo().trim());
            ps.setString(3, UserValidationRules.normalizeEmail(user.getEmail()));
            if (updatePassword) {
                ps.setString(4, PasswordUtils.hash(user.getPassword()));
                ps.setString(5, user.getRole());
                ps.setString(6, user.getFaceDescriptorJson());
                ps.setInt(7, user.getId());
            } else {
                ps.setString(4, user.getRole());
                ps.setString(5, user.getFaceDescriptorJson());
                ps.setInt(6, user.getId());
            }
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Cet email existe deja. Veuillez en choisir un autre.");
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to update user: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to delete user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, nom AS full_name, pseudo, email, password, role, face_descriptor_json FROM user ORDER BY id";

        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSet(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to fetch users: " + e.getMessage(), e);
        }
    }

    public Optional<User> authenticate(String email, String password) {
        Optional<User> user = findByEmail(email);
        if (user.isPresent() && PasswordUtils.matches(password, user.get().getPassword())) {
            return user;
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, nom AS full_name, pseudo, email, password, role, face_descriptor_json FROM user WHERE email = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, UserValidationRules.normalizeEmail(email));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to fetch user by email: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public User findOrCreateGoogleUser(String email, String fullName) {
        return findOrCreateOauthUser(email, fullName);
    }

    public User findOrCreateDiscordUser(String email, String fullName) {
        return findOrCreateOauthUser(email, fullName);
    }

    private User findOrCreateOauthUser(String email, String fullName) {
        String normalizedEmail = UserValidationRules.normalizeEmail(email);
        Optional<User> existing = findByEmail(normalizedEmail);
        if (existing.isPresent()) {
            return existing.get();
        }

        String safeName = sanitizeFullName(fullName, normalizedEmail);
        String pseudo = sanitizePseudo(normalizedEmail);
        String generatedPassword = "G-" + UUID.randomUUID();

        add(new User(
                safeName,
                pseudo,
                normalizedEmail,
                generatedPassword,
                "ROLE_JOUEUR",
                null
        ));
        return findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalStateException("Unable to load OAuth user after creation."));
    }

    public void updatePasswordByEmail(String email, String newRawPassword) {
        String normalizedEmail = UserValidationRules.normalizeEmail(email);
        String sql = "UPDATE user SET password = ? WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, PasswordUtils.hash(newRawPassword));
            ps.setString(2, normalizedEmail);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Aucun utilisateur trouve pour cet email.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to update password: " + e.getMessage(), e);
        }
    }

    private User mapResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("pseudo"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getString("face_descriptor_json")
        );
    }

    public List<User> findUsersWithFaceDescriptor() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, nom AS full_name, pseudo, email, password, role, face_descriptor_json "
                + "FROM user WHERE face_descriptor_json IS NOT NULL AND TRIM(face_descriptor_json) <> ''";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSet(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to fetch users with face descriptor: " + e.getMessage(), e);
        }
    }

    private void ensureFaceDescriptorColumnExists() {
        if (schemaChecked) {
            return;
        }
        synchronized (UserService.class) {
            if (schemaChecked) {
                return;
            }
            String sql = "ALTER TABLE user ADD COLUMN face_descriptor_json LONGTEXT NULL";
            try (Statement st = connection.createStatement()) {
                st.executeUpdate(sql);
            } catch (SQLException e) {
                String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
                if (!msg.contains("duplicate column") && !msg.contains("already exists")) {
                    throw new IllegalStateException("Unable to initialize face descriptor column: " + e.getMessage(), e);
                }
            }
            schemaChecked = true;
        }
    }

    private void validate(User user, boolean update) {
        if (user == null) {
            throw new IllegalArgumentException("Les informations utilisateur sont obligatoires.");
        }
        if (update && user.getId() <= 0) {
            throw new IllegalArgumentException("Identifiant utilisateur invalide pour la modification.");
        }
        UserValidationRules.validateFullName(user.getFullName());
        UserValidationRules.validatePseudo(user.getPseudo());
        UserValidationRules.validateEmail(user.getEmail());
        if (!update) {
            UserValidationRules.validatePasswordForCreate(user.getPassword());
        } else {
            UserValidationRules.validatePasswordForUpdate(user.getPassword());
        }
        if (user.getRole() == null || user.getRole().isBlank()) {
            throw new IllegalArgumentException("Le role est obligatoire.");
        }
    }

    private void ensureUniqueEmailForCreate(String email) {
        String sql = "SELECT id FROM user WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, UserValidationRules.normalizeEmail(email));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Cet email existe deja. Veuillez en choisir un autre.");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible de verifier l'unicite de l'email: " + e.getMessage(), e);
        }
    }

    private void ensureUniqueEmailForUpdate(int userId, String email) {
        String sql = "SELECT id FROM user WHERE email = ? AND id <> ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, UserValidationRules.normalizeEmail(email));
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Cet email existe deja. Veuillez en choisir un autre.");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible de verifier l'unicite de l'email: " + e.getMessage(), e);
        }
    }

    private static String sanitizeFullName(String fullName, String email) {
        String value = fullName == null ? "" : fullName.trim();
        if (value.length() >= 3) {
            return value;
        }
        String localPart = email == null ? "" : email.split("@")[0];
        localPart = localPart == null ? "" : localPart.trim();
        if (localPart.length() >= 3) {
            return localPart;
        }
        return "Google User";
    }

    private static String sanitizePseudo(String email) {
        String localPart = email == null ? "" : email.split("@")[0];
        String pseudo = localPart.replaceAll("[^A-Za-z0-9_]", "");
        if (pseudo.length() >= 3) {
            return pseudo;
        }
        if (pseudo.isBlank()) {
            pseudo = "usr";
        }
        while (pseudo.length() < 3) {
            pseudo += "x";
        }
        return pseudo;
    }
}
