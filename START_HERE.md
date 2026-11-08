# Start here

Read this page once, from top to bottom. It explains the setup, your files, the missions and how to submit.

## 1. What you need

- Java 17 or newer. Check in a terminal: `java -version` and `javac -version`. Both must show 17 or more. If `javac` is not found, you have only a JRE: install a JDK (for example Temurin 17 or 21).
- git. Check: `git --version`.
- An editor. IntelliJ IDEA Community or VS Code with the Java extension both work. You can also use only a terminal.

Windows, macOS and Linux all work. On Windows use PowerShell or cmd, not Git Bash if something behaves strangely. Every command in this file is the same on all three systems. If `javac -d out src/*.java` says it cannot find the files, run `java tools/Check.java` instead: it compiles for you. If you see strange characters or "unmappable character" warnings, add `-encoding UTF-8` after `javac`.

## 2. Open the right folder

Unzip the project. Open the folder `patient-zero`: the one that contains `src`, `tools` and `README.md`. Not the folder above it. All commands below are run in this folder.

If your editor complains: in IntelliJ choose File, New, Project from Existing Sources and mark `src` as the Sources root (blue folder). In VS Code open the folder and let the Java extension load it. If the editor still does not cooperate you do not need it for checking: `java tools/Check.java` works from any terminal.

## 3. Make your data

    java tools/MakeVariant.java YOUR_STUDENT_ID data

This creates four files in `data`. They are yours, made from your ID. Type the ID exactly as it is in Moodle, with all its digits: `12345` and `012345` give different files.

## 4. The story and the files

A virus is spreading. You are the analyst of the lab. You compare two strains of the virus, search a reference genome for a gene, and decide whom to test. A DNA sequence is a word of the letters A, C, G, T.

`strains.txt`: two lines, `A ACGT...` and `B ACGT...`. A is the old strain, B is the new one. About a thousand letters each.

`genome.txt`: one line with the reference genome, about 24 000 to 32 000 letters. Positions are counted from 0.

`markers.txt`: one line `marker WORD`: the gene you look for.

`tree.txt`: the tree of infections. The first line is the number of people n, people are numbered 0 to n-1. Then n-1 lines `x y`: one of them infected the other (which one is not given). The last line has n numbers: the cost of testing person 0, 1, 2, ... A tree has no cycles, and between two people there is exactly one chain of infections.

A tiny example you can solve on paper (it is in `examples/tiny`):

    strains.txt                  genome.txt     markers.txt     tree.txt
    A ACGTACGTACGTAC             ACGACGACGT     marker ACGAC    6
    B ACGTACGTACGTAG                                            0 1
                                                                0 2
                                                                1 3
                                                                1 4
                                                                2 5
                                                                3 2 4 1 5 2

Its 9 lines:

    Mutations: 1          Shared core: 13        Marker hits: 2
    First hit: 0          Border length: 2       Shared words: 2
    Tests needed: 2       Cheapest testing: 6    Longest chain: 4

Check that you can get each of them by hand before you write code.

## 5. See your results

Compile and run once:

    javac -encoding UTF-8 -d out src/*.java
    java -cp out Main

The program prints 9 lines (at the beginning most of them are wrong, that is normal) and writes `output/outbreak.html`. Open it in a browser by double click. This is the lab console. It recomputes every mission on your own data by itself and shows how each algorithm works, step by step, with animations you can play, pause and step through:

- scoreboard: a green tick or a red cross for every one of your 9 lines (the lab never shows the correct number, only whether you agree);
- case map of the genome with the hits of your KMP;
- 1 and 2: the table of edit distance filling up, the route through it, and the alignment of the two strains letter by letter; the shared core drawn as lines between the strains;
- 3: the KMP search, step by step, with the prefix table;
- 4: the rolling hash sliding over strain A, with a switch to a tiny hash size so you can see false alarms;
- 5: the numbers of the tree DP climbing from the leaves to the root, and the people your program chose to test.

If a mission shows a red cross, watch its animation and compare with what your code does, then use `java tools/Check.java` and the example in `examples/tiny`. Make a screenshot of the whole page for your README. The quiz asks about the same algorithms (20 questions, 30 minutes: 3 easy warm-up, 3 small calculations, 14 theory questions). Read `docs/Assignment4_defense_prep.pdf` to see what it contains, and use the animations as preparation.

## 6. Where you write code

    Mission  File            What you write
    1, 2     Strains.java    editDistance, lcsLength
    3, 4     Marker.java     prefixFunction, findAll (KMP), sharedWords (Rabin-Karp)
    5        Outbreak.java   cheapestTests, longestChain (DP on a tree)
    6        no code         commit after every mission, fill in README.md
    7        no code         run Bench, fill in the timing and complexity tables

You do not change `Main.java`, `Input.java`, `Render.java`, `Generator.java` and `Bench.java`: they are ready. Every file you write in has a comment above each method with the exact rules and a small example: read it before you type. Add your own helper classes if you want.

How long it takes: about 4 to 5 hours in total. Mission 1 about 40 minutes, 2 about 30 minutes, 3 about 1 hour, 4 about 45 minutes, 5 about 1 hour, 6 and 7 about 45 minutes. Do not leave Mission 3 for the last evening. If you are stuck, `docs/Hints.md` has the idea, the usual trap and how to see the mistake in the lab console for every mission.

## 7. Check yourself

    java tools/Check.java

It compiles everything and runs your program on three examples in `examples/`. For a wrong line it tells you which mission to look at. Run it after every mission. The examples: `tiny` you can solve on paper, `small` is a small random case, `dense` is full of overlaps (words inside words, letters repeating) and catches the typical mistakes.

The check cannot see everything. Your data are bigger and not all mistakes show on small examples, so also read the pictures. A solution that passes the examples but uses a slow idea (for example, recomputing everything inside a loop) can run for minutes on your genome. The program must finish on your data in under one minute.

## 8. Run on your data

    javac -encoding UTF-8 -d out src/*.java
    java -cp out Main

In an IDE run `Main`: without arguments it uses the four files in `data`. Paste the 9 lines into `README.md`. For Mission 7 run `java -cp out Bench` and copy the numbers.

## 9. Git

Do not leave this for the last night. The repository shows how you worked.

1. Create an empty public repository on GitHub, for example `patient-zero`.
2. In the project folder:

        git init
        git add .
        git commit -m "Add starter project"
        git branch -M main
        git remote add origin https://github.com/YOUR_NAME/patient-zero.git
        git push -u origin main

3. Work on `main`. After every mission make a commit with a message that says what changed, and push it:

        git add -A
        git commit -m "Add edit distance"
        git push

   Good messages: "Add KMP", "Fix off-by-one in the edit distance table". "update" tells nobody anything. At least one commit per mission, so at least 5 commits. `main` must compile.

Logging in. GitHub does not accept your password in the terminal. The easiest ways: GitHub Desktop, or sign in to GitHub from IntelliJ or VS Code (it opens a browser). With plain git you need a personal access token: GitHub, Settings, Developer settings, Personal access tokens, create one with the `repo` right, and use it instead of the password.

## 10. Submit

Fill in `README.md`: it is your report, with the section "AI use" (the syllabus requires you to say which AI tools you used and for what; write "none" if you used none). Check that the four files in `data` and the screenshot of `outbreak.html` are in the repository. Open the link in a private browser window to see that it works without your login. The deadline is set in Moodle: upload the link there before it. After the deadline a quiz follows: it asks about the algorithms of the missions (small calculations by hand and theory), so you must understand every line you submit.
