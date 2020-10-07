import com.github.javaparser.utils.Pair;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class Utils {

    /* compares by maximum potential length of segment starting from that piece */
    static class PotentialLengthComparator implements Comparator<Pair<Integer, Integer>>
    {
        @Override
        public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
            Integer nearestReplacePiece1 = Main.piecesToReplace.get(o1.a).ceiling(o1.b);
            if (nearestReplacePiece1==null)
                nearestReplacePiece1 = Main.blocks.get(o1.a).list.size();
            Integer nearestReplacePiece2 = Main.piecesToReplace.get(o1.a).ceiling(o1.b);
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
}
