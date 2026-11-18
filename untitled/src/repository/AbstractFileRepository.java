package repository;

import domain.Entity;
import domain.validators.Validator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Abstract file-based repository implementing the Template Method pattern
 * using Java 8 Streams and NIO.2.
 *
 * @param <ID> the type of the entity identifier
 * @param <E>  the type of entities managed by this repository
 */
public abstract class AbstractFileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E> {
    private final String fileName;

    /**
     * Constructs a file repository and loads existing entities from the specified file.
     *
     * @param validator the validator used to validate entities
     * @param fileName  the relative path to the storage file
     */
    public AbstractFileRepository(Validator<E> validator, String fileName) {
        super(validator);
        this.fileName = fileName;
        loadData();
    }

    /**
     * Converts a line of text from the file into an entity.
     *
     * @param line the raw text line
     * @return the extracted entity
     */
    protected abstract E extractEntity(String line);

    /**
     * Converts an entity into its string representation for file storage.
     *
     * @param entity the entity to serialize
     * @return the formatted string line
     */
    protected abstract String createEntityAsString(E entity);

    private void loadData() {
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            return;
        }

        List<String> lines;
        try (Stream<String> lineStream = Files.lines(path)) {
            lines = lineStream.collect(Collectors.toList());
        } catch (IOException e) {
            throw new RepositoryException("Eroare la citirea din fisier: " + fileName, e);
        }

        IntStream.range(0, lines.size()).forEach(i -> {
            String line = lines.get(i);
            if (!line.trim().isEmpty()) {
                try {
                    E entity = extractEntity(line);
                    super.save(entity);
                } catch (Exception e) {
                    System.err.println("Linie corupta ignorata in fisierul " + fileName + " la linia " + (i + 1) + ": " + e.getMessage());
                }
            }
        });
    }

    private void writeToFile() {
        Path path = Paths.get(fileName);
        List<String> lines = StreamSupport.stream(super.findAll().spliterator(), false)
                .map(this::createEntityAsString)
                .collect(Collectors.toList());

        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, lines);
        } catch (IOException e) {
            throw new RepositoryException("Eroare la scrierea in fisier: " + fileName, e);
        }
    }

    @Override
    public E save(E entity) {
        E result = super.save(entity);
        if (result == null) {
            writeToFile();
        }
        return result;
    }

    @Override
    public E delete(ID id) {
        E entity = super.delete(id);
        if (entity != null) {
            writeToFile();
        }
        return entity;
    }

    @Override
    public E update(E entity) {
        E result = super.update(entity);
        if (result == null) {
            writeToFile();
        }
        return result;
    }
}