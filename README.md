# CS102 Auto Grading System

A desktop (Swing) application that grades a batch of student exam submissions
(ZIP files) in one go. For each submission it:

1. **Unzips** it into its own folder.
2. **Checks** it against the exam template (`RenameToYourUsername`): folder name
   (it must be the username from the ZIP name), folder hierarchy, missing/extra files, and the `Name:` / `Email ID:` header in
   every `.java` file. Each problem is returned as an exception.
3. **Fixes** what it can, based on the exceptions found: renames the top folder to
   the username, moves `Q1`/`Q2`/`Q3` into a username folder, moves misplaced files,
   deletes junk (`.DS_Store`, `__MACOSX`, old `.class` files) and adds missing
   name/email headers.
4. **Grades** it by compiling and running the instructor's tester files with a time
   limit. A program that runs too long (e.g. an infinite loop) is stopped. If that
   happens, or some code does not compile, the testers are run one by one so only
   the affected cases lose marks. Each case is marked as passed, failed, compile
   error, runtime error, timeout or missing file.
5. **Exports** the totals to a CSV in the score sheet format.

It can also **check for plagiarism** across all graded submissions.

If one submission fails, the program reports it and moves on to the next one.

## Requirements

- Java JDK 11 or newer (`javac` and `java` on the `PATH`)

No external libraries are used.

## How to compile and run

From the project folder:

| Windows | macOS / Linux / Git Bash |
|---|---|
| `compile.bat` | `sh compile.sh` |
| `run.bat` | `sh run.sh` |

The compile script puts the compiled program in `src/classes/`.

**Always compile before running, and start the app with the run script.**
Grading uses `src/classes/model/Tester.class` (the base class of every tester), and
all paths are relative to the project folder. If you start the app from an IDE's
Run button instead, `src/classes/` may be empty or out of date and every tester will
fail to compile.

## Using the application

The window has a sidebar of buttons and an output area that shows what each step
did.

| Button | What it does |
|---|---|
| 1. Check submissions | Unzips each submission into a fresh folder and lists every problem found. Nothing is changed yet. |
| 2. Fix submissions | Repairs the problems found in step 1 and lists each fix made. |
| 3. Grade submissions | Compiles and runs the tester files on each fixed submission and shows the scores. |
| View results | Shows every graded student's score per question. |
| Export CSV | Writes the graded students' totals to `output/ScoreSheet-graded.csv` in the score sheet format. The original score sheet is **not** changed. |
| Run all steps | Runs check, fix and grade on every submission in one go. |
| Run all on one | Lets you pick one ZIP file and runs all the steps on it. |
| Check plagiarism | Runs the three plagiarism checks (see below). Grade first. |
| Settings | Change the submissions folder, tester files folder, score sheet, student result template and model folder used for file cleanup/repair. Invalid entries are rejected and the old value is kept. |

Steps 1–3 run on every ZIP in the submissions folder and show progress
(`[3/6] ...`). **Fix** stays locked until **Check** has run, and **Grade** until
**Fix** has run. Once you start step by step, the two "Run all" buttons are locked
so the two ways of grading are not mixed.

A typical session is **1 → 2 → 3 → View results → Export CSV**, or
**Run all steps → View results → Export CSV** to do it all at once.

### Plagiarism check

1. **Shared wrong answers:** pairs of students who get the same test case wrong
   in exactly the same way. Answers the blank template gives (e.g. a student who
   left a question unchanged) are ignored. Highest score first.
2. **Foreign headers:** a file whose `Email ID:` header has another student's
   username. This reads the original ZIPs, because the fixer rewrites the header.
3. **Similar code:** pairs of students whose code in the same file is at least
   80% alike. Variable names, numbers and strings are ignored, so renaming does
   not hide copying, and the exam template's code is taken out first. This also
   reads the original ZIPs. Most similar first.

### Default settings

| Setting | Default |
|---|---|
| Submissions folder | `src/student-submission` |
| Tester files folder | `Project sample/TesterFiles` |
| Score sheet | `Project sample/IS442-ScoreSheet.csv` |
| Student result template | `Project sample/StudentResultTemplate.csv` |
| Model folder | `Project sample/RenameToYourUsername` |

Each program gets a fixed **5-second** time limit while grading. This is not a
setting.

### When the program closes

Closing the window stops any tester still running and deletes the files the
program made: `unzippedFolder/`, `correctStructure/`, `studentStructure/`,
`error.txt` and `output.txt`. **`output/` and `src/classes/` are kept.**

Results are only kept in memory, so **export the CSV and run the plagiarism
check before you close**.

## Input formats

**Submissions:** ZIP files named `<anything>-<anything>-<username>.zip`, e.g.
`2023-2024-ping.lee.2023.zip`. The username is taken from the file name.

**Tester files:** one folder per question part, each holding one tester per test case:

```
TesterFiles/
├── Q1a/Q1aPart1Tester.java, Q1aPart2Tester.java, ...
├── Q2a/Q2aPart1Tester.java, ...
├── Q3/Q3Part1Tester.java, ...
└── personstester.txt, studentstester.txt   (data files the testers read)
```

Each tester extends `model.Tester` and prints one line per case:
`<question>,<case>,<expected>,<actual>,<passed>,<points>`.

**Student result template:** the marks for each case, one line per case:

```
questionId,caseId,maxScore
Q1a,1,1.0
Q1a,2,1.0
```

**Score sheet:** the eLearn CSV format
(`OrgDefinedId,Username,Last Name,First Name,Email,...`). Student names and IDs are
read from it, and the exported copy has the score columns filled in.

## Project structure

```
src/
├── app/              Main (starts the GUI), GradingCtrl (runs the pipeline, keeps settings)
├── gui/              GradingGUI (main window), SettingGUI (settings screen), GuiStyle
├── checker/          FileStructureChecker (compares a submission with the template),
│                     FolderNameChecker, MissingNameOrEmail
├── exceptions/       One exception type per kind of problem found (incl. InvalidZipException
│                     for empty or corrupt ZIPs)
├── fixer/            SubmissionFixer and the individual fixers (folder name, hierarchy,
│                     misplaced files, junk files, headers), UsernameResolver
├── grading/          Grading: compiles and runs the testers with a time limit;
│                     GradingResultsViewer: results table and CSV export
├── plagiarism/       WrongOutputChecker (shared wrong answers), HeaderChecker (foreign
│                     headers), SimilarCodeChecker (similar code), PlagiarismReport
│                     (formats the reports)
├── model/            Student, StudentResult, QuestionResult, CaseResultStatus, Tester
├── filehandling/     FileHandlings: unzipping (zip-slip protection, empty/corrupt ZIP
│                     detection) and folder deletion
└── fileclasses/      FilesClass, Folder (not used by the app)
```

Folders created while running (not part of the submission):
`src/classes/`, `unzippedFolder/`, `correctStructure/`, `studentStructure/`, `output/`.
All except `src/classes/` and `output/` are deleted when the program closes.
