import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.VoidType;
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
            actuallyMakeMethod(segmentsInNodes);
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
    static String makeMethodName(List<List<Node>> segmentList)
    {
        StringBuilder methodNameBuilder = new StringBuilder();
        for (int segment=0; segment<segmentList.size(); segment++)
        {
            Node node = segmentList.get(segment).get(0);
            node.walk(Node.TreeTraversal.PARENTS, node1 -> {
                if (node1 instanceof MethodDeclaration)
                {
                    String funcName = ((MethodDeclaration) node1).getNameAsString();
                    if (methodNameBuilder.length()!=0) {
                        methodNameBuilder.append("And");
                        funcName = funcName.substring(0, 1).toUpperCase()+funcName.substring(1);
                    }
                    methodNameBuilder.append(funcName);
                }
            });
        }
        return methodNameBuilder.toString();
    }

    static MethodDeclaration actuallyMakeMethod(List<List<Node>> segmentList)
    {
        if (segmentList.size()<2)
            return null;
        List<Integer> lastCommonNodeInSegment = new ArrayList<>();
        List<List<Boolean>> isSimilarToEveryone = new ArrayList<>();
        Map<String, Integer> wasParameterNameUsed = new HashMap<>();
        List<String> parameterNames = new ArrayList<>();
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
        for (int segmentIndex=0; segmentIndex<segmentList.size(); segmentIndex++) {
            int cnt=0;
            for (int nodeIndex=0; nodeIndex<segmentList.get(segmentIndex).size(); nodeIndex++) {
                if (!isSimilarToEveryone.get(segmentIndex).get(nodeIndex)) {
                    notSimilarToEveryoneCommands.get(cnt).add(segmentList.get(segmentIndex).get(nodeIndex));
                }
                else
                    cnt++;
            }
        }

        for (int pos=0; pos<similarCommands.size(); pos++)
        {
            List<List<LiteralExpr>> literalExprs = new ArrayList<>();
            for (int commandIndex=0; commandIndex<similarCommands.get(pos).size(); commandIndex++)
            {
                Pair<Integer, Integer> nodeIndices = similarCommands.get(pos).get(commandIndex);
                Node node = segmentList.get(nodeIndices.a).get(nodeIndices.b);
                literalExprs.add(new ArrayList<>());
                node.walk(LiteralExpr.class, literalExprs.get(literalExprs.size()-1)::add);
            }
            int minLiteralSize = Integer.MAX_VALUE;
            for (int commandIndex=0; commandIndex<similarCommands.get(pos).size(); commandIndex++)
                minLiteralSize = Math.min(minLiteralSize, literalExprs.get(commandIndex).size());
            for (int literalIndex=0; literalIndex<minLiteralSize; literalIndex++) {
                Set<String> values = new HashSet<>();
                for (int commandIndex = 0; commandIndex < similarCommands.get(pos).size(); commandIndex++)
                    values.add(literalExprs.get(commandIndex).get(literalIndex).toString());
                if (values.size()!=1)
                    for (int commandIndex = 0; commandIndex < similarCommands.get(pos).size(); commandIndex++)
                        literalExprs.get(commandIndex).get(literalIndex).replace(new NameExpr("khui")); /// TODO give it a proper name
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
                String booleanParameterName = "do"+Utils.makeNameFromNode(hashNode.getValue());
                if (wasParameterNameUsed.getOrDefault(booleanParameterName, 0).equals(0))
                    wasParameterNameUsed.put(booleanParameterName, 1);
                else {
                    wasParameterNameUsed.put(booleanParameterName, wasParameterNameUsed.get(booleanParameterName) + 1);
                    booleanParameterName+="_"+wasParameterNameUsed.get(booleanParameterName);
                }
                parameterNames.add(booleanParameterName);
                if (hashNode.getValue() instanceof Expression) {
                    Expression expr = (Expression) hashNode.getValue();
                    thenStmt.addStatement(expr.clone());
                }
                if (hashNode.getValue() instanceof Statement) {
                    Statement stmt = (Statement) hashNode.getValue();
                    thenStmt.addStatement(stmt.clone());
                }
                methodCommands.add(new IfStmt().setThenStmt(thenStmt).setCondition(new NameExpr(booleanParameterName)));
            }
        }
        return makeDeclarationFromParamsAndCommands(parameterNames, methodCommands, makeMethodName(segmentList));
    }
    static MethodDeclaration makeDeclarationFromParamsAndCommands(List<String> parameterNames,
                                                                  List<Node> methodCommands,
                                                                  String methodName)
    {
        MethodDeclaration declaration = new MethodDeclaration();
        declaration.setStatic(true);
        NodeList<Parameter> parameterNodes = new NodeList<Parameter>();
        BlockStmt body = new BlockStmt();
        for (var parameterName : parameterNames)
            parameterNodes.add(new Parameter().setName(parameterName).setType(PrimitiveType.booleanType()));
        for (var command : methodCommands)
        {
            if (command instanceof Statement)
                body.addStatement((Statement) command);
            else
                body.addStatement((Expression) command);
        }
        declaration.setParameters(parameterNodes);
        declaration.setBody(body);
        declaration.setType(new VoidType());
        declaration.setName(methodName);
        System.out.println(declaration);
        return declaration;
    }

}
