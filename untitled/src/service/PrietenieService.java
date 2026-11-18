package service;

import domain.Entity;
import domain.Prietenie;
import domain.Utilizator;
import repository.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Service managing friendship operations using Java 8 Streams and Optional.
 */
public class PrietenieService {
    private final Repository<Long, Utilizator> userRepository;
    private final Repository<Long, Prietenie> friendshipRepository;
    private Long friendshipIdCounter;

    /**
     * Initializes the friendship service, synchronizes the ID counter, and rebuilds in-memory friend lists.
     *
     * @param userRepository       the repository managing users
     * @param friendshipRepository the repository managing friendships
     */
    public PrietenieService(Repository<Long, Utilizator> userRepository, Repository<Long, Prietenie> friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;

        this.friendshipIdCounter = friendshipStream()
                .map(Entity::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .map(maxId -> maxId + 1)
                .orElse(1L);

        friendshipStream().forEach(p -> {
            Optional<Utilizator> u1 = Optional.ofNullable(userRepository.findOne(p.getIdUser1()));
            Optional<Utilizator> u2 = Optional.ofNullable(userRepository.findOne(p.getIdUser2()));
            if (u1.isPresent() && u2.isPresent()) {
                Utilizator user1 = u1.get();
                Utilizator user2 = u2.get();
                if (!user1.getFriends().contains(user2)) {
                    user1.addFriend(user2);
                }
                if (!user2.getFriends().contains(user1)) {
                    user2.addFriend(user1);
                }
            }
        });
    }

    private Stream<Prietenie> friendshipStream() {
        return StreamSupport.stream(friendshipRepository.findAll().spliterator(), false);
    }

    /**
     * Creates a friendship between two existing users.
     *
     * @param id1 the ID of the first user
     * @param id2 the ID of the second user
     * @throws ServiceException if either user does not exist or if the friendship already exists
     */
    public void addFriendship(Long id1, Long id2) {
        Utilizator u1 = Optional.ofNullable(userRepository.findOne(id1))
                .orElseThrow(() -> new ServiceException("Utilizatorul cu ID " + id1 + " nu exista!"));
        Utilizator u2 = Optional.ofNullable(userRepository.findOne(id2))
                .orElseThrow(() -> new ServiceException("Utilizatorul cu ID " + id2 + " nu exista!"));

        Prietenie p = new Prietenie(id1, id2);
        p.setId(friendshipIdCounter++);

        boolean exists = friendshipStream()
                .anyMatch(existing -> existing.getIdUser1().equals(p.getIdUser1())
                        && existing.getIdUser2().equals(p.getIdUser2()));

        if (exists) {
            throw new ServiceException("Prietenia exista deja!");
        }

        friendshipRepository.save(p);
        u1.addFriend(u2);
        u2.addFriend(u1);
    }

    /**
     * Removes an existing friendship between two users.
     *
     * @param id1 the ID of the first user
     * @param id2 the ID of the second user
     * @throws ServiceException if the friendship does not exist
     */
    public void removeFriendship(Long id1, Long id2) {
        Long minId = Math.min(id1, id2);
        Long maxId = Math.max(id1, id2);

        Prietenie prietenie = friendshipStream()
                .filter(p -> p.getIdUser1().equals(minId) && p.getIdUser2().equals(maxId))
                .findFirst()
                .orElseThrow(() -> new ServiceException("Prietenia nu exista!"));

        friendshipRepository.delete(prietenie.getId());

        Optional<Utilizator> u1 = Optional.ofNullable(userRepository.findOne(id1));
        Optional<Utilizator> u2 = Optional.ofNullable(userRepository.findOne(id2));
        if (u1.isPresent() && u2.isPresent()) {
            u1.get().removeFriend(u2.get());
            u2.get().removeFriend(u1.get());
        }
    }

    /**
     * Removes all friendships associated with a given user ID.
     *
     * @param userId the ID of the user whose friendships should be removed
     */
    public void removeAllFriendshipsOfUser(Long userId) {
        Optional<Utilizator> userOpt = Optional.ofNullable(userRepository.findOne(userId));

        List<Prietenie> friendshipsToRemove = friendshipStream()
                .filter(p -> p.getIdUser1().equals(userId) || p.getIdUser2().equals(userId))
                .collect(Collectors.toList());

        friendshipsToRemove.forEach(p -> {
            Long otherId = p.getIdUser1().equals(userId) ? p.getIdUser2() : p.getIdUser1();
            Optional.ofNullable(userRepository.findOne(otherId))
                    .ifPresent(otherUser -> userOpt.ifPresent(otherUser::removeFriend));
            friendshipRepository.delete(p.getId());
        });
    }
}