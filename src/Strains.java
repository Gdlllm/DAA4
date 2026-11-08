/**
 * Missions 1 and 2. Compare two strains with dynamic programming.
 *
 * Both are the same kind of table: d[i][j] answers a question about the first i letters of a and the first j letters of b.
 * Think about three things before you type: what d[i][j] means, how it follows from smaller cells, and what the first row and column are.
 */
public class Strains {

    /**
     * Mission 1. The smallest number of single-letter changes that turn a into b. One change is: replace a letter,
     * insert a letter, or delete a letter. This number is the "Mutations" line.
     * Example: "ACGT" -> "AGT" is 1 (delete C). "GATTACA" -> "GCATGCA" needs 3.
     */
    public static int editDistance(String a, String b) {
        int[][] d = new int[a.length() + 1][b.length() + 1];
        // Empty prefixes require only insertions or deletions.
        for (int i = 0; i <= a.length(); i++) d[i][0] = i;
        for (int j = 0; j <= b.length(); j++) d[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int replace = d[i - 1][j - 1]
                        + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1);
                int delete = d[i - 1][j] + 1;
                int insert = d[i][j - 1] + 1;
                d[i][j] = Math.min(replace, Math.min(delete, insert));
            }
        }
        return d[a.length()][b.length()];
    }

    /**
     * Mission 2. The length of the longest common subsequence: letters that appear in both strains in the same order,
     * but not necessarily next to each other. This is the "Shared core" line.
     * Example: "ACGTT" and "AGTC": the longest is "AGT", length 3.
     */
    public static int lcsLength(String a, String b) {
        int[][] d = new int[a.length() + 1][b.length() + 1];
        // An empty prefix has no common letters with any other prefix.
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    d[i][j] = d[i - 1][j - 1] + 1;
                } else {
                    d[i][j] = Math.max(d[i - 1][j], d[i][j - 1]);
                }
            }
        }
        return d[a.length()][b.length()];
    }
}
