package domain.validators;

import domain.Prietenie;

public class PrietenieValidator implements Validator<Prietenie> {
    @Override
    public void validate(Prietenie entity) throws ValidationException {
        if (entity.getIdUser1().equals(entity.getIdUser2())) {
            throw new ValidationException("Id-urile utilizatorilor trebuie sa fie diferite!");
        }
    }
}