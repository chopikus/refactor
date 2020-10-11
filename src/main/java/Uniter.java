import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.Pair;

import java.util.*;

public class Uniter {

    static void makeMethod(List<List<List<Pair<Integer, Integer>>>> duplicatedSegments) {
        Set<Integer> usedNodeIDs = new TreeSet<>();
        for (var segmList : duplicatedSegments) {
            List<List<Node>> segmentsInNodes = new ArrayList<>();
            for (var segm : segmList) {
                List<Node> segmentInNodes = new ArrayList<Node>();
                segm.forEach((blockPiece) -> {
                    int blockRootId = Main.blocks.get(blockPiece.a).root.getData(Main.NODE_ID);
                    Node node = Main.blocks.get(blockPiece.a).list.get(blockPiece.b).node;
                    final Node[] prevNode = {node};
                    final Node[] nodeToAdd = {null};
                    node.walk(Node.TreeTraversal.PARENTS, (potentialParent) -> {
                        if (potentialParent.getData(Main.NODE_ID).equals(blockRootId))
                            nodeToAdd[0] = prevNode[0];
                        prevNode[0] = potentialParent;
                    });
                    if (nodeToAdd[0] != null && !usedNodeIDs.contains(nodeToAdd[0].getData(Main.NODE_ID))) {
                        segmentInNodes.add(nodeToAdd[0]);
                        usedNodeIDs.add(nodeToAdd[0].getData(Main.NODE_ID));
                    }
                });
                segmentsInNodes.add(segmentInNodes);
            }
            replaceAllStatics(segmentsInNodes);
            makeMethodBody(segmentsInNodes);
        }
    }

    static void replaceAllStatics(List<List<Node>> segments) {
        for (List<Node> nodeList : segments) {
            for (Node node : nodeList) {
                final String[] className = {""};
                node.walk(Node.TreeTraversal.PARENTS, parent -> {
                    if (parent instanceof ClassOrInterfaceDeclaration) {
                        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = ((ClassOrInterfaceDeclaration) parent);
                        className[0] = classOrInterfaceDeclaration.getNameAsString();
                    }
                });
                node.walk(MethodCallExpr.class, methodCallExpr -> {
                    try {
                        if (!methodCallExpr.resolve().getPackageName().startsWith("java")) {
                            if (methodCallExpr.getScope().isEmpty())
                                methodCallExpr.setScope(new NameExpr(methodCallExpr.resolve().getClassName()));
                            else if (!methodCallExpr.getScope().get().toString().startsWith(className[0]))
                                methodCallExpr.setScope(
                                        new FieldAccessExpr().
                                                setScope(new NameExpr(className[0])).
                                                setName(methodCallExpr.getScope().get().toString())
                                );
                        }
                    }
                    catch (Exception ignored) {} ///TODO not ignore the exception
                });
                node.walk(FieldAccessExpr.class, fieldAccessExpr -> {
                    try {
                        if (fieldAccessExpr.getScope().toString().equals(""))
                            fieldAccessExpr.setScope(new NameExpr(className[0]));
                        else if (!fieldAccessExpr.getScope().toString().startsWith(className[0]))
                            fieldAccessExpr.setScope(
                                    new FieldAccessExpr().
                                            setScope(new NameExpr(className[0])).
                                            setName(fieldAccessExpr.getScope().toString())
                            );
                    }
                    catch (Exception ignored) {} ///TODO not ignore the exception
                });
                node.walk(NameExpr.class, nameExpr -> {
                    try {
                        if (nameExpr.resolve().isField()) {
                            nameExpr.replace(new FieldAccessExpr().setScope(new NameExpr(className[0])).
                                    setName(nameExpr.getName()));
                        }
                    } catch (Exception ignored) {} ///TODO not ignore the exception
                });
            }
        }
    }
    static void makeMethodBody(List<List<Node>> segmentList)
    {
        if (segmentList.size()<2)
            return;
        List<Integer> lastCommonNodeInSegment = new ArrayList<>();
        List<List<Boolean>> isSimilarToEveryone = new ArrayList<>();
        Map<String, Integer> wasParameterNameUsed = new HashMap<>();
        for (int i=0; i<segmentList.size(); i++) {
            lastCommonNodeInSegment.add(-1);
            isSimilarToEveryone.add(new ArrayList<>());
            for (int j=0; j<segmentList.get(i).size(); j++)
                isSimilarToEveryone.get(isSimilarToEveryone.size()-1).add(false);
        }
        List<List<Pair<Integer, Integer>>> similarCommands = new ArrayList<>();
        for (int nodeIndex = 0; nodeIndex<segmentList.get(0).size(); nodeIndex++) {
            List<Pair<Integer, Integer> > similarCommand = new ArrayList<>();
            for (int segment=1; segment<segmentList.size(); segment++) {
                for (int k=Math.max(lastCommonNodeInSegment.get(segment)+1, nodeIndex-3);
                     k<Math.min(nodeIndex+4, segmentList.get(segment).size()); k++) {
                    if (Utils.hashNode(segmentList.get(segment).get(k))==
                            Utils.hashNode(segmentList.get(0).get(nodeIndex))) {
                        if (similarCommand.size()==0)
                            similarCommand.add(new Pair<>(0, nodeIndex));
                        similarCommand.add(new Pair<>(segment, k));
                        lastCommonNodeInSegment.set(segment, k);
                        break;
                    }
                }
            }
            if (similarCommand.size()!=0)
                similarCommands.add(similarCommand);
        }
        for (var similarCommand : similarCommands) {
            if (similarCommand.size()==segmentList.size()) {
                for (var segmentNodeIndex : similarCommand) {
                    isSimilarToEveryone.get(segmentNodeIndex.a).set(segmentNodeIndex.b, true);
                }
            }
        }
        List<List<Node>> notSimilarToEveryoneCommands = new ArrayList<>();
        List<Node> methodCommands = new ArrayList<>();
        for (int i=0; i<=similarCommands.size(); i++)
            notSimilarToEveryoneCommands.add(new ArrayList<>());
        for (int segment=0; segment<segmentList.size(); segment++) {
            int cnt=0;
            for (int nodeIndex=0; nodeIndex<segmentList.get(segment).size(); nodeIndex++) {
                if (!isSimilarToEveryone.get(segment).get(nodeIndex)) {
                    notSimilarToEveryoneCommands.get(cnt).add(segmentList.get(segment).get(nodeIndex));
                }
                else
                    cnt++;
            }
        }
        for (int pos=0; pos<=similarCommands.size(); pos++)
        {
            if (pos!=similarCommands.size()){
                Pair<Integer, Integer> commandIndexes = similarCommands.get(pos).get(0);
                methodCommands.add(segmentList.get(commandIndexes.a).get(commandIndexes.b));
            }
            Map<Integer, Node> hashNodeMap = new TreeMap<>();
            for (int command=0; command<notSimilarToEveryoneCommands.get(pos).size(); command++)
            {
                Node node = notSimilarToEveryoneCommands.get(pos).get(command);
                hashNodeMap.put(Utils.hashNode(node), node);
            }
            for (Map.Entry<Integer, Node> hashNode : hashNodeMap.entrySet())
            {
                BlockStmt thenStmt = new BlockStmt();
                String booleanParameterName = "do"+hashNode.getValue().toString().replaceAll("[.;]", "").
                        split("[()]")[0];
                if (wasParameterNameUsed.getOrDefault(booleanParameterName, 0).equals(0))
                    wasParameterNameUsed.put(booleanParameterName, 1);
                else {
                    wasParameterNameUsed.put(booleanParameterName, wasParameterNameUsed.get(booleanParameterName) + 1);
                    booleanParameterName+="_"+wasParameterNameUsed.get(booleanParameterName);
                }
                if (hashNode.getValue() instanceof Expression) {
                    Expression expr = (Expression) hashNode.getValue();
                    thenStmt.addStatement(expr);
                }
                if (hashNode.getValue() instanceof Statement) {
                    Statement stmt = (Statement) hashNode.getValue();
                    thenStmt.addStatement(stmt);
                }
                methodCommands.add(new IfStmt().setThenStmt(thenStmt).setCondition(new NameExpr(booleanParameterName)));
            }
        }
        System.out.println(methodCommands);
    }
}
