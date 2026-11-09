# CS102 Auto Grading System

A console application that grades a batch of student exam submissions (ZIP files)
in one go. For each submission it:

1. **Unzips** it into its own folder.
2. **Checks** it against the exam template (`RenameToYourUsername`): folder name,
   folder hierarchy, missing/extra files, and the `Name:` / `Email ID:` header in
   every `.java` file. Each problem is returned as an exception.
3. **Fixes** what it can, based on the exceptions found: renames the top folder to
   the username, moves `Q1`/`Q2`/`Q3` into a username folder, moves misplaced files,
   deletes junk (`.DS_Store`, `__MACOSX`, old `.class` files) and adds missing
   name/email headers.
4. **Grades** it by compiling and running the instructor's tester files with a time
   limit. A program that runs too long (e.g. an infinite loop) is stopped, and the
   testers are then run one by one so only the cases that hang lose marks.
5. **Exports** the totals to a CSV in the score sheet format.

If one submission fails, the program reports it and moves on to the next one.

## Requirements

- Java JDK 11 or newer (`javac` and `java` on the `PATH`)
- A shell that can run `.sh` files (Git Bash on Windows, Terminal on macOS/Linux)

No external libraries are used.

## How to compile and run

From the project folder:

```sh
sh compile.sh   # compiles everything in src/ into classes/
sh run.sh       # starts the application
```

## Using the application

```
========== Auto Grading System ==========
-- Grading steps --
1. Check submissions   (find problems)
2. Fix submissions     (repair what was found)
3. Grade submissions   (run the testers)
-- Results --
4. View results
5. Export results to CSV
-- Other --
6. Run all steps (1 -> 2 -> 3) on all submissions
7. Run all steps on one submission
8. Settings
9. Exit
```

The first three options are the grading steps, in order. Each one runs on every
ZIP in the submissions folder and shows progress (`[3/6] ...`). A step is skipped
for a submission that has not been through the step before it.

| Option | What it does |
|---|---|
| 1. Check | Unzips each submission into a fresh folder and lists every problem found. Nothing is changed yet. |
| 2. Fix | Repairs the problems found in step 1 and lists each fix made. |
| 3. Grade | Compiles and runs the tester files on each fixed submission and shows the scores. |
| 4. View results | Shows every graded student's score per question. |
| 5. Export results to CSV | Writes the graded students' totals to `output/ScoreSheet-graded.csv` in the score sheet format. The original score sheet is **not** changed. |
| 6. Run all steps | Runs check, fix and grade on every submission in one go. |
| 7. Run all steps on one | Lists the ZIP files and runs all the steps on the one you pick. |
| 8. Settings | Change the submissions folder, tester files folder, score sheet and time limit per program (1–60 seconds). Invalid entries are rejected and the old value is kept. |

A typical session is **1 → 2 → 3 → 4 → 5 → 9**, or **6 → 4 → 5 → 9** to do it all at once.

### Default settings

| Setting | Default |
|---|---|
| Submissions folder | `src/student-submission` |
| Tester files folder | `Project sample/TesterFiles` |
| Score sheet | `Project sample/IS442-ScoreSheet.csv` |
| Time limit per program | 5 seconds |

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
The marks for each case are listed in `src/model/QuestionResultTemplate.csv`.

**Score sheet:** the eLearn CSV format
(`OrgDefinedId,Username,Last Name,First Name,Email,...`). Student names and IDs are
read from it, and the exported copy has the score columns filled in.

## Project structure

```
src/
├── app/              Main, GradingMenu (console UI), GradingCtrl (runs the pipeline, keeps settings)
├── checker/          FileStructureChecker (compares a submission with the template),
│                     FolderNameChecker, MissingNameOrEmail
├── exceptions/       One exception type per kind of problem found (incl. InvalidZipException
│                     for empty or corrupt ZIPs)
├── fixer/            SubmissionFixer and the individual fixers (folder name, hierarchy,
│                     misplaced files, junk files, headers), UsernameResolver
├── grading/          Grading: compiles and runs the testers with a time limit;
│                     GradingResultsViewer: viewing and CSV export
├── model/            Student, StudentResult, QuestionResult, CaseResultStatus, Tester
├── filehandling/     FileHandlings: unzipping (zip-slip protection, empty/corrupt ZIP
│                     detection) and folder deletion
└── fileclasses/      FilesClass, Folder
```

Folders created while running (safe to delete, not part of the submission):
`classes/`, `unzippedFolder/`, `correctStructure/`, `studentStructure/`, `output/`.
