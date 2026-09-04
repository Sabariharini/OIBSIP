# Java Console ATM Interface

A console-based ATM (Automated Teller Machine) simulation built in Java using
Object-Oriented Programming principles. Users authenticate with a User ID and
PIN, then perform standard banking operations — checking transaction history,
withdrawing, depositing, and transferring funds — all through a simple
text-menu interface.

## Objective

Build a console-based simulation of an ATM machine that allows users to
authenticate with a PIN and perform standard banking transactions.

## Tech Stack

- **Language:** Java (JDK 8+)
- **Type:** Console application
- **Design:** Object-Oriented, multi-class architecture
- **Dependencies:** None — uses only the Java Standard Library
  (`java.util.Scanner`, `java.util.ArrayList`/`HashMap`, `java.time`)

## Features

- Startup login with **User ID** and **PIN**; access is denied after **3
  incorrect attempts**
- Main menu after a successful login with the following options:
  1. **Transaction History** — view a log of all transactions made during the
     current session
  2. **Withdraw** — enter an amount, validate sufficient balance, update
     balance, log the transaction
  3. **Deposit** — enter an amount, update balance, log the transaction
  4. **Transfer** — enter a recipient User ID and amount, validate balance,
     update both accounts, log the transaction on both sides
  5. **Quit** — display a goodbye message and exit
- Balance check before every withdrawal/transfer; displays **"Insufficient
  Funds"** if the balance is too low
- All transactions stored in an `ArrayList<Transaction>` and displayed with
  timestamp, type, amount, and resulting balance
- Input validation for amounts (rejects non-numeric or non-positive values)
- Prevents transferring to your own account or to a non-existent account

## Project Structure

```
ATM-Interface/
└── src/
    ├── Main.java          # Entry point — starts the ATM session
    ├── Bank.java          # Stores all accounts; handles authentication
    ├── Account.java       # Represents a single account (balance, PIN, history)
    ├── Transaction.java   # Represents a single logged transaction
    └── ATM.java           # Core simulation logic: login flow, menu, transactions
```

### Class Overview

| Class | Responsibility |
|---|---|
| `Main` | Application entry point. Creates the `Bank`, `Scanner`, and `ATM`, then starts the session. |
| `Bank` | Manages the collection of all accounts (`Map<String, Account>`). Pre-loads demo accounts and authenticates User ID/PIN pairs. |
| `Account` | Encapsulates a single account's data — user ID, PIN, balance, and transaction history (private fields with public getters/setters). Contains `deposit()`, `withdraw()`, `transferOut()`, and `transferIn()` methods. |
| `Transaction` | An immutable record of one transaction: type, amount, resulting balance, timestamp, and optional details (e.g., transfer counterparty). |
| `ATM` | Drives the user-facing flow: login loop (max 3 attempts), main menu loop, and the logic for each menu option (history, withdraw, deposit, transfer, quit). |

## Design Principles Used

- **Encapsulation** — `Account` fields are `private`; all access goes through
  getters/setters or controlled methods (`deposit`, `withdraw`, etc.)
- **Separation of concerns** — each class has a single responsibility (data
  storage vs. business logic vs. user interaction)
- **Immutability** — `Transaction` objects are immutable once created
- **Defensive validation** — every transaction checks balance sufficiency and
  input validity before mutating state

## How to Run

### Prerequisites
- JDK 8 or later installed
- `javac` and `java` available on your PATH

### Compile
```bash
cd ATM-Interface/src
javac *.java
```

### Run
```bash
java Main
```

## Demo Accounts

The `Bank` class pre-loads the following accounts for testing (no external
database required):

| User ID | PIN | Starting Balance |
|---|---|---|
| `user1` | `1234` | $1000.00 |
| `user2` | `5678` | $500.00 |
| `user3` | `0000` | $2500.00 |

## Sample Session

```
=========================================
      WELCOME TO JAVA CONSOLE ATM
=========================================

Enter User ID: user1
Enter PIN: 1234

Login successful. Welcome, user1!

----------------- MAIN MENU -----------------
1. Transaction History
2. Withdraw
3. Deposit
4. Transfer
5. Quit
Choose an option (1-5): 2
Enter amount to withdraw: $200
Withdrawal successful. New balance: $800.00
```

## Known Limitations / Future Improvements

- **No persistence** — accounts and balances reset every time the program
  restarts (in-memory only via `HashMap` and `ArrayList`)
- Could be extended with file or database storage (e.g., JDBC + SQLite) to
  persist accounts and history across sessions
- Could add a "Change PIN" feature or account creation flow
- Could add unit tests (JUnit) for `Account` and `Bank` logic

## License

This project was built as a learning exercise for practicing Java OOP
concepts (encapsulation, multi-class design, collections).
