import java.util.*;

/**
 * Mission 5. Dynamic programming on the tree of infections. Root the tree in person 0.
 * Input.Tree.neighbours() gives the neighbours of every person. The number of people is at most about 100, so a recursive DFS is fine here.
 */
public class Outbreak {

    /**
     * Choose people to test so that for every link (somebody infected somebody) at least one of the two is tested,
     * and the total cost of testing is as small as possible. cost[v] is the price of testing person v.
     * Returns tested[v] = true if person v is tested.
     *
     * State for every person v (looking only at v and everything below it): the smallest cost when v IS tested, and
     * the smallest cost when v is NOT tested. What must then be true about the children of v in each case?
     * Do not forget to rebuild the answer (who exactly is tested), not only the cost: the picture needs it.
     * Main calls this twice: with all costs equal to 1 (the line "Tests needed") and with the real costs ("Cheapest testing").
     */
    public static boolean[] cheapestTests(int n, int[][] nb, int[] cost) {
        boolean[] tested = new boolean[n];
        if (n == 0) return tested;
        long[] take = new long[n];
        long[] skip = new long[n];
        testingCosts(0, -1, nb, cost, take, skip);
        rebuild(0, -1, false, nb, take, skip, tested);
        return tested;
    }

    /**
     * The number of links on the longest chain of infections, that is the longest path in the tree (counted in links).
     * A path goes up from one person and down to another; for every person you need the two longest ways down.
     * Example: a line 0-1-2-3 has chain 3; a star with centre 0 and leaves 1, 2, 3 has chain 2.
     */
    public static int longestChain(int n, int[][] nb) {
        if (n == 0) return 0;
        int[] best = {0};
        depth(0, -1, nb, best);
        return best[0];
    }

    private static void testingCosts(int v, int parent, int[][] nb, int[] cost,
                                     long[] take, long[] skip) {
        take[v] = cost[v];
        for (int child : nb[v]) {
            if (child == parent) continue;
            testingCosts(child, v, nb, cost, take, skip);
            take[v] += Math.min(take[child], skip[child]);
            skip[v] += take[child]; // An untested parent forces every child to be tested.
        }
    }

    private static void rebuild(int v, int parent, boolean forced, int[][] nb,
                                long[] take, long[] skip, boolean[] tested) {
        tested[v] = forced || take[v] < skip[v];
        for (int child : nb[v]) {
            if (child != parent) rebuild(child, v, !tested[v], nb, take, skip, tested);
        }
    }

    // Return the longest downward path; combine the two best branches at each vertex.
    private static int depth(int v, int parent, int[][] nb, int[] best) {
        int first = 0, second = 0;
        for (int child : nb[v]) {
            if (child == parent) continue;
            int branch = 1 + depth(child, v, nb, best);
            if (branch > first) {
                second = first;
                first = branch;
            } else if (branch > second) {
                second = branch;
            }
        }
        best[0] = Math.max(best[0], first + second);
        return first;
    }
}
