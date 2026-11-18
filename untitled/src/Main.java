import domain.Prietenie;
import domain.Utilizator;
import domain.validators.PrietenieValidator;
import domain.validators.UtilizatorValidator;
import domain.validators.ValidationException;
import domain.validators.Validator;
import repository.Repository;
import repository.RepositoryException;
import repository.RepositoryFactory;
import service.ComunitatiService;
import service.PrietenieService;
import service.UtilizatorService;
import ui.ConsoleUI;

/**
 * Application entry point wiring validators, repositories, services, and UI.
 */
public class Main {
    public static void main(String[] args) {
        try {
            Validator<Utilizator> userValidator = new UtilizatorValidator()
                    .andThen(user -> {
                        if (user.getFirstName().trim().length() < 2 || user.getLastName().trim().length() < 2) {
                            throw new ValidationException("Numele si prenumele trebuie sa contina cel putin 2 caractere!");
                        }
                    });
            Validator<Prietenie> friendshipValidator = new PrietenieValidator();

            RepositoryFactory factory = new RepositoryFactory();
            Repository<Long, Utilizator> userRepo = factory.createUtilizatorRepository(userValidator);
            Repository<Long, Prietenie> friendshipRepo = factory.createPrietenieRepository(friendshipValidator);

            UtilizatorService userService = new UtilizatorService(userRepo);
            PrietenieService friendshipService = new PrietenieService(userRepo, friendshipRepo);
            ComunitatiService communityService = new ComunitatiService(userRepo);

            ConsoleUI ui = new ConsoleUI(userService, friendshipService, communityService);
            ui.start();
        } catch (RepositoryException e) {
            System.out.println("Eroare la conectarea cu mediul de stocare: " + e.getMessage());
        }
    }
}