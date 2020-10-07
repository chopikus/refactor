import com.github.javaparser.utils.Pair;

import java.util.List;

public class Uniter {

    static void makeMethod(List<List<List<Pair<Integer, Integer>>>> duplicatedSegments) {
        for (var segmList : duplicatedSegments) {
            for (var segm : segmList) {
                for (var blockPiece : segm) {
                    System.out.println(Main.blocks.get(blockPiece.a).list.get(blockPiece.b));
                }
                System.out.println("+");
            }
            System.out.println("===========");
        }

    }
}
