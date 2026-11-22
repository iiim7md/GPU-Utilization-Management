
package storage;

import java.io.*;
import java.nio.file.*;
import model.AppState;

public class FileDataStore implements DataStore {

    private final Path filePath;

    public FileDataStore(String fileName) {
        this.filePath = Paths.get(fileName);
    }

    @Override
    public void save(AppState state) throws IOException {
        if (state == null) {
            throw new IllegalArgumentException(
                "AppState cannot be null"
            );
        }

        Path parent = filePath.toAbsolutePath().getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (ObjectOutputStream output =
                new ObjectOutputStream(
                    Files.newOutputStream(filePath))) {

            output.writeObject(state);
        }
    }

    @Override
    public AppState load()
            throws IOException, ClassNotFoundException {

        if (Files.notExists(filePath)) {
            return new AppState();
        }

        try (ObjectInputStream input =
                new ObjectInputStream(
                    Files.newInputStream(filePath))) {

            return (AppState) input.readObject();
        }
    }
}
