import com.github.javaparser.utils.Pair;

import java.util.Comparator;
import java.util.List;

public class Utils {
    static class PairIntComparator implements Comparator<Pair<Integer, Integer>>
    {
        @Override
        public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
            int res = o1.a.compareTo(o2.a);
            if (res==0)
                return o1.b.compareTo(o2.b);
            return res;
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
}
