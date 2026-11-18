package repository;

import domain.Utilizator;
import domain.validators.Validator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-based repository for {@link Utilizator} entities backed by a PostgreSQL database.
 */
public class UtilizatorDbRepository implements Repository<Long, Utilizator> {
    private final Validator<Utilizator> validator;
    private final String url;
    private final String user;
    private final String password;

    /**
     * Constructs a PostgreSQL repository for users.
     *
     * @param validator the validator used to validate user entities before persistence
     * @param url       the JDBC database URL
     * @param user      the database username
     * @param password  the database password
     */
    public UtilizatorDbRepository(Validator<Utilizator> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private RepositoryException wrapSqlException(String context, SQLException e) {
        String state = e.getSQLState();
        if (state != null && state.startsWith("08")) {
            return new RepositoryException("Nu se poate realiza conexiunea la baza de date PostgreSQL!", e);
        }
        return new RepositoryException(context + ": " + e.getMessage(), e);
    }

    /**
     * Finds a user by their database ID.
     *
     * @param id the ID of the user to find; must not be null
     * @return the found {@link Utilizator}, or null if no user has the given ID
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException      if a database access error occurs
     */
    @Override
    public Utilizator findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID-ul nu poate fi null!");
        }
        String sql = "SELECT id, first_name, last_name FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Utilizator u = new Utilizator(rs.getString("first_name"), rs.getString("last_name"));
                    u.setId(rs.getLong("id"));
                    return u;
                }
            }
            return null;
        } catch (SQLException e) {
            throw wrapSqlException("Eroare la cautarea utilizatorului in baza de date", e);
        }
    }

    /**
     * Retrieves all users from the database.
     *
     * @return an {@link Iterable} containing all stored users
     * @throws RepositoryException if a database access error occurs
     */
    @Override
    public Iterable<Utilizator> findAll() {
        List<Utilizator> users = new ArrayList<>();
        String sql = "SELECT id, first_name, last_name FROM users";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Utilizator u = new Utilizator(rs.getString("first_name"), rs.getString("last_name"));
                u.setId(rs.getLong("id"));
                users.add(u);
            }
            return users;
        } catch (SQLException e) {
            throw wrapSqlException("Eroare la incarcarea utilizatorilor din baza de date", e);
        }
    }

    /**
     * Saves a new user in the database and assigns the auto-generated ID to the entity.
     *
     * @param entity the user to save; must not be null
     * @return null if the user was saved successfully
     * @throws IllegalArgumentException             if entity is null
     * @throws domain.validators.ValidationException if the user is not valid
     * @throws RepositoryException                  if a database access error occurs
     */
    @Override
    public Utilizator save(Utilizator entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null!");
        }
        validator.validate(entity);

        String sql = "INSERT INTO users (first_name, last_name) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entity.getFirstName());
            ps.setString(2, entity.getLastName());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getLong(1));
                }
            }
            return null;
        } catch (SQLException e) {
            throw wrapSqlException("Eroare la salvarea utilizatorului in baza de date", e);
        }
    }

    /**
     * Deletes the user with the given ID from the database.
     *
     * @param id the ID of the user to delete; must not be null
     * @return the deleted {@link Utilizator}, or null if no user existed with the given ID
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException      if a database access error occurs
     */
    @Override
    public Utilizator delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID-ul nu poate fi null!");
        }
        Utilizator existing = findOne(id);
        if (existing == null) {
            return null;
        }

        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            return existing;
        } catch (SQLException e) {
            throw wrapSqlException("Eroare la stergerea utilizatorului din baza de date", e);
        }
    }

    /**
     * Updates an existing user in the database.
     *
     * @param entity the user with updated fields; must not be null
     * @return null if the user was updated, or the entity if no user with its ID exists
     * @throws IllegalArgumentException             if entity is null or has a null ID
     * @throws domain.validators.ValidationException if the user is not valid
     * @throws RepositoryException                  if a database access error occurs
     */
    @Override
    public Utilizator update(Utilizator entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("Entitatea si ID-ul nu pot fi null!");
        }
        validator.validate(entity);

        String sql = "UPDATE users SET first_name = ?, last_name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getFirstName());
            ps.setString(2, entity.getLastName());
            ps.setLong(3, entity.getId());
            int affectedRows = ps.executeUpdate();
            return affectedRows == 0 ? entity : null;
        } catch (SQLException e) {
            throw wrapSqlException("Eroare la actualizarea utilizatorului in baza de date", e);
        }
    }
}