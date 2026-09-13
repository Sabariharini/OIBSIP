# Online Examination System (Java Swing)

Single-file implementation of Task 4, covering every item on the checklist.

## Run it

Needs a JDK (not just a JRE) — javac wasn't available in this sandbox, so
please compile/run it locally:

```
javac ExamApp.java
java ExamApp
```

## Demo login
- Username: `student`
- Password: `pass123`

Or click **Create Account** on the login screen to make a new one.

## Where each checklist item lives
- **Login screen** → `LoginPanel`
- **Profile update screen** → `ProfilePanel` (shown right after login, before the exam starts; "Skip & Start Exam" bypasses it)
- **Exam screen** (1 MCQ + 4 radio buttons) → `ExamPanel`
- **Next/Previous navigation** → `ExamPanel.navigate()`
- **Countdown timer, always visible, auto-submit at 0** → `ExamApp.startExam()` (a `javax.swing.Timer` ticking every second) + `ExamPanel.updateTimerLabel()`
- **Manual submit + confirmation dialog** → `ExamApp.requestManualSubmit()`
- **Result screen** (score, time taken, correct/incorrect breakdown) → `ResultPanel`
- **Close-button confirmation during an exam** → `ExamApp.handleWindowClose()`
- **Logout button on results, back to login** → `ExamApp.logout()`

## Notes / things you may want to change
- Accounts are stored in memory only (a `HashMap`), so they reset each run. Swap in a file or DB if you need persistence.
- The question bank (`ExamApp.buildQuestions()`) has 8 sample Java-trivia MCQs — replace with your own.
- Time limit is 30 minutes (`TIME_LIMIT_SECONDS`), easy to change to whatever your assignment wants.
