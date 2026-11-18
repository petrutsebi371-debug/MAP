package repository;

import domain.Prietenie;
import domain.validators.Validator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-based repository for {@link Prietenie} entities backed by a PostgreSQL database.
 */
public class PrietenieDbRepository implements Repository<Long, Prietenie> {
    private final Validator<Prietenie> validator;
    private final String url;
    private final String user;
    private final String password;

    /**
     * Constructs a PostgreSQL repository for friendships.
     *
     * @param validator the validator used to validate friendship entities before persistence
     * @param url       the JDBC database URL
     * @param user      the database username
     * @param password  the database password
     */
    public PrietenieDbRepository(Validator<Prietenie> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private Long encodeId(Long id1, Long id2) {
        long min = Math.min(id1, id2);
        long max = Math.max(id1, id2);
        return (min << 32) | (max & 0xFFFFFFFFL);
    }

    private long decodeId1(Long encodedId) {
        return encodedId >>> 32;
    }

    private long decodeId2(Long encodedId) {
        return encodedId & 0xFFFFFFFFL;
    }

    private RepositoryException wrapSqlException(String context, SQLException e) {
        String state = e.getSQLState();
        if (state != null && state.startsWith("08")) {
            return new RepositoryException("Nu se poate realiza conexiunea la baza de date PostgreSQL!", e);
        }
        if ("23505".equals(state)) {
            return new RepositoryException("Prietenia exista deja in baza de date!", e);
        }
        if ("23503".equals(state)) {
            return new RepositoryException("Unul dintre utilizatori nu exista in baza de date!", e);
        }
        return new RepositoryException(context + ": " + e.getMessage(), e);
    }

    /**
     * Finds a friendship by its composite encoded ID.
     *
     * @param id the encoded ID of the friendship; must not be null
     * @return the found {@link Prietenie}, or null if not found
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException      if a database access error occurs
     */
    @Override
    public Prietenie findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID-ul nu poate fi null!");
        }
        long id1 = decodeId1(id);
        long id2 = decodeId2(id);

        String sql = "SELECT id1, id2, friends_from FROM friendships WHERE id1 = ? AND id2 = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id1);
            ps.setLong(2, id2);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Prietenie p = new Prietenie(rs.getLong("id1"), rs.getLong("id2"));
                    p.setId(encodeId(p.getIdUser1(), p.getIdUser2()));
                    return p;
                }
            }
            return null;
        } catch (SQLException e) {
            throw wrapSqlException("Eroare la cautarea prieteniei in baza de date", e);
        }
    }

    /**
     * Retrieves all friendships from the database.
     *
     * @return an {@link Iterable} containing all stored friendships
     * @throws RepositoryException if a database access error occurs
     */
    @Override
    public Iterable<Prietenie> findAll() {
        List<Prietenie> friendships = new ArrayList<>();
        String sql = "SELECT id1, id2, friends_from FROM friendships";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Prietenie p = new Prietenie(rs.getLong("id1"), rs.getLong("id2"));
                p.setId(encodeId(p.getIdUser1(), p.getIdUser2()));
                friendships.add(p);
            }
            return friendships;
        } catch (SQLException e) {
            throw wrapSqlException("Eroare la incarcarea prieteniilor din baza de date", e);
        }
    }

    /**
     * Saves a new friendship in the database.
     *
     * @param entity the friendship to save; must not be null
     * @return null if the friendship was saved successfully, or the entity if it already exists
     * @throws IllegalArgumentException             if entity is null
     * @throws domain.validators.ValidationException if the friendship is not valid
     * @throws RepositoryException                  if a database access error occurs
     */
    @Override
    public Prietenie save(Prietenie entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null!");
        }
        validator.validate(entity);

        long id1 = Math.min(entity.getIdUser1(), entity.getIdUser2());
        long id2 = Math.max(entity.getIdUser1(), entity.getIdUser2());
        Long encodedId = encodeId(id1, id2);

        if (findOne(encodedId) != null) {
            return entity;
        }

        String sql = "INSERT INTO friendships (id1, id2, friends_from) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id1);
            ps.setLong(2, id2);
            LocalDateTime date = entity.getDate() != null ? entity.getDate() : LocalDateTime.now();
            ps.setTimestamp(3, Timestamp.valueOf(date));
            ps.executeUpdate();
            entity.setId(encodedId);
            return null;
        } catch (SQLException e) {
            throw wrapSqlException("Eroare la salvarea prieteniei in baza de date", e);
        }
    }

    /**
     * Deletes a friendship from the database by its encoded ID.
     *
     * @param id the encoded ID of the friendship to delete; must not be null
     * @return the deleted {@link Prietenie}, or null if it did not exist
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException      if a database access error occurs
     */
    @Override
    public Prietenie delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID-ul nu poate fi null!");
        }
        Prietenie existing = findOne(id);
        if (existing == null) {
            return null;
        }

        String sql = "DELETE FROM friendships WHERE id1 = ? AND id2 = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, existing.getIdUser1());
            ps.setLong(2, existing.getIdUser2());
            ps.executeUpdate();
            return existing;
        } catch (SQLException e) {
            throw wrapSqlException("Eroare la stergerea prieteniei din baza de date", e);
        }
    }

    /**
     * Updates the timestamp of an existing friendship in the database.
     *
     * @param entity the friendship to update; must not be null
     * @return null if updated successfully, or the entity if the friendship does not exist
     * @throws IllegalArgumentException             if entity is null
     * @throws domain.validators.ValidationException if the friendship is not valid
     * @throws RepositoryException                  if a database access error occurs
     */
    @Override
    public Prietenie update(Prietenie entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null!");
        }
        validator.validate(entity);

        long id1 = Math.min(entity.getIdUser1(), entity.getIdUser2());
        long id2 = Math.max(entity.getIdUser1(), entity.getIdUser2());

        String sql = "UPDATE friendships SET friends_from = ? WHERE id1 = ? AND id2 = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            LocalDateTime date = entity.getDate() != null ? entity.getDate() : LocalDateTime.now();
            ps.setTimestamp(1, Timestamp.valueOf(date));
            ps.setLong(2, id1);
            ps.setLong(3, id2);
            int affectedRows = ps.executeUpdate();
            return affectedRows == 0 ? entity : null;
        } catch (SQLException e) {
            throw wrapSqlException("Eroare la actualizarea prieteniei in baza de date", e);
        }
    }
}