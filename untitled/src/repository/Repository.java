package repository;

import domain.Entity;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * CRUD operations repository interface.
 *
 * @param <ID> type E must have an attribute of type ID
 * @param <E>  type of entities saved in repository
 */
public interface Repository<ID, E extends Entity<ID>> {

    /**
     * Finds the entity with the given id.
     *
     * @param id the id of the entity to be returned; must not be null
     * @return the entity with the specified id, or null if there is no entity with the given id
     * @throws IllegalArgumentException if id is null
     */
    E findOne(ID id);

    /**
     * Returns all entities.
     *
     * @return an Iterable containing all entities
     */
    Iterable<E> findAll();

    /**
     * Returns all entities that match the given predicate filter.
     *
     * @param filter the predicate used to filter entities; must not be null
     * @return a list of entities matching the filter
     * @throws NullPointerException if filter is null
     */
    default List<E> findAll(Predicate<E> filter) {
        Objects.requireNonNull(filter);
        return StreamSupport.stream(findAll().spliterator(), false)
                .filter(filter)
                .collect(Collectors.toList());
    }

    /**
     * Saves the given entity.
     *
     * @param entity the entity to be saved; must not be null
     * @return null if the entity was saved successfully, or the entity (if it already exists)
     * @throws IllegalArgumentException             if the given entity is null
     * @throws domain.validators.ValidationException if the entity is not valid
     */
    E save(E entity);

    /**
     * Removes the entity with the given id.
     *
     * @param id the id of the entity to be removed; must not be null
     * @return the removed entity, or null if there is no entity with the given id
     * @throws IllegalArgumentException if the given id is null
     */
    E delete(ID id);

    /**
     * Updates the given entity.
     *
     * @param entity the entity to be updated; must not be null
     * @return null if the entity was updated successfully, or the entity (if it does not exist)
     * @throws IllegalArgumentException             if the given entity is null
     * @throws domain.validators.ValidationException if the entity is not valid
     */
    E update(E entity);
}