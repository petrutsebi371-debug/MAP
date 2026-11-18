package service;

import domain.Utilizator;
import repository.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Service managing user operations using Java 8 Streams and Optional.
 */
public class UtilizatorService {
    private final Repository<Long, Utilizator> userRepository;

    /**
     * Initializes the user service.
     *
     * @param userRepository the repository managing user entities
     */
    public UtilizatorService(Repository<Long, Utilizator> userRepository) {
        this.userRepository = userRepository;
    }

    private Stream<Utilizator> userStream() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false);
    }

    /**
     * Adds a new user to the network; the storage layer generates and assigns the user's ID.
     *
     * @param firstName the user's first name
     * @param lastName  the user's last name
     * @return the newly created user with its generated ID
     * @throws ServiceException if a user with the generated ID already exists
     */
    public Utilizator addUser(String firstName, String lastName) {
        Utilizator user = new Utilizator(firstName, lastName);
        Optional.ofNullable(userRepository.save(user))
                .ifPresent(existing -> {
                    throw new ServiceException("Utilizatorul exista deja!");
                });
        return user;
    }

    /**
     * Removes a user by ID.
     *
     * @param id the ID of the user to remove
     * @return the removed user
     * @throws ServiceException if the user does not exist
     */
    public Utilizator removeUser(Long id) {
        Utilizator user = findOne(id)
                .orElseThrow(() -> new ServiceException("Utilizatorul nu exista!"));
        return userRepository.delete(user.getId());
    }

    /**
     * Finds a user by ID.
     *
     * @param id the ID of the user to find
     * @return an {@link Optional} containing the user if found, or empty otherwise
     */
    public Optional<Utilizator> findOne(Long id) {
        return Optional.ofNullable(userRepository.findOne(id));
    }

    /**
     * Returns all users sorted alphabetically by last name and then by first name.
     *
     * @return a sorted list of all users
     */
    public List<Utilizator> getAllUsers() {
        return userStream()
                .sorted(Comparator.comparing(Utilizator::getLastName)
                        .thenComparing(Utilizator::getFirstName))
                .collect(Collectors.toList());
    }

    /**
     * Finds all users matching the given search predicate.
     *
     * @param filter the predicate used to filter users
     * @return a list of matching users
     */
    public List<Utilizator> findUsersByCriteria(Predicate<Utilizator> filter) {
        return userRepository.findAll(filter);
    }
}