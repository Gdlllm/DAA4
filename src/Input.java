import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Reading of the four data files. Ready to use, you do not need to change it. */
public class Input {

    /** Two virus strains, letters A C G T. */
    public record Strains(String a, String b) {}

    /** The marker (one gene to find). */
    public record Markers(String marker, String[] panel) {}

    /** The tree of infections: n people numbered 0 .. n-1, n-1 links "a b" (somebody infected somebody, the direction is not given),
     *  and the cost of testing each person. */
    public record Tree(int n, int[] a, int[] b, int[] cost) {
        public int links() { return a.length; }
        /** neighbours[v] = the people linked with v. Root the tree in person 0 yourself. */
        public int[][] neighbours() {
            int[] deg = new int[n];
            for (int i = 0; i < a.length; i++) { deg[a[i]]++; deg[b[i]]++; }
            int[][] nb = new int[n][];
            for (int v = 0; v < n; v++) nb[v] = new int[deg[v]];
            int[] fill = new int[n];
            for (int i = 0; i < a.length; i++) { nb[a[i]][fill[a[i]]++] = b[i]; nb[b[i]][fill[b[i]]++] = a[i]; }
            return nb;
        }
    }

    /** Reads a text file as lines. Tolerates Windows line ends, a BOM at the start and blank lines at the end. */
    static List<String> lines(String path) throws IOException {
        String t = new String(Files.readAllBytes(Path.of(path)), java.nio.charset.StandardCharsets.UTF_8);
        if (t.startsWith("\uFEFF")) t = t.substring(1);
        List<String> l = new ArrayList<>(Arrays.asList(t.split("\\r?\\n", -1)));
        while (!l.isEmpty() && l.get(l.size() - 1).isBlank()) l.remove(l.size() - 1);
        return l;
    }

    public static Strains readStrains(String path) throws IOException {
        List<String> l = lines(path);
        return new Strains(l.get(0).split("\\s+")[1].trim(), l.get(1).split("\\s+")[1].trim());
    }

    public static String readGenome(String path) throws IOException {
        return lines(path).get(0).trim();
    }

    public static Markers readMarkers(String path) throws IOException {
        List<String> l = lines(path);
        String marker = null; List<String> panel = new ArrayList<>();
        for (String line : l) {
            if (line.isBlank()) continue;
            String[] p = line.trim().split("\\s+");
            if (p[0].equals("marker")) marker = p[1]; else panel.add(p[1]);
        }
        return new Markers(marker, panel.toArray(new String[0]));
    }

    public static Tree readTree(String path) throws IOException {
        List<String> l = lines(path);
        int n = Integer.parseInt(l.get(0).trim());
        int[] a = new int[n - 1], b = new int[n - 1], cost = new int[n];
        for (int i = 0; i < n - 1; i++) { String[] p = l.get(1 + i).trim().split("\\s+"); a[i] = Integer.parseInt(p[0]); b[i] = Integer.parseInt(p[1]); }
        String[] c = l.get(n).trim().split("\\s+");
        for (int i = 0; i < n; i++) cost[i] = Integer.parseInt(c[i]);
        return new Tree(n, a, b, cost);
    }
}
