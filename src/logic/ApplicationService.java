
package logic;

import java.io.IOException;

import model.AppState;
import storage.DataStore;

public class ApplicationService {

    private final AppState state;
    private final AccountService accountService;
    private final DataStore dataStore;

    public ApplicationService(
            AppState state, DataStore dataStore) {

        if (state == null || dataStore == null) {
            throw new IllegalArgumentException(
                "Application dependencies cannot be null."
            );
        }

        this.state = state;
        this.dataStore = dataStore;
        this.accountService = new AccountService(state);
    }

    public AppState getState() {
        return state;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public void save() throws IOException {
        dataStore.save(state);
    }
}
