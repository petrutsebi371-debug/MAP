package domain.validators;

import java.util.Objects;

/**
 * Functional interface for validating entities.
 *
 * @param <T> the type of entity to validate
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Validates the given entity.
     *
     * @param entity the entity to be validated
     * @throws ValidationException if the entity is not valid
     */
    void validate(T entity) throws ValidationException;

    /**
     * Returns a composed validator that performs, in sequence, this validation
     * followed by the {@code other} validation.
     *
     * @param other the validator to apply after this validator
     * @return a composed validator
     * @throws NullPointerException if {@code other} is null
     */
    default Validator<T> andThen(Validator<T> other) {
        Objects.requireNonNull(other);
        return entity -> {
            this.validate(entity);
            other.validate(entity);
        };
    }
}