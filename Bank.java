import java.util.HashMap;
import java.util.Map;
public class Bank {
    private final Map<String, Account> accounts;

    public Bank() {
        accounts = new HashMap<>();
        seedAccounts();
    }
    private void seedAccounts() {
        accounts.put("user1", new Account("user1", "1234", 1000.00));
        accounts.put("user2", new Account("user2", "5678", 500.00));
        accounts.put("user3", new Account("user3", "0000", 2500.00));
    }
    public Account authenticate(String userId, String pin) {
        Account acc = accounts.get(userId);
        if (acc != null && acc.checkPin(pin)) {
            return acc;
        }
        return null;
    }

    public boolean accountExists(String userId) {
        return accounts.containsKey(userId);
    }

    public Account getAccount(String userId) {
        return accounts.get(userId);
    }
}