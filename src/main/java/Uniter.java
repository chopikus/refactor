import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.ExpressionStmt;

import java.util.*;

public class Uniter {

    static void unite(List<Node> nodes)
    {
        Map<List<String>, List<Integer>> exprMap = new HashMap<>();
        List<ExpressionStmt> expressionStmts = new ArrayList<>();
        ArrayList<Integer> stmtToNode = new ArrayList<>();
        List<Boolean> commonStmt = new ArrayList<>();
        Map<String, List<String> > variableHash = new HashMap<>();
        Map<List<String>, List<String> > varMap = new HashMap<>();
        int nodeCounter=0;
        for (Node node : nodes)
        {
            Set<String> variableNames = new HashSet<>();
            int finalNodeCounter = nodeCounter;
            node.walk(Node.TreeTraversal.PREORDER, stmt -> {
                if (stmt instanceof ExpressionStmt) {
                    expressionStmts.add((ExpressionStmt) stmt);
                    stmtToNode.add(finalNodeCounter);
                    commonStmt.add(false);
                }
                if (stmt instanceof VariableDeclarator)
                {
                    VariableDeclarator variableDecl = (VariableDeclarator) stmt;
                    System.out.println(variableDecl);
                    variableNames.add(variableDecl.getName().getIdentifier());
                }
            });
            node.walk(stmt -> {
               if (stmt instanceof SimpleName && !(stmt.getParentNodeForChildren() instanceof NameExpr)
                && variableNames.contains(((SimpleName) stmt).getIdentifier()))
                   stmt.setData(Main.IS_VARIABLE, true);
               else if (stmt instanceof NameExpr && variableNames.contains(((NameExpr) stmt).getName().getIdentifier()))
                   stmt.setData(Main.IS_VARIABLE, true);
               else
                   stmt.setData(Main.IS_VARIABLE, false);
            });
            nodeCounter++;
        }
        int stmtCounter=0;
        for (ExpressionStmt stmt : expressionStmts)
        {
            List<String> dfsed = new ArrayList<String>();
            stmt.walk(node -> {
                if (!node.getData(Main.IS_VARIABLE) && !(node instanceof LiteralExpr))
                    dfsed.add(node.getMetaModel().getTypeNameGenerified());
            });
            List<Integer> list;
            if (!exprMap.containsKey(dfsed)) {
                list = new ArrayList<>();
            }
            else
                list = exprMap.get(dfsed);
            list.add(stmtCounter);
            exprMap.put(dfsed, list);
            stmtCounter++;
        }
        for (Map.Entry<List<String>, List<Integer>> entry : exprMap.entrySet())
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
            int finalStmtCounter = stmtCounter;
            stmt.walk(node1 -> {
                if (node1.getData(Main.IS_VARIABLE))
                {
                    String key = stmtToNode.get(finalStmtCounter)+";"+node1.toString();
                    List<String> lst;
                    if (!variableHash.containsKey(key))
                        lst = new ArrayList<>();
                    else
                        lst = variableHash.get(key);
                    final boolean[] wasStmt = {false};
                    node1.walk(Node.TreeTraversal.PARENTS, node2 -> {
                        if (!wasStmt[0])
                        {
                            lst.add(node2.getMetaModel().getTypeNameGenerified());
                        }
                        if (node2 instanceof ExpressionStmt)
                            wasStmt[0] = true;
                    });
                    variableHash.put(key, lst);
                }
            });
            stmtCounter++;
        }
        for (Map.Entry<String, List<String> > entry : variableHash.entrySet())
        {
            String varName = entry.getKey();
            List<String> hash = entry.getValue();
            List<String> list;
            if (varMap.containsKey(hash))
                list = varMap.get(hash);
            else
                list = new ArrayList<>();
            list.add(varName);
            varMap.put(hash, list);
        }
        Map<Integer, List<String> > ofWhichNode = new HashMap<>();
        for (List<String> commonVariables : varMap.values())
        {
            ofWhichNode.clear();
            for (String variable : commonVariables)
            {
                Integer key = Integer.parseInt(variable.split(";")[0]);
                List<String> list;
                if (ofWhichNode.containsKey(key))
                    list = ofWhichNode.get(key);
                else
                    list = new ArrayList<>();
                list.add(variable);
                ofWhichNode.put(key, list);
            }
            int minSize = Integer.MAX_VALUE;
            if (ofWhichNode.size()<nodes.size())
                continue;
            for (Map.Entry<Integer, List<String> > entry : ofWhichNode.entrySet())
            {
                minSize = Math.min(minSize, entry.getValue().size());
            }
            for (Map.Entry<Integer, List<String> > entry : ofWhichNode.entrySet())
            {
                List<String> list = entry.getValue();
                for (int j=0; j<minSize; j++)
                {
                    System.out.printf("%s ", list.get(j));
                }
                System.out.println();
            }
        }
        exprMap.clear();
    }
}