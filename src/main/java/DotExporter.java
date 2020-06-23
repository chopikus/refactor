import com.github.javaparser.ast.Node;
import com.github.javaparser.utils.Pair;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

public class DotExporter {

    private static final HashMap<Pair<String, Integer>, Integer> nodeIds = new HashMap<>();
    private static final HashMap<String, String> fileContent = new HashMap<>();
    private static int counter = 0;

    private static String getName(Node node) {
        String result = node.toString();
        if (result.startsWith("{\n") && result.endsWith("\n}")) {
            result = result.substring(2);
            result = result.substring(0, result.length() - 2);
        }
        return "ID: "+node.getData(Main.NODE_ID)+" "+result.replace('"', '\'');
    }


    static void addEdge(String whichGraph, Node from, Node to) {
        if (!fileContent.containsKey(whichGraph))
            fileContent.put(whichGraph, "");
        Pair<String, Integer> pairFrom = new Pair<>(whichGraph, from.getData(Main.NODE_ID));
        Pair<String, Integer> pairTo = new Pair<>(whichGraph, to.getData(Main.NODE_ID));
        if (!nodeIds.containsKey(pairFrom)) {
            nodeIds.put(pairFrom, counter);
            fileContent.put(whichGraph,
                    fileContent.get(whichGraph) + String.format("node%s [label=\"%s\"];\n", nodeIds.get(pairFrom),
                            getName(from)));
            counter++;
        }
        if (!nodeIds.containsKey(pairTo)) {
            nodeIds.put(pairTo, counter);
            fileContent.put(whichGraph,
                    fileContent.get(whichGraph) + String.format("node%s [label=\"%s\"];\n", nodeIds.get(pairTo),
                            getName(to)));
            counter++;
        }
        fileContent.put(whichGraph, fileContent.get(whichGraph) + String.format("node%s -> node%s;\n",
                nodeIds.get(pairFrom), nodeIds.get(pairTo)));
    }

    static void export() {
        for (String fileName : fileContent.keySet()) {
            try {
                File printFile = new File("out/" + fileName + ".dot");
                if (!printFile.exists() && !printFile.getParentFile().mkdirs() && !printFile.createNewFile())
                    throw new Exception();
                FileWriter fileWriter = new FileWriter(printFile, false);
                fileWriter.write(String.format("digraph %s {\n%s}", fileName.split("\\.")[0].replace(" ", ""),
                        fileContent.get(fileName)));
                fileWriter.close();
            } catch (Exception e) {
                System.out.println("Could not export to DOT: " + fileName);
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
}
