package domain;

import java.util.function.Predicate;

/**
 * Utility class providing static search criteria (predicates) for {@link Utilizator}.
 */
public class CriteriiUtilizator {

    /**
     * Checks if a user has no friends.
     *
     * @param u the user to check
     * @return true if the user's friend list is empty, false otherwise
     */
    public static boolean faraPrieteni(Utilizator u) {
        return u.getFriends().isEmpty();
    }

    /**
     * Returns a predicate matching users who have at least one friend.
     *
     * @return a predicate for users with friends
     */
    public static Predicate<Utilizator> arePrieteni() {
        return Predicate.not(CriteriiUtilizator::faraPrieteni);
    }

    /**
     * Returns a predicate matching users whose last name starts with the given prefix (case-insensitive).
     *
     * @param prefix the prefix to match
     * @return a predicate filtering by last name prefix
     */
    public static Predicate<Utilizator> numeIncepeCu(String prefix) {
        return u -> u.getLastName().toLowerCase().startsWith(prefix.toLowerCase());
    }

    /**
     * Returns a predicate matching users whose first name contains the given substring (case-insensitive).
     *
     * @param text the substring to search for
     * @return a predicate filtering by first name content
     */
    public static Predicate<Utilizator> prenumeContine(String text) {
        return u -> u.getFirstName().toLowerCase().contains(text.toLowerCase());
    }
}