import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
        }
    }

    static void replaceAllStatics(List<List<Node>> segment) {
        for (List<Node> nodeList : segment) {
            for (Node node : nodeList) {
                final String[] className = {""};
                node.walk(Node.TreeTraversal.PARENTS, parent -> {
                    if (parent instanceof ClassOrInterfaceDeclaration) {
                        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = ((ClassOrInterfaceDeclaration) parent);
                        className[0] = classOrInterfaceDeclaration.getNameAsString();
                    }
                });
                node.walk(MethodCallExpr.class, methodCallExpr -> {
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
                });
                node.walk(FieldAccessExpr.class, fieldAccessExpr -> {
                    if (fieldAccessExpr.getScope().toString().equals(""))
                        fieldAccessExpr.setScope(new NameExpr(className[0]));
                    else if (!fieldAccessExpr.getScope().toString().startsWith(className[0]))
                        fieldAccessExpr.setScope(
                                new FieldAccessExpr().
                                        setScope(new NameExpr(className[0])).
                                        setName(fieldAccessExpr.getScope().toString())
                        );
                });
                node.walk(NameExpr.class, nameExpr -> {
                    try {
                        if (nameExpr.resolve().isField()) {
                            nameExpr.replace(new FieldAccessExpr().setScope(new NameExpr(className[0])).setName(nameExpr.getName()));
                        }
                    } catch (Exception ignored) {
                    }
                });
            }
        }
        System.out.println(segment);
    }

}
