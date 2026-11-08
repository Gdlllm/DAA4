import java.util.*;

/** Independent small-case oracles; no external test framework is required. */
public class AlgorithmTests {
    private static final Random RANDOM = new Random(42);
    private static int checks;

    public static void main(String[] args) {
        equal(0, Strains.editDistance("", ""), "empty edit distance");
        equal(3, Strains.editDistance("", "ACG"), "insertions");
        equal(3, Strains.editDistance("GATTACA", "GCATGCA"), "known edit distance");
        equal(3, Strains.lcsLength("ACGTT", "AGTC"), "known LCS");
        require(Arrays.equals(new int[]{0, 0, 0, 1, 2}, Marker.prefixFunction("ACGAC")), "known prefix function");
        require(Arrays.equals(new int[]{0, 1, 2}, Marker.findAll("AAAA", "AA")), "overlaps");
        equal(3, Marker.sharedWords("AAAA", "AA", 2), "count positions, not words");
        equal(0, Marker.sharedWords("A", "AAAA", 2), "short strain");
        try {
            Marker.sharedWords("A", "A", 0);
            throw new AssertionError("nonpositive window accepted");
        } catch (IllegalArgumentException expected) { checks++; }

        for (int trial = 0; trial < 300; trial++) {
            String a = dna(RANDOM.nextInt(8)), b = dna(RANDOM.nextInt(8));
            equal(editOracle(a, b, 0, 0), Strains.editDistance(a, b), "edit distance vs exhaustive recursion");
            equal(lcsOracle(a, b), Strains.lcsLength(a, b), "LCS vs subsequence enumeration");
            int[] expectedPrefix = new int[b.length()];
            for (int i = 0; i < b.length(); i++) {
                for (int len = 1; len <= i; len++) {
                    if (same(b, 0, b, i + 1 - len, len)) expectedPrefix[i] = len;
                }
            }
            require(Arrays.equals(expectedPrefix, Marker.prefixFunction(b)), "prefix function vs exhaustive borders");
            List<Integer> expectedHits = new ArrayList<>();
            for (int i = 0; i + b.length() <= a.length(); i++) {
                if (same(a, i, b, 0, b.length())) expectedHits.add(i);
            }
            int[] actual = Marker.findAll(a, b);
            equal(expectedHits.size(), actual.length, "KMP count");
            for (int i = 0; i < actual.length; i++) equal(expectedHits.get(i), actual[i], "KMP position");
            for (int k = 1; k <= 9; k++) {
                int count = 0;
                for (int i = 0; i + k <= a.length(); i++) {
                    for (int j = 0; j + k <= b.length(); j++) {
                        if (same(a, i, b, j, k)) { count++; break; }
                    }
                }
                equal(count, Marker.sharedWords(a, b, k), "rolling hash vs direct comparison");
            }
        }
        collisionTest();
        equal(0, Outbreak.longestChain(0, new int[0][]), "empty tree diameter");
        equal(0, Outbreak.cheapestTests(0, new int[0][], new int[0]).length, "empty cover");
        for (int trial = 0; trial < 200; trial++) {
            int n = 1 + RANDOM.nextInt(11);
            int[] a = new int[n - 1], b = new int[n - 1], cost = new int[n];
            for (int i = 1; i < n; i++) { a[i - 1] = i; b[i - 1] = RANDOM.nextInt(i); }
            for (int i = 0; i < n; i++) cost[i] = RANDOM.nextInt(20);
            int[][] nb = new Input.Tree(n, a, b, cost).neighbours();
            checkCover(n, a, b, nb, cost);
            Arrays.fill(cost, 1);
            checkCover(n, a, b, nb, cost);
            int diameter = 0;
            for (int start = 0; start < n; start++) {
                int[] distance = new int[n];
                Arrays.fill(distance, -1);
                ArrayDeque<Integer> queue = new ArrayDeque<>();
                queue.add(start); distance[start] = 0;
                while (!queue.isEmpty()) {
                    int v = queue.remove();
                    diameter = Math.max(diameter, distance[v]);
                    for (int u : nb[v]) if (distance[u] == -1) {
                        distance[u] = distance[v] + 1; queue.add(u);
                    }
                }
            }
            equal(diameter, Outbreak.longestChain(n, nb), "diameter vs all-source BFS");
        }
        int[][] path = {{1}, {0, 2}, {1, 3}, {2}};
        int[] large = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
        checkCover(4, new int[]{0, 1, 2}, new int[]{1, 2, 3}, path, large);
        System.out.println("PASS: " + checks + " checks (seed 42, exhaustive oracles and real hash collision).");
    }

    private static void collisionTest() {
        // Find a real collision for the production hash, then require letter verification.
        Map<Long, String> seen = new HashMap<>();
        Random rng = new Random(123);
        for (int attempt = 0; attempt < 250000; attempt++) {
            StringBuilder word = new StringBuilder();
            long hash = 0;
            for (int i = 0; i < 12; i++) {
                char c = "ACGT".charAt(rng.nextInt(4));
                word.append(c); hash = (hash * 257 + c) % 1_000_000_007;
            }
            String s = word.toString(), previous = seen.putIfAbsent(hash, s);
            if (previous != null && !previous.equals(s)) {
                equal(0, Marker.sharedWords(previous, s, 12), "reject actual hash collision");
                equal(1, Marker.sharedWords(previous, s + previous, 12), "check all candidates in a collision bucket");
                return;
            }
        }
        throw new AssertionError("Could not construct collision fixture");
    }

    private static void checkCover(int n, int[] a, int[] b, int[][] nb, int[] cost) {
        long best = Long.MAX_VALUE;
        for (int mask = 0; mask < (1 << n); mask++) {
            boolean valid = true;
            for (int i = 0; i < a.length; i++) {
                if ((mask & (1 << a[i])) == 0 && (mask & (1 << b[i])) == 0) { valid = false; break; }
            }
            if (!valid) continue;
            long total = 0;
            for (int v = 0; v < n; v++) if ((mask & (1 << v)) != 0) total += cost[v];
            best = Math.min(best, total);
        }
        boolean[] selected = Outbreak.cheapestTests(n, nb, cost);
        long total = 0;
        for (int v = 0; v < n; v++) if (selected[v]) total += cost[v];
        for (int i = 0; i < a.length; i++) require(selected[a[i]] || selected[b[i]], "every edge covered");
        equal(best, total, "tree DP vs exhaustive subsets");
    }

    private static int editOracle(String a, String b, int i, int j) {
        if (i == a.length()) return b.length() - j;
        if (j == b.length()) return a.length() - i;
        if (a.charAt(i) == b.charAt(j)) return editOracle(a, b, i + 1, j + 1);
        return 1 + Math.min(editOracle(a, b, i + 1, j + 1),
                Math.min(editOracle(a, b, i + 1, j), editOracle(a, b, i, j + 1)));
    }

    private static int lcsOracle(String a, String b) {
        int best = 0;
        for (int mask = 0; mask < (1 << a.length()); mask++) {
            int j = 0, length = 0;
            boolean valid = true;
            for (int i = 0; i < a.length(); i++) if ((mask & (1 << i)) != 0) {
                while (j < b.length() && b.charAt(j) != a.charAt(i)) j++;
                if (j == b.length()) { valid = false; break; }
                j++; length++;
            }
            if (valid) best = Math.max(best, length);
        }
        return best;
    }

    private static boolean same(String a, int i, String b, int j, int k) {
        for (int x = 0; x < k; x++) if (a.charAt(i + x) != b.charAt(j + x)) return false;
        return true;
    }
    private static String dna(int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) s.append("ACGT".charAt(RANDOM.nextInt(4)));
        return s.toString();
    }
    private static void equal(long expected, long actual, String message) {
        require(expected == actual, message + ": expected " + expected + ", got " + actual);
    }
    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
}
