package ui;

import domain.CriteriiUtilizator;
import domain.Utilizator;
import domain.validators.ValidationException;
import repository.RepositoryException;
import service.ComunitatiService;
import service.PrietenieService;
import service.ServiceException;
import service.UtilizatorService;

import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Console-based user interface refactored to use Java 8 functional constructs and database feedback.
 */
public class ConsoleUI {
    private final UtilizatorService userService;
    private final PrietenieService friendshipService;
    private final ComunitatiService communityService;
    private final Scanner scanner;

    /**
     * Constructs the console UI with the required specialized services.
     *
     * @param userService       the service managing users
     * @param friendshipService the service managing friendships
     * @param communityService  the service analyzing network communities
     */
    public ConsoleUI(UtilizatorService userService, PrietenieService friendshipService, ComunitatiService communityService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.communityService = communityService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the interactive console menu loop.
     */
    public void start() {
        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Alege o optiune: ");
            String option = scanner.nextLine().trim();

            try {
                switch (option) {
                    case "1":
                        addUser();
                        break;
                    case "2":
                        removeUser();
                        break;
                    case "3":
                        addFriendship();
                        break;
                    case "4":
                        removeFriendship();
                        break;
                    case "5":
                        showUsers();
                        break;
                    case "6":
                        showNumberOfCommunities();
                        break;
                    case "7":
                        showMostSociableCommunity();
                        break;
                    case "8":
                        filterUsersMenu();
                        break;
                    case "0":
                        System.out.println("La revedere!");
                        running = false;
                        break;
                    default:
                        System.out.println("Optiune invalida!");
                }
            } catch (ValidationException | ServiceException | RepositoryException e) {
                System.out.println("Eroare: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Eroare: ID-ul trebuie sa fie un numar valid!");
            }
            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("=== RETEA DE SOCIALIZARE ===");
        System.out.println("1. Adauga Utilizator");
        System.out.println("2. Sterge Utilizator");
        System.out.println("3. Adauga Prieten");
        System.out.println("4. Sterge Prieten");
        System.out.println("5. Afiseaza toti utilizatorii");
        System.out.println("6. Numarul de comunitati (componente conexe)");
        System.out.println("7. Cea mai sociabila comunitate");
        System.out.println("8. Filtreaza utilizatori (Predicate / Lambda)");
        System.out.println("0. Iesire");
    }

    private void addUser() {
        System.out.print("Prenume: ");
        String firstName = scanner.nextLine();
        System.out.print("Nume: ");
        String lastName = scanner.nextLine();
        Utilizator added = userService.addUser(firstName, lastName);
        System.out.println("Utilizator adaugat cu succes! ID generat: " + added.getId());
    }

    private void removeUser() {
        System.out.print("ID utilizator de sters: ");
        Long id = Long.parseLong(scanner.nextLine().trim());
        friendshipService.removeAllFriendshipsOfUser(id);
        userService.removeUser(id);
        System.out.println("Utilizator si prieteniile sale au fost sterse cu succes!");
    }

    private void addFriendship() {
        System.out.print("ID primul utilizator: ");
        Long id1 = Long.parseLong(scanner.nextLine().trim());
        System.out.print("ID al doilea utilizator: ");
        Long id2 = Long.parseLong(scanner.nextLine().trim());
        friendshipService.addFriendship(id1, id2);
        System.out.println("Prietenie adaugata cu succes!");
    }

    private void removeFriendship() {
        System.out.print("ID primul utilizator: ");
        Long id1 = Long.parseLong(scanner.nextLine().trim());
        System.out.print("ID al doilea utilizator: ");
        Long id2 = Long.parseLong(scanner.nextLine().trim());
        friendshipService.removeFriendship(id1, id2);
        System.out.println("Prietenie stearsa cu succes!");
    }

    private void showUsers() {
        System.out.println("--- Lista Utilizatori (sortati alfabetic) ---");
        List<Utilizator> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Nu exista utilizatori in retea.");
        } else {
            users.forEach(System.out::println);
        }
    }

    private void showNumberOfCommunities() {
        int count = communityService.getNumberOfCommunities();
        System.out.println("Numarul de comunitati din retea este: " + count);
    }

    private void showMostSociableCommunity() {
        List<Utilizator> community = communityService.getMostSociableCommunity();
        if (community.isEmpty()) {
            System.out.println("Reteaua este goala.");
        } else {
            System.out.println("Cea mai sociabila comunitate este formata din:");
            community.forEach(System.out::println);
        }
    }

    private void filterUsersMenu() {
        System.out.println("--- Filtrare Utilizatori ---");
        System.out.println("1. Utilizatori fara prieteni");
        System.out.println("2. Utilizatori cu cel putin un prieten si nume care incepe cu un prefix");
        System.out.print("Alege filtrul: ");
        String choice = scanner.nextLine().trim();

        Predicate<Utilizator> criteria;
        if ("1".equals(choice)) {
            criteria = CriteriiUtilizator::faraPrieteni;
        } else if ("2".equals(choice)) {
            System.out.print("Introdu prefixul numelui de familie: ");
            String prefix = scanner.nextLine().trim();
            criteria = Predicate.not(CriteriiUtilizator::faraPrieteni)
                    .and(CriteriiUtilizator.numeIncepeCu(prefix));
        } else {
            System.out.println("Filtru invalid!");
            return;
        }

        List<Utilizator> filtered = userService.findUsersByCriteria(criteria);
        if (filtered.isEmpty()) {
            System.out.println("Niciun utilizator nu corespunde criteriului.");
        } else {
            filtered.forEach(System.out::println);
        }
    }
}