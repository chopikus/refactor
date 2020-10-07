import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;

import java.beans.Expression;
import java.util.List;

public class Uniter {

    static void makeMethod(List<List<List<Pair<Integer, Integer>>>> duplicatedSegments)
    {
        for (var segmList : duplicatedSegments) {
            for (var segm : segmList) {
                final boolean[] ok = {true};
                for (var blockPiece : segm) {
                    Node node = Main.blocks.get(blockPiece.a).list.get(blockPiece.b).node;
                    node.walk(NameExpr.class, nameExpr -> {
                        try {
                            var res = nameExpr.resolve();
                            boolean okThat = false;
                            if (!res.isVariable() && !res.isParameter() && !res.isEnumConstant()
                                    && !res.isField() && !res.isMethod() && !res.isType())
                                okThat = true;
                            if (res.isMethod() && res.asMethod().isStatic())
                                okThat = true;
                            if (res.isField() && res.asField().isStatic())
                                okThat = true;
                            if (res.isEnumConstant())
                                okThat = true;
                            if (!okThat)
                                ok[0] = false;
                        }
                        catch (UnsolvedSymbolException ignored) {
                        }
                        /// TODO not ignoring the exception, showing warning in console
                    });
                    if (!ok[0])
                        break;
                }
                if (ok[0]) {
                    for (var blockPiece : segm) {
                        System.out.println(Main.blocks.get(blockPiece.a).list.get(blockPiece.b));
                    }
                    System.out.println("+");
                }
            }
            System.out.println("===========");
        }
    }
}
