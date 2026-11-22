
package storage;

import java.io.IOException;
import model.AppState;

public interface DataStore {

    void save(AppState state) throws IOException;

    AppState load() throws IOException, ClassNotFoundException;
}
