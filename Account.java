import java.util.ArrayList;
import java.util.List;
public class Account {
    private final String userId;
    private String pin;
    private double balance;
    private final List<Transaction> history;

    public Account(String userId, String pin, double initialBalance) {
        this.userId = userId;
        this.pin = pin;
        this.balance = initialBalance;
        this.history = new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public boolean checkPin(String enteredPin) {
        return this.pin.equals(enteredPin);
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getHistory() {
        return history;
    }

    public void deposit(double amount) {
        balance += amount;
        history.add(new Transaction("DEPOSIT", amount, balance, ""));
    }

    public boolean withdraw(double amount) {
        if (amount > balance) {
            return false;
        }
        balance -= amount;
        history.add(new Transaction("WITHDRAW", amount, balance, ""));
        return true;
    }
    public boolean transferOut(double amount, String toUserId) {
        if (amount > balance) {
            return false;
        }
        balance -= amount;
        history.add(new Transaction("TRANSFER OUT", amount, balance, "to " + toUserId));
        return true;
    }

    public void transferIn(double amount, String fromUserId) {
        balance += amount;
        history.add(new Transaction("TRANSFER IN", amount, balance, "from " + fromUserId));
    }
}