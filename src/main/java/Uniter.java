import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.Statement;

import java.util.*;

public class Uniter {

    static List<Boolean> getCommonStmts(List<Statement> stmts, List<Integer> stmtToNode, int nodesSize, boolean hashWithVariableNames)
    {
        Map<List<String>, List<Integer>> stmtMap = new HashMap<>();
        List<Boolean> result = new ArrayList<>();
        for (int i=0; i<stmts.size(); i++)
            result.add(false);
        int stmtCounter=0;
        for (Statement stmt : stmts)
        {
            List<String> dfsed = new ArrayList<String>();
            int finalStmtCounter = stmtCounter;
            stmt.walk(node -> {
                if (!node.getData(Main.IS_VARIABLE) && !(node instanceof LiteralExpr))
                    dfsed.add(node.getMetaModel().getTypeNameGenerified());
                if (hashWithVariableNames && node.getData(Main.IS_VARIABLE))
                    dfsed.add(stmtToNode.get(finalStmtCounter)+";"+node.toString());
            });
            List<Integer> list;
            if (!stmtMap.containsKey(dfsed)) {
                list = new ArrayList<>();
            }
            else
                list = stmtMap.get(dfsed);
            list.add(stmtCounter);
            stmtMap.put(dfsed, list);
            stmtCounter++;
        }
        Map<Integer, List<Integer> > ofWhichNode = new HashMap<>();
        for (Map.Entry<List<String>, List<Integer>> entry : stmtMap.entrySet())
        {
            ofWhichNode.clear();
            Set<Integer> nodeSet = new HashSet<>();
            for (Integer stmtNumber : entry.getValue()) {
                int nodeNum = stmtToNode.get(stmtNumber);
                List<Integer> lst;
                if (ofWhichNode.containsKey(nodeNum))
                    lst = ofWhichNode.get(nodeNum);
                else
                    lst = new ArrayList<>();
                lst.add(stmtNumber);
                ofWhichNode.put(nodeNum, lst);
                nodeSet.add(nodeNum);
            }
            if (nodeSet.size()==nodesSize)
            {
                int mn = Integer.MAX_VALUE;
                for (List<Integer> value: ofWhichNode.values())
                    mn = Math.min(mn, value.size());
                for (List<Integer> value: ofWhichNode.values())
                    for (int i=0; i<mn; i++)
                        result.set(value.get(i), true);
            }
        }
        return result;
    }

    static void unite(List<Node> nodes)
    {
        List<Statement> stmts = new ArrayList<>();
        List<Integer> stmtToNode = new ArrayList<>();
        Map<String, List<String> > variableHash = new HashMap<>();
        Map<List<String>, List<String> > varMap = new HashMap<>();
        List<Boolean> isCommonStmt;
        int nodeCounter=0;
        for (Node node : nodes)
        {
            Set<String> variableNames = new HashSet<>();
            int finalNodeCounter = nodeCounter;
            node.walk(Node.TreeTraversal.PREORDER, stmt -> {
                if (stmt instanceof Statement) {
                    stmts.add((Statement) stmt);
                    stmtToNode.add(finalNodeCounter);
                }
                if (stmt instanceof VariableDeclarator)
                {
                    VariableDeclarator variableDecl = (VariableDeclarator) stmt;
                    System.out.println("DECL: "+variableDecl);
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
        isCommonStmt = (List<Boolean>) getCommonStmts(stmts, stmtToNode, nodes.size(), false);
        int stmtCounter = 0;
        for (Statement stmt : stmts)
        {
            if (isCommonStmt.get(stmtCounter))
            {
                int finalStmtCounter = stmtCounter;
                stmt.walk(node1 -> {
                    if (node1.getData(Main.IS_VARIABLE)) {
                        String key = stmtToNode.get(finalStmtCounter) + ";" + node1.toString();
                        List<String> lst;
                        if (!variableHash.containsKey(key))
                            lst = new ArrayList<>();
                        else
                            lst = variableHash.get(key);
                        final boolean[] wasStmt = {false};
                        node1.walk(Node.TreeTraversal.PARENTS, node2 -> {
                            if (!wasStmt[0]) {
                                if (node2 instanceof BinaryExpr)
                                    lst.add(((BinaryExpr) node2).getOperator().asString());
                                else if (node2 instanceof UnaryExpr)
                                    lst.add(((UnaryExpr) node2).getOperator().asString());
                                lst.add(node2.getMetaModel().getTypeNameGenerified());
                            }
                            if (node2 instanceof Statement)
                                wasStmt[0] = true;
                        });
                        variableHash.put(key, lst);
                    }
                });
            }
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
        Set<String> reallyCommonVariables = new HashSet<>();
        for (List<String> commonVariables : varMap.values())
        {
            System.out.println(commonVariables);
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
                for (int j=0; j<minSize; j++) {
                    reallyCommonVariables.add(list.get(j));
                    System.out.print(list.get(j)+" ");
                }
                System.out.println();
            }
        }
        nodeCounter = 0;
        for (Node node : nodes)
        {
            int finalNodeCounter = nodeCounter;
            node.walk(node1 -> {
                if (node1.getData(Main.IS_VARIABLE) && reallyCommonVariables.contains(finalNodeCounter+";"+node1.toString()))
                    node1.setData(Main.IS_VARIABLE, false);
            });
            nodeCounter++;
        }
        isCommonStmt = getCommonStmts(stmts, stmtToNode, nodes.size(), true);
        for (stmtCounter = 0; stmtCounter<stmts.size(); stmtCounter++)
        {
            System.out.println("EXPR: "+stmts.get(stmtCounter) + " " + isCommonStmt.get(stmtCounter));
        }
    }
}