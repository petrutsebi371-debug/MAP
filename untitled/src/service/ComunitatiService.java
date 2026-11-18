package service;

import domain.Utilizator;
import repository.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Service responsible for network community analysis using Java 8 Streams.
 */
public class ComunitatiService {
    private final Repository<Long, Utilizator> userRepository;

    /**
     * Initializes the community analysis service.
     *
     * @param userRepository the repository containing network users
     */
    public ComunitatiService(Repository<Long, Utilizator> userRepository) {
        this.userRepository = userRepository;
    }

    private Stream<Utilizator> userStream() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false);
    }

    private void dfs(Utilizator u, Set<Long> visited, List<Utilizator> community) {
        visited.add(u.getId());
        community.add(u);
        u.getFriends().stream()
                .filter(friend -> !visited.contains(friend.getId()))
                .forEach(friend -> dfs(friend, visited, community));
    }

    private List<List<Utilizator>> getAllCommunities() {
        Set<Long> visited = new HashSet<>();
        return userStream()
                .filter(u -> !visited.contains(u.getId()))
                .map(u -> {
                    List<Utilizator> community = new ArrayList<>();
                    dfs(u, visited, community);
                    return community;
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculates the total number of connected communities in the social network.
     *
     * @return the number of communities
     */
    public int getNumberOfCommunities() {
        return getAllCommunities().size();
    }

    /**
     * Finds the most sociable community (the connected component with the largest number of members).
     *
     * @return a list of users belonging to the largest community, or an empty list if the network is empty
     */
    public List<Utilizator> getMostSociableCommunity() {
        return getAllCommunities().stream()
                .max(Comparator.comparingInt(List::size))
                .orElseGet(ArrayList::new);
    }
}