import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Makes the personal files of one student: strains.txt, genome.txt, markers.txt, tree.txt.
 *  Usage: java tools/MakeVariant.java STUDENT_ID [outdir]
 *  strains.txt: two lines "A SEQUENCE" and "B SEQUENCE" (two virus strains, letters A C G T).
 *  genome.txt:  one line, the reference genome (letters A C G T).
 *  markers.txt: line "marker SEQUENCE" (one gene to find), .
 *  tree.txt:    first line n (people), then n-1 lines "x y" (x infected y or y infected x, the order is not given),
 *               then one line with n numbers: the cost of testing person 0, 1, ..., n-1. */
public class MakeVariant {
    static final String SALT = "AITU-DAA-A4-ZERO-2026";
    static final char[] L = {'A', 'C', 'G', 'T'};

    static class Rng {
        long x;
        Rng(String sid, String tag) throws Exception {
            byte[] h = MessageDigest.getInstance("SHA-256").digest((SALT + "|" + sid + "|" + tag).getBytes(StandardCharsets.UTF_8));
            long v = 0; for (int i = 0; i < 8; i++) v = (v << 8) | (h[i] & 0xFFL);
            x = v;
        }
        long next() {
            x += 0x9E3779B97F4A7C15L;
            long z = x;
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            return z ^ (z >>> 31);
        }
        int below(int n) { return (int) Long.remainderUnsigned(next(), n); }
        int randint(int a, int b) { return a + below(b - a + 1); }
        char base() { return L[below(4)]; }
        String dna(int n) { StringBuilder s = new StringBuilder(); for (int i = 0; i < n; i++) s.append(base()); return s.toString(); }
    }

    public static void main(String[] args) throws Exception {
        String sid = args[0]; File out = new File(args.length > 1 ? args[1] : "."); out.mkdirs();

        // strains: B is A with random changes
        Rng r = new Rng(sid, "strains");
        String a = r.dna(r.randint(900, 1300));
        StringBuilder b = new StringBuilder(a);
        int edits = r.randint(60, 120);
        for (int e = 0; e < edits; e++) {
            int pos = r.below(b.length()), kind = r.below(3);
            if (kind == 0) { char c; do c = r.base(); while (c == b.charAt(pos)); b.setCharAt(pos, c); }
            else if (kind == 1) b.insert(pos, r.base());
            else if (b.length() > 100) b.deleteCharAt(pos);
        }
        try (PrintWriter w = new PrintWriter(new File(out, "strains.txt"))) { w.println("A " + a); w.println("B " + b); }

        // genome: random letters, two copies of a long repeat, three copies of a shorter one, then the markers
        Rng g = new Rng(sid, "genome");
        int N = g.randint(24000, 32000);
        char[] gen = g.dna(N).toCharArray();
        boolean[] used = new boolean[N];
        int L1 = g.randint(240, 320), L2 = 90;
        int[] p1 = place(g, used, N, L1, 2), p2 = place(g, used, N, L2, 3);
        for (int k = 1; k < p1.length; k++) System.arraycopy(gen, p1[0], gen, p1[k], L1);
        for (int k = 1; k < p2.length; k++) System.arraycopy(gen, p2[0], gen, p2[k], L2);
        // marker: a word with a border (its start repeats at its end)
        String head = g.dna(g.randint(3, 4));
        String marker = head + g.dna(g.randint(2, 3)) + head;
        List<String> panel = new ArrayList<>();
        while (panel.size() < 8) { String s = g.dna(g.randint(7, 9)); if (!panel.contains(s)) panel.add(s); }
        String base = panel.get(g.below(8));
        panel.add(base.substring(g.randint(2, 3)));      // a suffix of another pattern
        panel.add(panel.get(g.below(8)).substring(1, 5));  // a word inside another pattern
        Set<String> seen = new HashSet<>(); List<String> uniq = new ArrayList<>();
        for (String s : panel) if (seen.add(s) && !s.equals(marker)) uniq.add(s);
        panel = uniq;
        List<String> toPlant = new ArrayList<>();
        for (int k = g.randint(4, 7); k > 0; k--) toPlant.add(marker);
        for (String s : panel) for (int k = g.randint(2, 4); k > 0; k--) toPlant.add(s);
        for (String s : toPlant) {
            int[] q = place(g, used, N, s.length(), 1);
            for (int i = 0; i < s.length(); i++) gen[q[0] + i] = s.charAt(i);
        }
        try (PrintWriter w = new PrintWriter(new File(out, "genome.txt"))) { w.println(new String(gen)); }
        try (PrintWriter w = new PrintWriter(new File(out, "markers.txt"))) {
            w.println("marker " + marker);
        }

        // tree of infections
        Rng t = new Rng(sid, "tree");
        int n = t.randint(60, 100);
        List<int[]> edges = new ArrayList<>();
        for (int i = 1; i < n; i++) {
            int p = t.below(Math.min(i, 7)) ; p = i - 1 - p;
            edges.add(t.below(2) == 0 ? new int[]{p, i} : new int[]{i, p});
        }
        for (int i = edges.size() - 1; i > 0; i--) { int j = t.below(i + 1); int[] tmp = edges.get(i); edges.set(i, edges.get(j)); edges.set(j, tmp); }
        try (PrintWriter w = new PrintWriter(new File(out, "tree.txt"))) {
            w.println(n);
            for (int[] e : edges) w.println(e[0] + " " + e[1]);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) sb.append(i > 0 ? " " : "").append(t.randint(1, 9));
            w.println(sb);
        }
        System.out.println("Files written to " + out.getPath() + ": strains.txt genome.txt markers.txt tree.txt");
    }

    /** k non-overlapping places for a word of length len; marks them as used (with a gap of 1). */
    static int[] place(Rng g, boolean[] used, int N, int len, int copies) {
        int[] res = new int[copies];
        for (int c = 0; c < copies; c++) {
            while (true) {
                int p = g.below(N - len - 1) + 1; boolean ok = true;
                for (int i = p - 1; i <= p + len && ok; i++) if (used[i]) ok = false;
                if (ok) { for (int i = p; i < p + len; i++) used[i] = true; res[c] = p; break; }
            }
        }
        return res;
    }
}
