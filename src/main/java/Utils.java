import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.utils.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class Utils {

    static Set<String> javaKeywords = Set.of("abstract" ,"continue" ,"for" ,"new" ,"switch",
            "assert" ,"default" ,"goto" ,"package" ,"synchronized",
            "boolean" ,"do" ,"if" ,"private" ,"this",
            "break" ,"double" ,"implements" ,"protected" ,"throw",
            "byte" ,"else" ,"import" ,"public" ,"throws",
            "case" ,"enum" ,"instanceof" ,"return" ,"transient",
            "catch" ,"extends" ,"int" ,"short" ,"try",
            "char" ,"final" ,"interface" ,"static" ,"void",
            "class" ,"finally" ,"long" ,"strictfp" ,"volatile",
            "const" ,"float" ,"native" ,"super" ,"while", "null",  "true", "false", "string");

    /* compares by maximum potential length of segment starting from that piece */
    static class PotentialLengthComparator implements Comparator<Pair<Integer, Integer>> {
        @Override
        public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
            Integer nearestReplacePiece1 = Main.badPieces.get(o1.a).ceiling(o1.b);
            if (nearestReplacePiece1 == null)
                nearestReplacePiece1 = Main.blocks.get(o1.a).list.size();
            Integer nearestReplacePiece2 = Main.badPieces.get(o1.a).ceiling(o1.b);
            if (nearestReplacePiece2 == null)
                nearestReplacePiece2 = Main.blocks.get(o1.a).list.size();
            return Integer.compare(nearestReplacePiece1 - o1.b, nearestReplacePiece2 - o2.b);
        }
    }

    /*
     compares lists of pair<block(integer) , index (integer) >
     The list with minimum of max(index) should be first
    */
    static class ListBlockIndexComparator implements Comparator<List<Pair<Integer, Integer>>> {
        @Override
        public int compare(List<Pair<Integer, Integer>> o1, List<Pair<Integer, Integer>> o2) {
            int max1 = 0, max2 = 0;
            for (Pair<Integer, Integer> p : o1)
                max1 = Math.max(p.b, max1);
            for (Pair<Integer, Integer> p : o2)
                max2 = Math.max(p.b, max2);
            return Integer.compare(max1, max2);
        }
    }
    static class TypicalPairComparator implements Comparator<Pair<Integer, Integer>>
    {
        @Override
        public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
            if (!o1.a.equals(o2.a))
                return Integer.compare(o1.a, o2.a);
            return Integer.compare(o1.b, o2.b);
        }
    }

    static <T> T getLast(List<T> l) {
        return l.get(l.size() - 1);
    }

    static int hashNode(Node node, boolean... hashNames) {
        final String[] s = {""};
        node.walk(com.github.javaparser.ast.Node.TreeTraversal.PREORDER, node1 -> {
            s[0] += node1.getMetaModel().getTypeNameGenerified();
            if (node1 instanceof BinaryExpr)
                s[0] += ((BinaryExpr) node1).getOperator();
            if (node1 instanceof UnaryExpr)
                s[0] += ((UnaryExpr) node1).getOperator();
            if (hashNames.length>=1 && hashNames[0] && node1 instanceof NameExpr)
                s[0] += ((NameExpr) node1).getNameAsString();
            if (node1 instanceof LiteralExpr) {
                try {
                    s[0] += ((LiteralExpr) node1).calculateResolvedType();
                }
                catch (Exception ignored) {} ///TODO not ignore the exception
            }
        });
        return s[0].hashCode();
    }

    static String makeNameFromNode(Node node, String... defaultName) {
        String[] mightReses = node.toString().replaceAll("[=\\-.;\\[\\]]", "").
                split("[( ).,='\";{}\\-]");
        String res = "duplicateFunction";
        if (defaultName.length!=0)
            res = defaultName[0];
        for (String mightRes : mightReses) {
            if (!mightRes.equals("") && mightRes.matches("[a-zA-Z]+") && !javaKeywords.contains(mightRes.toLowerCase())) {
                res = mightRes;
            }
        }
        return res;
    }

    public static boolean isDirEmpty(final Path directory) throws IOException {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    public static class SegmentTree {
        public static class STNode {
            int leftIndex;
            int rightIndex;
            int max;
            STNode leftNode;
            STNode rightNode;
        }

        static STNode constructSegmentTree(int[] A, int l, int r) {
            if (l == r) {
                STNode node = new STNode();
                node.leftIndex = l;
                node.rightIndex = r;
                node.max = A[l];
                return node;
            }
            int mid = (l + r) / 2;
            STNode leftNode = constructSegmentTree(A, l, mid);
            STNode rightNode = constructSegmentTree(A, mid+1, r);
            STNode root = new STNode();
            root.leftIndex = leftNode.leftIndex;
            root.rightIndex = rightNode.rightIndex;
            root.max = Math.max(leftNode.max, rightNode.max);
            root.leftNode = leftNode;
            root.rightNode = rightNode;
            return root;
        }

        static int getMax(STNode root, int l, int r) {
            if (root.leftIndex >= l && root.rightIndex <= r) {
                return root.max;
            }
            if (root.rightIndex < l || root.leftIndex > r) {
                return Integer.MIN_VALUE;
            }
            return Math.max(getMax(root.leftNode, l, r), getMax(root.rightNode, l, r));
        }
    }
    static void writeToFile(String s, File file) {
        try {
            File parent = file.getAbsoluteFile().getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IllegalStateException("Couldn't create dir: " + parent);
            }
            FileWriter fw = new FileWriter(file);
            fw.write(s);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

