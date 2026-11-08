/**
 * Mission 7. Timing. Ready: you do not change it, you only run it and copy the numbers to README.md.
 * Run:  java -cp out Bench
 */
public class Bench {

    public static void main(String[] args) {
        System.out.println("A. Looking for 'AAAA...AAB' (length 2000) in a text of only A letters: naive loop and your KMP, milliseconds (average of 3 runs)");
        String pattern = "A".repeat(1999) + "B";
        for (int size : new int[]{20000, 40000, 80000}) {
            String text = "A".repeat(size);
            double naive = 0, kmp = 0;
            for (int r = 0; r < 3; r++) { naive += timeMs(() -> naiveCount(text, pattern)); kmp += timeMs(() -> Marker.findAll(text, pattern)); }
            System.out.printf("   text %6d   naive %8.1f ms   KMP %6.1f ms%n", size, naive / 3, kmp / 3);
        }
        System.out.println("B. Your edit distance on two random strains, milliseconds (average of 3 runs)");
        for (int size : new int[]{500, 1000, 2000, 4000}) {
            String a = Generator.dna(size, 2), b = Generator.dna(size, 3);
            double t = 0;
            for (int r = 0; r < 3; r++) t += timeMs(() -> Strains.editDistance(a, b));
            System.out.printf("   length %5d   edit distance %7.1f ms%n", size, t / 3);
        }
    }

    static int naiveCount(String t, String p) {
        int c = 0;
        for (int i = 0; i + p.length() <= t.length(); i++) {
            int j = 0;
            while (j < p.length() && t.charAt(i + j) == p.charAt(j)) j++;
            if (j == p.length()) c++;
        }
        return c;
    }

    static double timeMs(Runnable task) {
        long start = System.nanoTime();
        task.run();
        return (System.nanoTime() - start) / 1e6;
    }
}
