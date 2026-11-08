import java.io.*;
import java.util.*;

/**
 * Reads your four data files, calls your methods and prints 9 lines. It also writes the page output/outbreak.html:
 * open it in a browser. Ready to use, you do not change it.
 * Run without arguments to use data/strains.txt, data/genome.txt, data/markers.txt and data/tree.txt,
 * or give four file names in this order: strains genome markers tree.
 */
public class Main {
    static final int WORD = 12;

    static void print(String name, long v) { System.out.println(name + ": " + v); }

    public static void main(String[] args) throws IOException {
        String[] f = args.length >= 4 ? args : new String[]{"data/strains.txt", "data/genome.txt", "data/markers.txt", "data/tree.txt"};
        if (!new File(f[0]).exists()) {
            System.out.println("No data files. Make your files first:  java tools/MakeVariant.java YOUR_ID data");
            return;
        }
        Input.Strains st = Input.readStrains(f[0]);
        String genome = Input.readGenome(f[1]);
        Input.Markers mk = Input.readMarkers(f[2]);
        Input.Tree tree = Input.readTree(f[3]);

        // Missions 1 and 2
        int mutations = Strains.editDistance(st.a(), st.b());
        int core = Strains.lcsLength(st.a(), st.b());

        // Mission 3
        int[] hits = Marker.findAll(genome, mk.marker());
        int[] pi = Marker.prefixFunction(mk.marker());
        int border = pi.length == 0 ? 0 : pi[pi.length - 1];

        // Mission 4
        int shared = Marker.sharedWords(st.a(), st.b(), WORD);

        // Mission 5
        int[][] nb = tree.neighbours();
        int[] ones = new int[tree.n()]; Arrays.fill(ones, 1);
        boolean[] fewest = Outbreak.cheapestTests(tree.n(), nb, ones);
        boolean[] cheapest = Outbreak.cheapestTests(tree.n(), nb, tree.cost());
        int tests = 0; long cost = 0;
        for (int v = 0; v < tree.n(); v++) { if (fewest[v]) tests++; if (cheapest[v]) cost += tree.cost()[v]; }
        int chain = Outbreak.longestChain(tree.n(), nb);
        for (int i = 0; i < tree.links(); i++)
            if (!cheapest[tree.a()[i]] && !cheapest[tree.b()[i]]) { System.out.println("WARNING: the link " + tree.a()[i] + " - " + tree.b()[i] + " has nobody tested"); break; }

        print("Mutations", mutations);
        print("Shared core", core);
        print("Marker hits", hits.length);
        print("First hit", hits.length == 0 ? -1 : hits[0]);
        print("Border length", border);
        print("Shared words", shared);
        print("Tests needed", tests);
        print("Cheapest testing", cost);
        print("Longest chain", chain);

        new File("output").mkdirs();
        Render.page(st, genome, mk, hits, tree, cheapest,
                new long[]{mutations, core, hits.length, hits.length == 0 ? -1 : hits[0], border, shared, tests, cost, chain},
                "output/outbreak.html");
    }
}
