import java.util.List;
import java.util.Scanner;
public class ATM {
    private static final int MAX_ATTEMPTS = 3;

    private final Bank bank;
    private final Scanner scanner;
    private Account currentAccount;

    public ATM(Bank bank, Scanner scanner) {
        this.bank = bank;
        this.scanner = scanner;
    }
    public void start() {
        System.out.println("=========================================");
        System.out.println("      WELCOME TO JAVA CONSOLE ATM");
        System.out.println("=========================================");

        if (login()) {
            mainMenu();
        } else {
            System.out.println("\nToo many incorrect attempts. Card retained.");
            System.out.println("Please contact your bank branch. Exiting...");
        }
    }

    private boolean login() {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            System.out.print("\nEnter User ID: ");
            String userId = scanner.nextLine().trim();

            System.out.print("Enter PIN: ");
            String pin = scanner.nextLine().trim();

            Account acc = bank.authenticate(userId, pin);
            if (acc != null) {
                currentAccount = acc;
                System.out.println("\nLogin successful. Welcome, " + userId + "!");
                return true;
            } else {
                int remaining = MAX_ATTEMPTS - attempt;
                if (remaining > 0) {
                    System.out.println("Incorrect User ID or PIN. Attempts remaining: " + remaining);
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private void mainMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n----------------- MAIN MENU -----------------");
            System.out.println("1. Transaction History");
            System.out.println("2. Withdraw");
            System.out.println("3. Deposit");
            System.out.println("4. Transfer");
            System.out.println("5. Quit");
            System.out.print("Choose an option (1-5): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    showTransactionHistory();
                    break;
                case "2":
                    withdraw();
                    break;
                case "3":
                    deposit();
                    break;
                case "4":
                    transfer();
                    break;
                case "5":
                    running = false;
                    System.out.println("\nThank you for using Java Console ATM. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1-5.");
            }
        }
    }

    private void showTransactionHistory() {
        List<Transaction> history = currentAccount.getHistory();
        System.out.println("\n------------- TRANSACTION HISTORY -------------");
        if (history.isEmpty()) {
            System.out.println("No transactions yet this session.");
        } else {
            for (Transaction t : history) {
                System.out.println(t);
            }
        }
    }

    private void withdraw() {
        double amount = promptForAmount("withdraw");
        if (amount <= 0) return;

        if (amount > currentAccount.getBalance()) {
            System.out.println("Insufficient Funds. Current balance: $" +
                    String.format("%.2f", currentAccount.getBalance()));
            return;
        }

        currentAccount.withdraw(amount);
        System.out.printf("Withdrawal successful. New balance: $%.2f%n", currentAccount.getBalance());
    }

    private void deposit() {
        double amount = promptForAmount("deposit");
        if (amount <= 0) return;

        currentAccount.deposit(amount);
        System.out.printf("Deposit successful. New balance: $%.2f%n", currentAccount.getBalance());
    }

    private void transfer() {
        System.out.print("\nEnter recipient User ID: ");
        String recipientId = scanner.nextLine().trim();

        if (recipientId.equals(currentAccount.getUserId())) {
            System.out.println("You cannot transfer to your own account.");
            return;
        }

        if (!bank.accountExists(recipientId)) {
            System.out.println("Recipient account not found.");
            return;
        }

        double amount = promptForAmount("transfer");
        if (amount <= 0) return;

        if (amount > currentAccount.getBalance()) {
            System.out.println("Insufficient Funds. Current balance: $" +
                    String.format("%.2f", currentAccount.getBalance()));
            return;
        }

        Account recipient = bank.getAccount(recipientId);
        currentAccount.transferOut(amount, recipientId);
        recipient.transferIn(amount, currentAccount.getUserId());

        System.out.printf("Transfer successful. New balance: $%.2f%n", currentAccount.getBalance());
    }
    private double promptForAmount(String actionName) {
        System.out.print("Enter amount to " + actionName + ": $");
        String input = scanner.nextLine().trim();
        try {
            double amount = Double.parseDouble(input);
            if (amount <= 0) {
                System.out.println("Amount must be greater than zero.");
                return -1;
            }
            return amount;
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount entered.");
            return -1;
        }
    }
}
