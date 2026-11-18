package repository;

import domain.Utilizator;
import domain.validators.Validator;

public class UtilizatorFileRepository extends AbstractFileRepository<Long, Utilizator> {

    public UtilizatorFileRepository(Validator<Utilizator> validator, String fileName) {
        super(validator, fileName);
    }

    @Override
    protected Utilizator extractEntity(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Linia nu contine date valide pentru Utilizator: " + line);
        }

        Long id = Long.parseLong(parts[0]);
        String firstName = parts[1];
        String lastName = parts[2];

        Utilizator user = new Utilizator(firstName, lastName);
        user.setId(id);
        return user;
    }

    @Override
    protected String createEntityAsString(Utilizator entity) {
        return entity.getId() + ";" + entity.getFirstName() + ";" + entity.getLastName();
    }
}