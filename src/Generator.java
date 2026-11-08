import java.util.Random;

/** Random letters for Bench. Ready to use. */
public class Generator {
    public static String dna(int n, long seed) {
        Random r = new Random(seed); StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) s.append("ACGT".charAt(r.nextInt(4)));
        return s.toString();
    }
}
