package repository;

import domain.Prietenie;
import domain.Utilizator;
import domain.validators.Validator;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

/**
 * Simple Factory for creating repository instances based on external configuration,
 * using functional mappings instead of switch statements.
 */
public class RepositoryFactory {
    private final Properties properties;
    private final Map<RepositoryType, Function<Validator<Utilizator>, Repository<Long, Utilizator>>> userFactories;
    private final Map<RepositoryType, Function<Validator<Prietenie>, Repository<Long, Prietenie>>> friendshipFactories;

    /**
     * Initializes the factory by loading {@code config.properties} and registering repository creators.
     *
     * @throws RepositoryException if the configuration file cannot be found or read
     */
    public RepositoryFactory() {
        properties = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                throw new RepositoryException("Nu s-a gasit fisierul de configurare config.properties!");
            }
            properties.load(is);
        } catch (RepositoryException e) {
            throw e;
        } catch (Exception e) {
            throw new RepositoryException("Eroare la incarcarea fisierului de configurare!", e);
        }

        userFactories = new EnumMap<>(RepositoryType.class);
        userFactories.put(RepositoryType.MEMORY, InMemoryRepository::new);
        userFactories.put(RepositoryType.FILE, validator -> {
            String file = getRequiredProperty("users.file");
            return new UtilizatorFileRepository(validator, file);
        });
        userFactories.put(RepositoryType.DATABASE, validator -> {
            String url = getRequiredProperty("db.url");
            String user = getRequiredProperty("db.user");
            String pass = getRequiredProperty("db.password");
            return new UtilizatorDbRepository(validator, url, user, pass);
        });

        friendshipFactories = new EnumMap<>(RepositoryType.class);
        friendshipFactories.put(RepositoryType.MEMORY, InMemoryRepository::new);
        friendshipFactories.put(RepositoryType.FILE, validator -> {
            String file = getRequiredProperty("friendships.file");
            return new PrietenieFileRepository(validator, file);
        });
        friendshipFactories.put(RepositoryType.DATABASE, validator -> {
            String url = getRequiredProperty("db.url");
            String user = getRequiredProperty("db.user");
            String pass = getRequiredProperty("db.password");
            return new PrietenieDbRepository(validator, url, user, pass);
        });
    }

    private String getRequiredProperty(String key) {
        return Optional.ofNullable(properties.getProperty(key))
                .orElseThrow(() -> new RepositoryException("Proprietatea " + key + " lipseste din config.properties!"));
    }

    private RepositoryType getRepositoryType() {
        String repoTypeStr = getRequiredProperty("repository.type");
        try {
            return RepositoryType.valueOf(repoTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RepositoryException("Tip de repository necunoscut: " + repoTypeStr, e);
        }
    }

    /**
     * Creates a {@link Utilizator} repository according to the configured persistence type.
     *
     * @param validator the validator strategy for users
     * @return the configured repository instance
     * @throws RepositoryException if configuration values are missing or invalid
     */
    public Repository<Long, Utilizator> createUtilizatorRepository(Validator<Utilizator> validator) {
        RepositoryType type = getRepositoryType();
        return Optional.ofNullable(userFactories.get(type))
                .orElseThrow(() -> new RepositoryException("Tip de repository nesuportat: " + type))
                .apply(validator);
    }

    /**
     * Creates a {@link Prietenie} repository according to the configured persistence type.
     *
     * @param validator the validator strategy for friendships
     * @return the configured repository instance
     * @throws RepositoryException if configuration values are missing or invalid
     */
    public Repository<Long, Prietenie> createPrietenieRepository(Validator<Prietenie> validator) {
        RepositoryType type = getRepositoryType();
        return Optional.ofNullable(friendshipFactories.get(type))
                .orElseThrow(() -> new RepositoryException("Tip de repository nesuportat: " + type))
                .apply(validator);
    }
}