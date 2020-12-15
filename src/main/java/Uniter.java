import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.utils.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import java.io.File;
import java.util.*;

public class Uniter {
    static Map<String, Integer> wasParameterUsed = new HashMap<>();
    static List<Pair<String, Type>> parameterNames = new ArrayList<>();

    static void exportBigEval(List<List<List<Pair<Integer, Integer>>>> duplicatedSegments, File file) {
        StringBuilder s = new StringBuilder();
        Set<Integer> usedNodeIDs = new TreeSet<>();

        for (var segmList : duplicatedSegments) {
            List<Quartet<String, String, Integer, Integer>> clonesList = new ArrayList<>();
            for (var segm : segmList) {
                final int[] startLine = {Integer.MAX_VALUE};
                final int[] endLine = {Integer.MIN_VALUE};
                final String[] subDirectory = {null};
                final String[] fileName = {null};
                segm.forEach((blockPiece) -> {
                    int blockRootId = Main.blocks.get(blockPiece.a).root.getData(Main.NODE_ID);
                    Node node = Main.blocks.get(blockPiece.a).list.get(blockPiece.b).node;
                    final Node[] prevNode = {node};
                    final Node[] nodeToAdd = {null};
                    node.walk(Node.TreeTraversal.PARENTS, (potentialParent) -> {
                        if (potentialParent.containsData(Main.NODE_ID) &&
                                potentialParent.getData(Main.NODE_ID).equals(blockRootId))
                            nodeToAdd[0] = prevNode[0];
                        prevNode[0] = potentialParent;
                    });
                    if (nodeToAdd[0] != null && !usedNodeIDs.contains(nodeToAdd[0].getData(Main.NODE_ID))) {
                        if (nodeToAdd[0].getRange().isPresent()) {
                            startLine[0] = Math.min(startLine[0], nodeToAdd[0].getRange().get().begin.line);
                            endLine[0] = Math.max(endLine[0], nodeToAdd[0].getRange().get().end.line);
                        }
                        if (subDirectory[0] == null)
                            subDirectory[0] = nodeToAdd[0].getData(Main.SUBDIRECTORY);
                        if (fileName[0] == null)
                            fileName[0] = nodeToAdd[0].getData(Main.FILE_NAME);
                        usedNodeIDs.add(nodeToAdd[0].getData(Main.NODE_ID));
                    }
                });
                if (startLine[0] != Integer.MAX_VALUE && startLine[0] != endLine[0]) {
                    clonesList.add(new Quartet<>(subDirectory[0], fileName[0], startLine[0], endLine[0]));
                }
            }
            for (int i = 0; i < clonesList.size(); i++) {
                Quartet<String, String, Integer, Integer> cloneI = clonesList.get(i);
                if (cloneI.getValue3()-cloneI.getValue2()<Main.minimumCloneLineAmount)
                    continue;
                for (int j = i + 1; j < clonesList.size(); j++) {
                    Quartet<String, String, Integer, Integer> cloneJ = clonesList.get(j);
                    if (cloneJ.getValue3()-cloneJ.getValue2()<Main.minimumCloneLineAmount)
                        continue;
                    if (!(cloneI.getValue0() + cloneI.getValue1())
                            .equals(cloneJ.getValue0() + cloneJ.getValue1()) ||
                            Utils.dontIntersect(cloneI.getValue2(), cloneI.getValue3(),
                                    cloneJ.getValue2(), cloneJ.getValue3())) {
                        for (int k=0; k<4; k++) {
                            s.append(cloneI.getValue(k));
                            s.append(",");
                        }
                        for (int k=0; k<4; k++) {
                            s.append(cloneJ.getValue(k));
                            s.append((k!=3) ? "," : "\n");
                        }
                    }
                }
            }
        }
        Utils.writeToFile(s.toString(), file);
    }

    static ClassOrInterfaceDeclaration makeMethod(List<List<List<Pair<Integer, Integer>>>> duplicatedSegments) {
        ClassOrInterfaceDeclaration copyClass = new ClassOrInterfaceDeclaration();
        copyClass.setName("Copied");
        copyClass.setPublic(true);
        Set<Integer> usedNodeIDs = new TreeSet<>();
        for (var segmList : duplicatedSegments) {
            List<List<Node>> segmentsInNodes = new ArrayList<>();
            for (var segm : segmList) {
                List<Node> segmentInNodes = new ArrayList<>();
                segm.forEach((blockPiece) -> {
                    int blockRootId = Main.blocks.get(blockPiece.a).root.getData(Main.NODE_ID);
                    Node node = Main.blocks.get(blockPiece.a).list.get(blockPiece.b).node;
                    final Node[] prevNode = {node};
                    final Node[] nodeToAdd = {null};
                    node.walk(Node.TreeTraversal.PARENTS, (potentialParent) -> {
                        if (potentialParent.containsData(Main.NODE_ID) &&
                                potentialParent.getData(Main.NODE_ID).equals(blockRootId))
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
            boolean hasBrokenSegment = false;
            for (List<Node> segmentsInNode : segmentsInNodes)
                if (segmentsInNode.size() == 0) {
                    hasBrokenSegment = true;
                    break;
                }
            if (!hasBrokenSegment) {
                replaceAllStatics(segmentsInNodes);
                var paramsAndMethod = actuallyMakeMethod(segmentsInNodes);
                if (paramsAndMethod != null) {
                    copyClass.addMethod("garbage").replace(paramsAndMethod.b);
                    for (int segment = 0; segment < segmentsInNodes.size(); segment++) {
                        MethodCallExpr methodCallExpr = new MethodCallExpr();
                        methodCallExpr.setName(new SimpleName(paramsAndMethod.b.getName().asString()));
                        methodCallExpr.setScope(new NameExpr("Copied"));
                        for (Parameter parameter : paramsAndMethod.b.getParameters())
                            methodCallExpr.addArgument(paramsAndMethod.a.get(segment).get(parameter.getNameAsString()));
                        /// supposing that segment has same root
                        for (int nodeIndex = 0; nodeIndex < segmentsInNodes.get(segment).size(); nodeIndex++) {
                            Node node = segmentsInNodes.get(segment).get(nodeIndex);
                            if (node.getRange().isPresent())
                                Main.duplicateLines += node.getRange().get().getLineCount();
                            if (nodeIndex == 0)
                                node.replace(new ExpressionStmt().setExpression(methodCallExpr));
                            else
                                node.remove();
                        }
                    }
                }
            }
        }
        return copyClass;
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
                    JavaParserFacade.clearInstances();
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
                    } catch (Exception ignored) {
                    } ///TODO not ignore the exception
                });
                node.walk(FieldAccessExpr.class, fieldAccessExpr -> {
                    try {
                        JavaParserFacade.clearInstances();
                        if (fieldAccessExpr.getScope().toString().equals(""))
                            fieldAccessExpr.setScope(new NameExpr(className[0]));
                        else if (!fieldAccessExpr.getScope().toString().startsWith(className[0]))
                            fieldAccessExpr.setScope(
                                    new FieldAccessExpr().
                                            setScope(new NameExpr(className[0])).
                                            setName(fieldAccessExpr.getScope().toString())
                            );
                    } catch (Exception ignored) {
                    } ///TODO not ignore the exception
                });
                node.walk(NameExpr.class, nameExpr -> {
                    try {
                        if (nameExpr.resolve().isField() && !className[0].equals("")) {
                            nameExpr.replace(new FieldAccessExpr().setScope(new NameExpr(className[0])).
                                    setName(nameExpr.getName()));
                        }
                    } catch (Exception ignored) {
                    } ///TODO not ignore the exception
                });
            }
        }
    }

    static String makeMethodName(List<List<Node>> segmentList) {
        StringBuilder methodNameBuilder = new StringBuilder();
        final int[] cnt = {1};
        for (List<Node> nodes : segmentList) {
            Node node = nodes.get(0);
            List<Node> parentsAndNode = new ArrayList<>();
            parentsAndNode.add(node);
            node.walk(Node.TreeTraversal.PARENTS, parentsAndNode::add);
            for (Node node1 : parentsAndNode) {
                String funcName = "";
                if (node1 instanceof MethodDeclaration)
                    funcName = ((MethodDeclaration) node1).getNameAsString();
                else if (node1 instanceof ClassOrInterfaceDeclaration)
                    funcName = ((ClassOrInterfaceDeclaration) node1).getNameAsString();
                else if (node1 instanceof LocalClassDeclarationStmt)
                    funcName = ((LocalClassDeclarationStmt) node1).getClassDeclaration().getNameAsString();
                if (cnt[0] <= 2 && !funcName.equals("")) {
                    if (methodNameBuilder.length() != 0) {
                        methodNameBuilder.append("And");
                        funcName = funcName.substring(0, 1).toUpperCase() + funcName.substring(1);
                    }
                    methodNameBuilder.append(funcName);
                    cnt[0]++;
                }
            }
        }
        return methodNameBuilder.toString();
    }

    static void checkAndChangeLiteralExprs(List<List<Pair<Integer, Integer>>> similarCommands, List<List<Node>> segmentList, List<Map<String, LiteralExpr>> paramsForEachSegment) {
        for (List<Pair<Integer, Integer>> similarCommand : similarCommands) {
            List<List<LiteralExpr>> literalExprs = new ArrayList<>();
            for (Pair<Integer, Integer> nodeIndices : similarCommand) {
                Node node = segmentList.get(nodeIndices.a).get(nodeIndices.b);
                literalExprs.add(new ArrayList<>());
                node.walk(LiteralExpr.class, literalExprs.get(literalExprs.size() - 1)::add);
            }
            int minLiteralSize = Integer.MAX_VALUE;
            for (int commandIndex = 0; commandIndex < similarCommand.size(); commandIndex++)
                minLiteralSize = Math.min(minLiteralSize, literalExprs.get(commandIndex).size());
            for (int literalIndex = 0; literalIndex < minLiteralSize; literalIndex++) {
                Set<String> values = new HashSet<>();
                Type type = null;
                for (int commandIndex = 0; commandIndex < similarCommand.size(); commandIndex++) {
                    values.add(literalExprs.get(commandIndex).get(literalIndex).toString());
                    String typeInString = literalExprs.get(commandIndex).get(literalIndex).getMetaModel().getTypeNameGenerified();
                    typeInString = typeInString.replace("LiteralExpr", "");
                    if (!typeInString.equals("null"))
                        type = StaticJavaParser.parseType(typeInString);
                }
                if (values.size() != 1) {
                    String name = "";
                    if (literalExprs.get(0).get(literalIndex).getParentNode().isPresent())
                        name = Utils.makeNameFromNode(literalExprs.get(0).get(literalIndex).getParentNode().get(), "");
                    if (name.equals(""))
                        name = "literal";
                    else
                        name = "literal" + name.substring(0, 1).toUpperCase() + name.substring(1);
                    name = checkAndChangeParameter(name);
                    parameterNames.add(new Pair<>(name, type));
                    for (int commandIndex = 0; commandIndex < similarCommand.size(); commandIndex++) {
                        literalExprs.get(commandIndex).get(literalIndex).replace(new NameExpr(name));
                        Integer segmentIndex = similarCommand.get(commandIndex).a;
                        paramsForEachSegment.get(segmentIndex).put(name, literalExprs.get(commandIndex).get(literalIndex));
                    }
                }
            }
        }
    }

    static List<List<Pair<Integer, Integer>>> findSimilarCommands(List<List<Node>> segmentList) {
        List<Integer> lastCommonNodeInSegment = new ArrayList<>();
        for (int i = 0; i < segmentList.size(); i++)
            lastCommonNodeInSegment.add(-1);
        List<List<Pair<Integer, Integer>>> similarCommands = new ArrayList<>();
        for (int nodeIndex = 0; nodeIndex < segmentList.get(0).size(); nodeIndex++) {
            List<Pair<Integer, Integer>> similarCommand = new ArrayList<>();
            for (int segment = 1; segment < segmentList.size(); segment++) {
                for (int k = Math.max(lastCommonNodeInSegment.get(segment) + 1, nodeIndex - 3);
                     k < Math.min(nodeIndex + 4, segmentList.get(segment).size()); k++) {
                    if (Utils.hashNode(segmentList.get(segment).get(k)) ==
                            Utils.hashNode(segmentList.get(0).get(nodeIndex))) {
                        if (similarCommand.size() == 0)
                            similarCommand.add(new Pair<>(0, nodeIndex));
                        similarCommand.add(new Pair<>(segment, k));
                        lastCommonNodeInSegment.set(segment, k);
                        break;
                    }
                }
            }
            if (similarCommand.size() == segmentList.size())
                similarCommands.add(similarCommand);
        }
        return similarCommands;
    }

    static String checkAndChangeParameter(String parameterName) {
        if (wasParameterUsed.getOrDefault(parameterName, 0).equals(0))
            wasParameterUsed.put(parameterName, 1);
        else {
            wasParameterUsed.put(parameterName, wasParameterUsed.get(parameterName) + 1);
            parameterName += wasParameterUsed.get(parameterName);
        }
        return parameterName;
    }

    static Pair<List<Map<String, LiteralExpr>>, MethodDeclaration> actuallyMakeMethod(List<List<Node>> segmentList) {
        if (segmentList.size() < 2)
            return null;
        int summaryNodesCountInBeginning = 0;
        int summaryNodesCountInEnd = 0;
        for (List<Node> l : segmentList)
            summaryNodesCountInBeginning += l.size();
        wasParameterUsed.clear();
        parameterNames.clear();
        boolean[][] isSimilarToEveryone = new boolean[segmentList.size()][];
        /// how much similar before, than list of commands between similar command, each non-similar commands
        // is defined by 2 indices and how much non-similar before it
        List<Map<String, LiteralExpr>> paramsForEachSegment = new ArrayList<>();
        List<List<Triplet<Integer, Integer, Integer>>> notSimilarToEveryoneCommands = new ArrayList<>();
        List<Node> methodCommands = new ArrayList<>();
        for (int segment = 0; segment < segmentList.size(); segment++) {
            isSimilarToEveryone[segment] = new boolean[segmentList.get(segment).size()];
            paramsForEachSegment.add(new HashMap<>());
        }
        List<List<Pair<Integer, Integer>>> similarCommands = findSimilarCommands(segmentList);
        checkAndChangeLiteralExprs(similarCommands, segmentList, paramsForEachSegment);
        for (var similarCommand : similarCommands)
            for (var segmentNodeIndex : similarCommand)
                isSimilarToEveryone[segmentNodeIndex.a][segmentNodeIndex.b] = true;
        for (int i = 0; i <= similarCommands.size(); i++)
            notSimilarToEveryoneCommands.add(new ArrayList<>());
        for (int segmentIndex = 0; segmentIndex < segmentList.size(); segmentIndex++) {
            int similarBefore = 0;
            int notSimilarBefore = 0;
            for (int nodeIndex = 0; nodeIndex < segmentList.get(segmentIndex).size(); nodeIndex++) {
                if (!isSimilarToEveryone[segmentIndex][nodeIndex]) {
                    notSimilarToEveryoneCommands.get(similarBefore).add(new Triplet<>(segmentIndex, nodeIndex,
                            notSimilarBefore));
                    notSimilarBefore++;
                } else {
                    similarBefore++;
                    notSimilarBefore = 0;
                }
            }
        }
        int doParameters = 0;
        for (int pos = 0; pos <= similarCommands.size(); pos++) {
            if (pos != similarCommands.size()) {
                Pair<Integer, Integer> commandIndexes = similarCommands.get(pos).get(0);
                methodCommands.add(segmentList.get(commandIndexes.a).get(commandIndexes.b));
                summaryNodesCountInEnd++;
            }
            /// key -> how many "not similar" before, hash
            SortedMap<Pair<Integer, Integer>, Node> hashNodeMap = new TreeMap<>(new Utils.TypicalPairComparator());
            SortedMap<Pair<Integer, Integer>, List<Integer>> usingSegmentsMap = new TreeMap<>(new Utils.TypicalPairComparator());
            for (int command = 0; command < notSimilarToEveryoneCommands.get(pos).size(); command++) {
                Triplet<Integer, Integer, Integer> nodeIndexesNotSimilarBefore = notSimilarToEveryoneCommands.get(pos).get(command);
                Node node = segmentList.get(nodeIndexesNotSimilarBefore.getValue0()).get(nodeIndexesNotSimilarBefore.getValue1());
                Pair<Integer, Integer> key = new Pair<>(nodeIndexesNotSimilarBefore.getValue2(), Utils.hashNode(node, true));
                List<Integer> usingSegmentsList = usingSegmentsMap.getOrDefault(key, new ArrayList<>());
                usingSegmentsList.add(nodeIndexesNotSimilarBefore.getValue0());
                usingSegmentsMap.put(key, usingSegmentsList);
                hashNodeMap.put(key, node);
            }
            for (Map.Entry<Pair<Integer, Integer>, Node> keyValue : hashNodeMap.entrySet()) {
                String booleanParameterName = checkAndChangeParameter("do" + Utils.makeNameFromNode(keyValue.getValue()));
                parameterNames.add(new Pair<>(booleanParameterName, PrimitiveType.booleanType()));
                doParameters++;
                if (doParameters > Main.maxDoParametersCount)
                    return null;
                BlockStmt thenStmt = new BlockStmt();
                if (keyValue.getValue() instanceof Expression) {
                    Expression expr = (Expression) keyValue.getValue();
                    thenStmt.addStatement(expr.clone());
                }
                if (keyValue.getValue() instanceof Statement) {
                    Statement stmt = (Statement) keyValue.getValue();
                    thenStmt.addStatement(stmt.clone());
                }
                for (Integer segment : usingSegmentsMap.getOrDefault(keyValue.getKey(), Collections.emptyList()))
                    paramsForEachSegment.get(segment).put(booleanParameterName, new BooleanLiteralExpr().setValue(true));
                for (int segment = 0; segment < segmentList.size(); segment++)
                    paramsForEachSegment.get(segment).put(booleanParameterName, paramsForEachSegment.get(segment)
                            .getOrDefault(booleanParameterName, new BooleanLiteralExpr(false)));
                methodCommands.add(new IfStmt().setThenStmt(thenStmt).setCondition(new NameExpr(booleanParameterName)));
                summaryNodesCountInEnd += 2;
            }
        }
        if (summaryNodesCountInEnd >= summaryNodesCountInBeginning)
            return null;
        MethodDeclaration declaration = makeDeclarationFromParamsAndCommands(parameterNames, methodCommands, makeMethodName(segmentList));
        if (declaration == null)
            return null;
        return new Pair<>(paramsForEachSegment, declaration);
    }

    static MethodDeclaration makeDeclarationFromParamsAndCommands(List<Pair<String, Type>> parameterNames,
                                                                  List<Node> methodCommands,
                                                                  String methodName) {
        MethodDeclaration declaration = new MethodDeclaration();
        declaration.setStatic(true);
        NodeList<Parameter> parameterNodes = new NodeList<>();
        BlockStmt body = new BlockStmt();
        for (var parameterName : parameterNames)
            parameterNodes.add(new Parameter().setName(parameterName.a).setType(parameterName.b));
        for (var command : methodCommands) {
            if (command instanceof Statement)
                body.addStatement((Statement) command.clone());
            else
                body.addStatement((Expression) command.clone());
        }
        declaration.setParameters(parameterNodes);
        declaration.setBody(body);
        declaration.setType(new VoidType());
        if (methodName.equals(""))
            return null;
        declaration.setName(methodName);
        return declaration;
    }

}
