import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.utils.Pair;

import java.util.Comparator;
import java.util.List;

public class Utils {

    /* compares by maximum potential length of segment starting from that piece */
    static class PotentialLengthComparator implements Comparator<Pair<Integer, Integer>>
    {
        @Override
        public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
            Integer nearestReplacePiece1 = Main.badPieces.get(o1.a).ceiling(o1.b);
            if (nearestReplacePiece1==null)
                nearestReplacePiece1 = Main.blocks.get(o1.a).list.size();
            Integer nearestReplacePiece2 = Main.badPieces.get(o1.a).ceiling(o1.b);
            if (nearestReplacePiece2==null)
                nearestReplacePiece2 = Main.blocks.get(o1.a).list.size();
            return Integer.compare(nearestReplacePiece1-o1.b, nearestReplacePiece2-o2.b);
        }
    }
    /*
     compares lists of pair<block(integer) , index (integer) >
     The list with minimum of max(index) should be first
    */
    static class ListBlockIndexComparator implements Comparator<List<Pair<Integer, Integer>>>
    {
        @Override
        public int compare(List<Pair<Integer, Integer>> o1, List<Pair<Integer, Integer>> o2) {
            int max1=0,max2=0;
            for (Pair<Integer, Integer> p : o1)
                max1 = Math.max(p.b, max1);
            for (Pair<Integer, Integer> p : o2)
                max2 = Math.max(p.b, max2);
            return Integer.compare(max1, max2);
        }
    }

    static <T> T getLast(List<T> l)
    {
        return l.get(l.size()-1);
    }

    static int hashNode(Node node)
    {
        final String[] s = {""};
        node.walk(com.github.javaparser.ast.Node.TreeTraversal.PREORDER, node1 -> {
            s[0]+=node1.getMetaModel().getTypeNameGenerified();
            if (node1 instanceof BinaryExpr)
                s[0]+=((BinaryExpr) node1).getOperator();
            if (node1 instanceof UnaryExpr)
                s[0]+=((UnaryExpr) node1).getOperator();
            if (node1 instanceof LiteralExpr)
                s[0]+=((LiteralExpr) node1).calculateResolvedType();
        });
        return s[0].hashCode();
    }

    static String makeNameFromNode(Node node)
    {
        return node.toString().replaceAll("[.;]", "").
                split("[()]")[0];
    }
}
