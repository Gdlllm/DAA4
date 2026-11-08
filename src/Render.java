import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Writes output/outbreak.html, the lab console, from the results of your program. Ready to use, you do not change it.
 *  The page itself is the file src/lab.html (it is also ready). */
public class Render {

    public static void page(Input.Strains st, String genome, Input.Markers mk, int[] hits,
                            Input.Tree tree, boolean[] tested, long[] my, String file) throws IOException {
        Path template = Path.of("src", "lab.html");
        if (!Files.exists(template)) { System.out.println("The page was not written: src/lab.html is missing (run the program from the project folder)."); return; }
        StringBuilder d = new StringBuilder("{");
        d.append("\"A\":\"").append(st.a()).append("\",\"B\":\"").append(st.b()).append("\",");
        d.append("\"genome\":\"").append(genome).append("\",\"marker\":\"").append(mk.marker()).append("\",");
        d.append("\"hits\":").append(Arrays.toString(hits)).append(",");
        d.append("\"tree\":{\"n\":").append(tree.n()).append(",\"a\":").append(Arrays.toString(tree.a())).append(",\"b\":").append(Arrays.toString(tree.b()));
        d.append(",\"cost\":").append(Arrays.toString(tree.cost())).append(",\"tested\":[");
        for (int i = 0; i < tested.length; i++) d.append(i > 0 ? "," : "").append(tested[i] ? 1 : 0);
        d.append("]},\"my\":").append(Arrays.toString(my)).append("}");
        String page = Files.readString(template).replace("/*DATA*/", d.toString());
        try (PrintWriter w = new PrintWriter(file)) { w.print(page); }
    }
}
