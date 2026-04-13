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

public class UserService implements IService<User> {

    private final Connection connection;

    public UserService() {
        this.connection = MyConnection.getInstance().getCnx();
    }

    @Override
    public void add(User user) {
        validate(user, false);
        ensureUniqueEmailForCreate(user.getEmail());
        String sql = "INSERT INTO users(full_name, pseudo, email, password, role) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPseudo().trim());
            ps.setString(3, UserValidationRules.normalizeEmail(user.getEmail()));
            ps.setString(4, PasswordUtils.hash(user.getPassword()));
            ps.setString(5, user.getRole());
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
        String sqlWithPassword = "UPDATE users SET full_name = ?, pseudo = ?, email = ?, password = ?, role = ? WHERE id = ?";
        String sqlWithoutPassword = "UPDATE users SET full_name = ?, pseudo = ?, email = ?, role = ? WHERE id = ?";
        boolean updatePassword = user.getPassword() != null && !user.getPassword().isBlank();

        try (PreparedStatement ps = connection.prepareStatement(updatePassword ? sqlWithPassword : sqlWithoutPassword)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getPseudo().trim());
            ps.setString(3, UserValidationRules.normalizeEmail(user.getEmail()));
            if (updatePassword) {
                ps.setString(4, PasswordUtils.hash(user.getPassword()));
                ps.setString(5, user.getRole());
                ps.setInt(6, user.getId());
            } else {
                ps.setString(4, user.getRole());
                ps.setInt(5, user.getId());
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
        String sql = "DELETE FROM users WHERE id = ?";
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
        String sql = "SELECT id, full_name, pseudo, email, password, role FROM users ORDER BY id";

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
        String sql = "SELECT id, full_name, pseudo, email, password, role FROM users WHERE email = ?";

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

    private User mapResultSet(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("pseudo"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("role")
        );
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
        String sql = "SELECT id FROM users WHERE email = ?";
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
        String sql = "SELECT id FROM users WHERE email = ? AND id <> ?";
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
}
