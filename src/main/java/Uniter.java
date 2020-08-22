import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.utils.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Uniter {
    static CompilationUnit cu = new CompilationUnit();
    static ClassOrInterfaceDeclaration copied = cu.addClass("Copied");

    static boolean isStatement(Node node)
    {
        return (node instanceof Statement && node.containsData(Main.IS_DEPTH_ONE) && node.getData(Main.IS_DEPTH_ONE) 
                && !(node instanceof ExplicitConstructorInvocationStmt));
    }

    static List<Boolean> getCommonStmts(List<Statement> stmts, List<Integer> stmtToNode, int nodesSize, boolean hashVariables) {
        Map<List<String>, List<Integer>> stmtMap = new HashMap<>();
        List<Boolean> result = new ArrayList<>();
        for (int i = 0; i < stmts.size(); i++)
            result.add(false);
        int stmtCounter = 0;
        for (Statement stmt : stmts) {
            List<String> dfsed = new ArrayList<String>();
            int finalStmtCounter = stmtCounter;
            stmt.walk(node -> {
                if (!node.getData(Main.IS_VARIABLE) && !(node instanceof LiteralExpr))
                    dfsed.add(node.getMetaModel().getTypeNameGenerified());
                if (hashVariables && node.getData(Main.IS_VARIABLE))
                    dfsed.add(stmtToNode.get(finalStmtCounter) + ";" + node.toString());
            });
            List<Integer> list = stmtMap.containsKey(dfsed) ? stmtMap.get(dfsed) : new ArrayList<>();
            list.add(stmtCounter);
            stmtMap.put(dfsed, list);
            stmtCounter++;
        }
        Map<Integer, List<Integer>> ofWhichNode = new HashMap<>();
        for (Map.Entry<List<String>, List<Integer>> entry : stmtMap.entrySet()) {
            ofWhichNode.clear();
            Set<Integer> nodeSet = new HashSet<>();
            for (Integer stmtNumber : entry.getValue()) {
                int nodeNum = stmtToNode.get(stmtNumber);
                List<Integer> lst = ofWhichNode.containsKey(nodeNum) ? ofWhichNode.get(nodeNum) : new ArrayList<>();
                lst.add(stmtNumber);
                ofWhichNode.put(nodeNum, lst);
                nodeSet.add(nodeNum);
            }
            if (nodeSet.size() == nodesSize) {
                int mn = Integer.MAX_VALUE;
                for (List<Integer> value : ofWhichNode.values())
                    mn = Math.min(mn, value.size());
                for (List<Integer> value : ofWhichNode.values())
                    for (int i = 0; i < mn; i++)
                        result.set(value.get(i), true);
            }
        }
        return result;
    }

    static void uniteCode(List<Node> nodes, int which) {
        List<Statement> stmts = new ArrayList<>();
        List<Integer> stmtToNode = new ArrayList<>();
        Map<String, List<String>> variableHash = new HashMap<>();
        Map<List<String>, List<String>> varMap = new HashMap<>();
        List<Boolean> isCommonStmt;
        int nodeCounter = 0;
        for (Node node : nodes) {
            Set<String> variableNames = new HashSet<>();
            int finalNodeCounter = nodeCounter;
            node.walk(Node.TreeTraversal.DIRECT_CHILDREN, node1 -> {
                node1.setData(Main.IS_DEPTH_ONE, true);
            });
            node.walk(Node.TreeTraversal.PREORDER, stmt -> {
                if (isStatement(stmt)) {
                    stmts.add((Statement) stmt);
                    stmtToNode.add(finalNodeCounter);
                }
                if (stmt instanceof VariableDeclarator) {
                    VariableDeclarator variableDecl = (VariableDeclarator) stmt;
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
        isCommonStmt = getCommonStmts(stmts, stmtToNode, nodes.size(), false);
        int stmtCounter = 0;
        for (Statement stmt : stmts) {
            if (isCommonStmt.get(stmtCounter)) {
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
                            if (isStatement(node2))
                                wasStmt[0] = true;
                        });
                        variableHash.put(key, lst);
                    }
                });
            }
            stmtCounter++;
        }

        for (Map.Entry<String, List<String>> entry : variableHash.entrySet()) {
            String varName = entry.getKey();
            List<String> hash = entry.getValue();
            List<String> list = varMap.containsKey(hash) ? varMap.get(hash) : new ArrayList<>();
            list.add(varName);
            varMap.put(hash, list);
        }
        Map<Integer, List<String>> ofWhichNode = new HashMap<>();
        Set<String> reallyCommonVariables = new HashSet<>();
        for (List<String> commonVariables : varMap.values()) {
            ofWhichNode.clear();
            for (String variable : commonVariables) {
                Integer key = Integer.parseInt(variable.split(";")[0]);
                List<String> list = ofWhichNode.containsKey(key) ? ofWhichNode.get(key) : new ArrayList<>();
                list.add(variable);
                ofWhichNode.put(key, list);
            }
            int minSize = Integer.MAX_VALUE;
            if (ofWhichNode.size() < nodes.size())
                continue;
            for (Map.Entry<Integer, List<String>> entry : ofWhichNode.entrySet()) {
                minSize = Math.min(minSize, entry.getValue().size());
            }
            for (Map.Entry<Integer, List<String>> entry : ofWhichNode.entrySet()) {
                List<String> list = entry.getValue();
                for (int j = 0; j < minSize; j++)
                    reallyCommonVariables.add(list.get(j));
            }
        }
        nodeCounter = 0;
        for (Node node : nodes) {
            int finalNodeCounter = nodeCounter;
            node.walk(node1 -> {
                if (node1.getData(Main.IS_VARIABLE) && reallyCommonVariables.contains(finalNodeCounter + ";" + node1.toString()))
                    node1.setData(Main.IS_VARIABLE, false);
            });
            nodeCounter++;
        }
        isCommonStmt = getCommonStmts(stmts, stmtToNode, nodes.size(), true);
        int commonStatements = 0;
        for (stmtCounter=0; stmtCounter<stmts.size(); stmtCounter++)
        {
            if (stmtToNode.get(stmtCounter)==0 && isCommonStmt.get(stmtCounter))
                commonStatements++;
        }
        List<List<Pair<Statement, Integer>>> statementsToWrite = new ArrayList<>();
        List<Statement> firstNodeCommonStatements = new ArrayList<>();
        List<LiteralExpr> firstNodeLiteralExprs = new ArrayList<>();
        for (int i=0; i<=commonStatements; i++)
            statementsToWrite.add(new ArrayList<>());
        int prevNode = -1;
        AtomicInteger literalExprCounter= new AtomicInteger();
        List<ResolvedType> literalTypes = new ArrayList<>();
        int howManyCommon = 0;
        stmtCounter = 0;
        for (Statement stmt : stmts)
        {
            int thisNode = stmtToNode.get(stmtCounter);
            if (thisNode!=prevNode)
                howManyCommon = 0;
            if (isCommonStmt.get(stmtCounter)) {
                if (thisNode==0) {
                    firstNodeCommonStatements.add(stmt);
                    stmt.findAll(LiteralExpr.class).forEach(node1->{
                        literalExprCounter.getAndIncrement();
                        ResolvedType type = node1.calculateResolvedType();
                        literalTypes.add(type);
                        firstNodeLiteralExprs.add(node1);
                        node1.replace(new NameExpr("commonLiteral"+literalExprCounter.get()));
                    });
                }
                howManyCommon++;
            }
            else
                statementsToWrite.get(howManyCommon).add(new Pair<>(stmt, thisNode));
            prevNode = thisNode;
            stmtCounter++;
        }
        List<Pair<Statement, Integer>> statementsToActuallyWrite = new ArrayList<>();
        for (int i=0; i<=commonStatements; i++)
        {
            statementsToActuallyWrite.addAll(statementsToWrite.get(i));
            if (i!=commonStatements)
                statementsToActuallyWrite.add(new Pair<>(firstNodeCommonStatements.get(i), -1));
        }
        writeInOneMethod(statementsToActuallyWrite, literalTypes, which);
        AtomicInteger atomicStmtCounter= new AtomicInteger();
        final List<Boolean> finalIsCommonStmt = isCommonStmt;
        nodeCounter = 0;
        for (Node node : nodes)
        {
            final List<LiteralExpr> lst = new ArrayList<>();
            if (nodeCounter==0)
                lst.addAll(firstNodeLiteralExprs);
            node.walk(Node.TreeTraversal.PREORDER, node1->{
               if (isStatement(node1))
                   if (finalIsCommonStmt.get(atomicStmtCounter.getAndIncrement()))
                       node1.findAll(LiteralExpr.class, lst::add);
            });
            BlockStmt stmt = new BlockStmt();
            MethodCallExpr expr = new MethodCallExpr();
            expr.setScope(new NameExpr("Copied"));
            expr.setName("repeatedCode"+which);
            expr.addArgument(new IntegerLiteralExpr(Integer.toString(nodeCounter)));
            for (LiteralExpr lexpr : lst)
                expr.addArgument(lexpr);
            stmt.addStatement(expr);
            node.replace(stmt);
            nodeCounter++;
        }
    }

    static void writeInOneMethod(List<Pair<Statement, Integer> > statements, List<ResolvedType> literalTypes, int which) {
        MethodDeclaration decl = copied.addMethod("repeatedCode"+which);
        BlockStmt blockStmt = new BlockStmt();
        decl.setBody(blockStmt);
        decl.setStatic(true);
        decl.addParameter(PrimitiveType.shortType(), "whoCalled");
        for (int i=0; i<literalTypes.size(); i++) {
            decl.addParameter(new Parameter(literalTypes.get(i).isNull() ? PrimitiveType.booleanType() : StaticJavaParser.parseType(literalTypes.get(i).describe()), "commonLiteral" + (i + 1)));
        }
        int prevB=-1;
        String prevA="";
        for (Pair<Statement, Integer> statementListPair : statements) {
            if (statementListPair.b == -1)
                blockStmt.addStatement(statementListPair.a);
            else {
                if (statementListPair.b==prevB)
                {
                    blockStmt.getStatement(blockStmt.getStatements().size()-1).asIfStmt().getThenStmt().
                            asBlockStmt().addStatement(statementListPair.a);
                }
                else if (statementListPair.a.toString().equals(prevA))
                {
                    Expression condition1 = blockStmt.getStatement(blockStmt.getStatements().size()-1).asIfStmt().getCondition();
                    Expression condition2 = new BinaryExpr(new NameExpr("whoCalled"),
                            new IntegerLiteralExpr(statementListPair.b.toString()), BinaryExpr.Operator.EQUALS);
                    Expression condition = new BinaryExpr(condition1, condition2, BinaryExpr.Operator.OR);
                    blockStmt.getStatement(blockStmt.getStatements().size()-1).asIfStmt().setCondition(condition);
                }
                else {
                    IfStmt stmt = new IfStmt();
                    stmt.setThenStmt(new BlockStmt().addStatement(statementListPair.a));
                    Expression condition = new BinaryExpr(new NameExpr("whoCalled"),
                            new IntegerLiteralExpr(statementListPair.b.toString()), BinaryExpr.Operator.EQUALS);
                    stmt.setCondition(condition);
                    blockStmt.addStatement(stmt);
                }
            }
            prevA = statementListPair.a.toString();
            prevB = statementListPair.b;
        }
    }

    static void exportClass()
    {
        FileWriter writer = null;
        try {
            writer = new FileWriter("out/Copied.java");
            writer.write(copied.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}