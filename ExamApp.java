import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Online Examination System (Task 4)
 *
 * A single-file Swing application implementing:
 *  - Login screen (username + password)
 *  - Profile update screen (display name + password, before starting the exam)
 *  - Exam screen (one MCQ at a time, 4 radio button options)
 *  - Next / Previous navigation
 *  - Countdown timer (30 minutes) with auto-submit at zero
 *  - Manual submit button with confirmation dialog
 *  - Result screen (score X/Y, time taken, correct/incorrect breakdown)
 *  - Close-button confirmation while an exam is in progress
 *  - Logout button on the result screen, returning to the login screen
 *
 * Compile:  javac ExamApp.java
 * Run:      java ExamApp
 *
 * Demo login: username "student", password "pass123"
 * (You can also create a new account from the login screen.)
 */
public final class ExamApp extends JFrame {

    // ---- Card names ----
    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_PROFILE = "PROFILE";
    public static final String CARD_EXAM = "EXAM";
    public static final String CARD_RESULT = "RESULT";

    // ---- Navigation ----
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // ---- "Database" (in-memory) ----
    private final Map<String, String> credentials = new LinkedHashMap<>();
    private final Map<String, String> displayNames = new HashMap<>();

    // ---- Session state ----
    private String currentUsername;
    private String currentDisplayName;

    // ---- Exam state ----
    private final List<Question> questions = buildQuestions();
    private int[] userAnswers;
    private int currentQuestionIndex;

    private static final int TIME_LIMIT_SECONDS = 30 * 60; // 30 minutes
    private int secondsRemaining;
    private Timer countdownTimer;
    private long examStartMillis;
    private boolean examInProgress = false;

    // ---- Screens ----
    private final LoginPanel loginPanel;
    private final ProfilePanel profilePanel;
    private final ExamPanel examPanel;
    private final ResultPanel resultPanel;

    public ExamApp() {
        super("Online Examination System");

        // Seed one demo account so the app is runnable out of the box.
        credentials.put("student", "pass123");
        displayNames.put("student", "Student");

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });

        loginPanel = new LoginPanel(this);
        profilePanel = new ProfilePanel(this);
        examPanel = new ExamPanel(this);
        resultPanel = new ResultPanel(this);

        cardPanel.add(loginPanel, CARD_LOGIN);
        cardPanel.add(profilePanel, CARD_PROFILE);
        cardPanel.add(examPanel, CARD_EXAM);
        cardPanel.add(resultPanel, CARD_RESULT);

        setContentPane(cardPanel);
        setSize(780, 580);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(null);
        
        showCard(CARD_LOGIN);
    }

    // ---------------------------------------------------------------
    // Session management (close-button confirmation)
    // ---------------------------------------------------------------
    private void handleWindowClose() {
        if (examInProgress) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "An exam is in progress. Are you sure you want to quit?",
                    "Confirm Exit",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                System.exit(0);
            }
            // else: do nothing, stay open
        } else {
            dispose();
            System.exit(0);
        }
    }

    void showCard(String name) {
        cardLayout.show(cardPanel, name);
    }

    // ---------------------------------------------------------------
    // Auth / profile
    // ---------------------------------------------------------------
    boolean login(String username, String password) {
        String stored = credentials.get(username);
        if (stored != null && stored.equals(password)) {
            currentUsername = username;
            currentDisplayName = displayNames.getOrDefault(username, username);
            return true;
        }
        return false;
    }

    boolean registerIfAbsent(String username, String password) {
        if (credentials.containsKey(username)) {
            return false;
        }
        credentials.put(username, password);
        displayNames.put(username, username);
        return true;
    }

    void notifyProfileScreenEntered() {
        profilePanel.loadCurrentUser();
    }

    String getCurrentUsername() {
        return currentUsername;
    }

    String getCurrentDisplayName() {
        return currentDisplayName;
    }

    void updateProfile(String newDisplayName, String newPassword) {
        if (newDisplayName != null && !newDisplayName.trim().isEmpty()) {
            currentDisplayName = newDisplayName.trim();
            displayNames.put(currentUsername, currentDisplayName);
        }
        if (newPassword != null && !newPassword.isEmpty()) {
            credentials.put(currentUsername, newPassword);
        }
    }

    void logout() {
        stopTimerIfRunning();
        examInProgress = false;
        currentUsername = null;
        currentDisplayName = null;
        loginPanel.reset();
        showCard(CARD_LOGIN);
    }

    // ---------------------------------------------------------------
    // Exam lifecycle
    // ---------------------------------------------------------------
    void startExam() {
        userAnswers = new int[questions.size()];
        Arrays.fill(userAnswers, -1);
        currentQuestionIndex = 0;
        secondsRemaining = TIME_LIMIT_SECONDS;
        examStartMillis = System.currentTimeMillis();
        examInProgress = true;

        examPanel.loadQuestion(0);
        examPanel.updateTimerLabel(secondsRemaining);
        showCard(CARD_EXAM);

        countdownTimer = new Timer(1000, e -> {
            secondsRemaining--;
            examPanel.updateTimerLabel(secondsRemaining);
            if (secondsRemaining <= 0) {
                stopTimerIfRunning();
                JOptionPane.showMessageDialog(
                        ExamApp.this,
                        "Time is up! Your exam is being submitted automatically.",
                        "Time's Up",
                        JOptionPane.INFORMATION_MESSAGE);
                finishExam();
            }
        });
        countdownTimer.start();
    }

    private void stopTimerIfRunning() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
    }

    void requestManualSubmit() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to submit the exam?",
                "Confirm Submit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            stopTimerIfRunning();
            finishExam();
        }
    }

    private void finishExam() {
        long elapsedMillis = System.currentTimeMillis() - examStartMillis;
        examInProgress = false;
        resultPanel.showResults(questions, userAnswers, elapsedMillis);
        showCard(CARD_RESULT);
    }

    List<Question> getQuestions() {
        return questions;
    }

    int[] getUserAnswers() {
        return userAnswers;
    }

    int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    void setCurrentQuestionIndex(int idx) {
        currentQuestionIndex = idx;
    }

    // ---------------------------------------------------------------
    // Sample question bank
    // ---------------------------------------------------------------
    private static List<Question> buildQuestions() {
        List<Question> list = new ArrayList<>();
        list.add(new Question(
                "Which keyword is used to inherit a class in Java?",
                new String[]{"implements", "extends", "inherits", "super"},
                1));
        list.add(new Question(
                "Which of these is NOT a primitive data type in Java?",
                new String[]{"int", "boolean", "String", "double"},
                2));
        list.add(new Question(
                "Which Swing class is typically used to drive a countdown timer?",
                new String[]{"javax.swing.Timer", "java.util.Date", "Thread.sleep", "Calendar"},
                0));
        list.add(new Question(
                "Which layout manager is commonly used to switch between multiple panels/screens?",
                new String[]{"BorderLayout", "GridLayout", "CardLayout", "FlowLayout"},
                2));
        list.add(new Question(
                "Which class groups JRadioButtons so only one can be selected at a time?",
                new String[]{"ButtonModel", "ButtonGroup", "RadioGroup", "ItemGroup"},
                1));
        list.add(new Question(
                "What does JVM stand for?",
                new String[]{"Java Verified Machine", "Java Virtual Machine", "Java Variable Method", "Joint Virtual Machine"},
                1));
        list.add(new Question(
                "Which method is the entry point of a standard Java application?",
                new String[]{"start()", "run()", "main()", "init()"},
                2));
        list.add(new Question(
                "Which exception is thrown when dividing an integer by zero?",
                new String[]{"NullPointerException", "ArithmeticException", "NumberFormatException", "IOException"},
                1));
        return list;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException |
                     IllegalAccessException | UnsupportedLookAndFeelException ignored) {
                // fall back to default look and feel
            }
            ExamApp app = new ExamApp();
            app.setVisible(true);
        });
    }
}

/** Simple immutable MCQ model: question text, 4 options, index of the correct option. */
class Question {
    final String text;
    final String[] options;
    final int correctIndex;

    Question(String text, String[] options, int correctIndex) {
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
    }
}

/** Login screen: username + password, on success loads the profile screen. */
class LoginPanel extends JPanel {
    private final ExamApp app;
    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel statusLabel = new JLabel(" ");

    LoginPanel(ExamApp app) {
        this.app = app;
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Online Examination System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        form.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        form.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        form.add(passwordField, gbc);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Create Account");
        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(loginButton);
        buttons.add(registerButton);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        form.add(buttons, gbc);

        gbc.gridy = 4;
        statusLabel.setForeground(Color.RED);
        form.add(statusLabel, gbc);

        JLabel hint = new JLabel("Demo account: student / pass123");
        hint.setForeground(Color.GRAY);
        gbc.gridy = 5;
        form.add(hint, gbc);

        add(form);

        loginButton.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> attemptRegister());
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Please enter both username and password.", Color.RED);
            return;
        }
        if (app.login(username, password)) {
            setStatus(" ", Color.RED);
            app.notifyProfileScreenEntered();
            app.showCard(ExamApp.CARD_PROFILE);
        } else {
            setStatus("Invalid username or password.", Color.RED);
        }
    }

    private void attemptRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            setStatus("Enter a username and password to create an account.", Color.RED);
            return;
        }
        if (app.registerIfAbsent(username, password)) {
            setStatus("Account created. Click Login to continue.", new Color(0, 128, 0));
        } else {
            setStatus("That username already exists.", Color.RED);
        }
    }

    private void setStatus(String text, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setText(text);
    }

    void reset() {
        usernameField.setText("");
        passwordField.setText("");
        setStatus(" ", Color.RED);
    }
}

/** Profile update screen: change display name / password before starting the exam. */
class ProfilePanel extends JPanel {
    private final ExamApp app;
    private final JLabel usernameLabel = new JLabel();
    private final JTextField displayNameField = new JTextField(18);
    private final JPasswordField newPasswordField = new JPasswordField(18);
    private final JPasswordField confirmPasswordField = new JPasswordField(18);
    private final JLabel statusLabel = new JLabel(" ");

    ProfilePanel(ExamApp app) {
        this.app = app;
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Update Your Profile");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        form.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        form.add(usernameLabel, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        form.add(new JLabel("Display Name:"), gbc);
        gbc.gridx = 1;
        form.add(displayNameField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        form.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        form.add(newPasswordField, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        form.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        form.add(confirmPasswordField, gbc);

        JLabel optionalHint = new JLabel("(Leave password fields blank to keep your current password)");
        optionalHint.setForeground(Color.GRAY);
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        form.add(optionalHint, gbc);

        JButton saveAndStartButton = new JButton("Save & Start Exam");
        JButton skipButton = new JButton("Skip & Start Exam");
        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(saveAndStartButton);
        buttons.add(skipButton);
        gbc.gridy = 6;
        form.add(buttons, gbc);

        gbc.gridy = 7;
        statusLabel.setForeground(Color.RED);
        form.add(statusLabel, gbc);

        add(form);

        saveAndStartButton.addActionListener(e -> saveAndStart());
        skipButton.addActionListener(e -> app.startExam());
    }

    void loadCurrentUser() {
        usernameLabel.setText(app.getCurrentUsername());
        displayNameField.setText(app.getCurrentDisplayName());
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        statusLabel.setText(" ");
    }

    private void saveAndStart() {
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        if (!newPassword.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }
        app.updateProfile(displayNameField.getText(), newPassword.isEmpty() ? null : newPassword);
        app.startExam();
    }
}

/** Exam screen: one MCQ at a time, radio buttons, Next/Previous, timer, submit. */
class ExamPanel extends JPanel {
    private final ExamApp app;
    private final JLabel timerLabel = new JLabel("Time Left: 30:00");
    private final JLabel questionCounterLabel = new JLabel();
    private final JLabel questionTextLabel = new JLabel();
    private final JRadioButton[] optionButtons = new JRadioButton[4];
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private final JButton prevButton = new JButton("Previous");
    private final JButton nextButton = new JButton("Next");
    private final JButton submitButton = new JButton("Submit Exam");

    ExamPanel(ExamApp app) {
        this.app = app;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top bar: question counter + always-visible countdown timer
        JPanel topBar = new JPanel(new BorderLayout());
        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.BOLD, 18f));
        timerLabel.setForeground(new Color(180, 0, 0));
        questionCounterLabel.setFont(questionCounterLabel.getFont().deriveFont(Font.BOLD, 14f));
        topBar.add(questionCounterLabel, BorderLayout.WEST);
        topBar.add(timerLabel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Center: question text + 4 radio button options
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        questionTextLabel.setFont(questionTextLabel.getFont().deriveFont(Font.PLAIN, 16f));
        questionTextLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(questionTextLabel);
        centerPanel.add(Box.createVerticalStrut(15));

        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            optionButtons[i].setFont(optionButtons[i].getFont().deriveFont(15f));
            final int idx = i;
            optionButtons[i].addActionListener(e -> {
                int[] answers = app.getUserAnswers();
                if (answers != null) {
                    answers[app.getCurrentQuestionIndex()] = idx;
                }
            });
            buttonGroup.add(optionButtons[i]);
            centerPanel.add(optionButtons[i]);
            centerPanel.add(Box.createVerticalStrut(8));
        }

        JScrollPane scrollPane = new JScrollPane(centerPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Bottom: Previous/Next on the left, Submit on the right
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navPanel.add(prevButton);
        navPanel.add(nextButton);
        JPanel submitPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitPanel.add(submitButton);
        bottomPanel.add(navPanel, BorderLayout.WEST);
        bottomPanel.add(submitPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        prevButton.addActionListener(e -> navigate(-1));
        nextButton.addActionListener(e -> navigate(1));
        submitButton.addActionListener(e -> app.requestManualSubmit());
    }

    private void navigate(int delta) {
        int newIndex = app.getCurrentQuestionIndex() + delta;
        List<Question> questions = app.getQuestions();
        if (newIndex < 0 || newIndex >= questions.size()) {
            return;
        }
        app.setCurrentQuestionIndex(newIndex);
        loadQuestion(newIndex);
    }

    void loadQuestion(int index) {
        List<Question> questions = app.getQuestions();
        Question q = questions.get(index);
        questionCounterLabel.setText("Question " + (index + 1) + " of " + questions.size());
        questionTextLabel.setText("<html><body style='width: 420px'>" + escape(q.text) + "</body></html>");

        buttonGroup.clearSelection();
        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setText(q.options[i]);
        }
        int existingAnswer = app.getUserAnswers()[index];
        if (existingAnswer >= 0) {
            optionButtons[existingAnswer].setSelected(true);
        }

        prevButton.setEnabled(index > 0);
        nextButton.setEnabled(index < questions.size() - 1);
    }

    void updateTimerLabel(int secondsRemaining) {
        int clamped = Math.max(secondsRemaining, 0);
        int mins = clamped / 60;
        int secs = clamped % 60;
        timerLabel.setText(String.format("Time Left: %02d:%02d", mins, secs));
        timerLabel.setForeground(secondsRemaining <= 60 ? Color.RED.darker() : new Color(180, 0, 0));
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}

/** Result screen: score, time taken, and a correct/incorrect breakdown; has a Logout button. */
class ResultPanel extends JPanel {
    private final JLabel scoreLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private final JPanel breakdownPanel = new JPanel();

    ResultPanel(ExamApp app) {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Exam Results");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        scoreLabel.setFont(scoreLabel.getFont().deriveFont(Font.BOLD, 16f));
        scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(title);
        topPanel.add(Box.createVerticalStrut(8));
        topPanel.add(scoreLabel);
        topPanel.add(timeLabel);
        add(topPanel, BorderLayout.NORTH);

        breakdownPanel.setLayout(new BoxLayout(breakdownPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(breakdownPanel);
        add(scrollPane, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Logout");
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(logoutButton);
        add(bottomPanel, BorderLayout.SOUTH);

        logoutButton.addActionListener(e -> app.logout());
    }

    void showResults(List<Question> questions, int[] userAnswers, long elapsedMillis) {
        int correctCount = 0;
        breakdownPanel.removeAll();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            int userAnswer = userAnswers[i];
            boolean isCorrect = userAnswer == q.correctIndex;
            if (isCorrect) {
                correctCount++;
            }

            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                    new EmptyBorder(8, 4, 8, 4)));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel qLabel = new JLabel("<html><body style='width: 500px'><b>Q" + (i + 1) + ".</b> "
                    + escape(q.text) + "</body></html>");
            qLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.add(qLabel);

            String yourAnswerText = userAnswer >= 0 ? q.options[userAnswer] : "(no answer)";
            String correctAnswerText = q.options[q.correctIndex];

            JLabel answerLabel = new JLabel("Your answer: " + yourAnswerText
                    + (isCorrect ? "   \u2714 Correct" : "   \u2716 Incorrect"));
            answerLabel.setForeground(isCorrect ? new Color(0, 128, 0) : new Color(180, 0, 0));
            answerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.add(answerLabel);

            if (!isCorrect) {
                JLabel correctLabel = new JLabel("Correct answer: " + correctAnswerText);
                correctLabel.setForeground(new Color(0, 0, 150));
                correctLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                row.add(correctLabel);
            }

            breakdownPanel.add(row);
        }

        scoreLabel.setText("Score: " + correctCount + " out of " + questions.size());
        long totalSeconds = elapsedMillis / 1000;
        long mins = totalSeconds / 60;
        long secs = totalSeconds % 60;
        timeLabel.setText(String.format("Time taken: %d min %02d sec", mins, secs));

        breakdownPanel.revalidate();
        breakdownPanel.repaint();
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}