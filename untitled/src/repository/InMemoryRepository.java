package repository;

import domain.Entity;
import domain.validators.Validator;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory CRUD repository implementation backed by a {@link HashMap}.
 *
 * @param <ID> the type of the entity identifier
 * @param <E>  the type of entities stored in the repository
 */
public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    private final Validator<E> validator;
    private final Map<ID, E> entities;

    /**
     * Constructs an empty in-memory repository with the given validator.
     *
     * @param validator the validator strategy used to validate entities
     */
    public InMemoryRepository(Validator<E> validator) {
        this.validator = validator;
        this.entities = new HashMap<>();
    }

    @Override
    public E findOne(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID-ul nu poate fi null!");
        }
        return entities.get(id);
    }

    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }

    @Override
    @SuppressWarnings("unchecked")
    public E save(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null!");
        }
        validator.validate(entity);

        if (entity.getId() == null) {
            long nextId = entities.keySet().stream()
                    .filter(Long.class::isInstance)
                    .mapToLong(id -> (Long) id)
                    .max()
                    .orElse(0L) + 1;
            entity.setId((ID) Long.valueOf(nextId));
        }

        if (entities.get(entity.getId()) != null) {
            return entity;
        }
        entities.put(entity.getId(), entity);
        return null;
    }

    @Override
    public E delete(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID-ul nu poate fi null!");
        }
        return entities.remove(id);
    }

    @Override
    public E update(E entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entitatea nu poate fi null!");
        }
        validator.validate(entity);

        if (entities.get(entity.getId()) != null) {
            entities.put(entity.getId(), entity);
            return null;
        }
        return entity;
    }
}