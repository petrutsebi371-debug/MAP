package repository;

import domain.Prietenie;
import domain.validators.Validator;

import java.time.LocalDateTime;

public class PrietenieFileRepository extends AbstractFileRepository<Long, Prietenie> {
    private Long idCounter = 1L;

    public PrietenieFileRepository(Validator<Prietenie> validator, String fileName) {
        super(validator, fileName);
    }

    @Override
    protected Prietenie extractEntity(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Linia nu contine date valide pentru Prietenie: " + line);
        }

        Long idUser1 = Long.parseLong(parts[0]);
        Long idUser2 = Long.parseLong(parts[1]);
        LocalDateTime date = LocalDateTime.parse(parts[2]);

        Prietenie prietenie = new Prietenie(idUser1, idUser2);
        prietenie.setId(idCounter++); // Atribuim un ID provizoriu pentru memorie
        prietenie.setDate(date);

        return prietenie;
    }

    @Override
    protected String createEntityAsString(Prietenie entity) {
        return entity.getIdUser1() + ";" + entity.getIdUser2() + ";" + entity.getDate().toString();
    }
}