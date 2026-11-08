import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.tools.*;

/**
 * Compiles your program and checks it on three small examples.
 * Run from the project folder:   java tools/Check.java
 */
public class Check {

    // the mission of each of the 9 output lines
    static final String[] MISSION = {"1", "2", "3", "3", "3", "4", "5", "5", "5"};

    public static void main(String[] args) throws Exception {
        List<String> files = new ArrayList<>();
        try (var s = Files.list(Path.of("src"))) { s.filter(p -> p.toString().endsWith(".java")).forEach(p -> files.add(p.toString())); }
        List<String> javac = new ArrayList<>(List.of("-encoding", "UTF-8", "-d", "out"));
        javac.addAll(files);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) { System.out.println("No compiler found. You need a JDK (javac), not only a JRE."); return; }
        if (compiler.run(null, null, null, javac.toArray(new String[0])) != 0) {
            System.out.println("The program does not compile. Fix the errors above.");
            return;
        }
        System.out.println("Compiles.\n\nExamples:");
        boolean all = true;
        for (String name : new String[]{"tiny", "small", "dense"}) {
            String d = "examples/" + name + "/";
            List<String> expected = Files.readAllLines(Path.of(d + "expected.txt"));
            List<String> got = run("Main", d + "strains.txt", d + "genome.txt", d + "markers.txt", d + "tree.txt");
            got.removeIf(l -> !l.contains(": ") || l.startsWith("WARNING"));
            int wrong = 0;
            StringBuilder report = new StringBuilder();
            Set<String> firstMissions = new LinkedHashSet<>();
            for (int i = 0; i < expected.size(); i++) {
                String g = i < got.size() ? got.get(i) : "(missing)";
                if (!g.equals(expected.get(i))) {
                    wrong++;
                    firstMissions.add(MISSION[i]);
                    if (wrong <= 3) report.append("    Mission ").append(MISSION[i]).append(": expected \"").append(expected.get(i)).append("\", got \"").append(g).append("\"\n");
                }
            }
            if (wrong == 0) System.out.println("  " + name + ": all 9 lines are right");
            else { all = false; System.out.println("  " + name + ": " + wrong + " of 9 lines are wrong"); System.out.print(report); }
        }
        System.out.println(all ? "\nAll examples pass. Now run it on your own data: java -cp out Main" : "\nFix the first mission in the list and run this check again.");
    }

    static List<String> run(String main, String... args) throws Exception {
        List<String> cmd = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp", "out", main));
        cmd.addAll(List.of(args));
        Path tmp = Files.createTempFile("check", ".txt");
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
        pb.redirectOutput(tmp.toFile());
        Process p = pb.start();
        if (!p.waitFor(20, java.util.concurrent.TimeUnit.SECONDS)) {
            p.destroyForcibly();
            System.out.println("  WARNING: " + main + " did not finish in 20 seconds (an endless loop?) and was stopped.");
        }
        List<String> lines = new ArrayList<>(Files.readAllLines(tmp));
        Files.deleteIfExists(tmp);
        return lines;
    }
}
