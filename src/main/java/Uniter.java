import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;

import java.util.*;

public class Uniter {

    static void unite(List<Node> nodes)
    {
        HashMap<List<String>, List<Integer>> map = new HashMap<>();
        List<ExpressionStmt> expressionStmts = new ArrayList<>();
        ArrayList<Integer> stmtToNode = new ArrayList<>();
        List<Boolean> commonStmt = new ArrayList<>();
        int nodeCounter=0;
        for (Node node : nodes)
        {
            int finalNodeCounter = nodeCounter;
            node.walk(Node.TreeTraversal.PREORDER, stmt -> {
                if (stmt instanceof ExpressionStmt) {
                    expressionStmts.add((ExpressionStmt) stmt);
                    stmtToNode.add(finalNodeCounter);
                    commonStmt.add(false);
                }
            });
            nodeCounter++;
        }
        int stmtCounter=0;
        for (ExpressionStmt stmt : expressionStmts)
        {
            List<String> dfsed = new ArrayList<String>();
            stmt.walk(node -> {
                dfsed.add(node.toString());
            });
            List<Integer> list;
            if (!map.containsKey(dfsed)) {
                list = new ArrayList<>();
            }
            else
                list = map.get(dfsed);
            list.add(stmtCounter);
            map.put(dfsed, list);
        }
        for (Map.Entry<List<String>, List<Integer>> entry : map.entrySet())
        {
            Set<Integer> nodeSet = new HashSet<>();
            for (Integer stmtNumber : entry.getValue())
                nodeSet.add(stmtToNode.get(stmtNumber));
            if (nodeSet.size()==nodes.size())
            {
                for (Integer stmtNumber : entry.getValue())
                    commonStmt.set(stmtNumber, true);
            }
        }
        stmtCounter = 0;
        for (ExpressionStmt stmt : expressionStmts)
        {
            if (commonStmt.get(stmtCounter))
            {
                stmt.walk(node -> {
                    if (node instanceof NameExpr)
                    {
                        NameExpr expr;

                    }
                });
            }
        }
    }
}