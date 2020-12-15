import at.unisalzburg.dbresearch.apted.costmodel.CostModel;
import at.unisalzburg.dbresearch.apted.node.Node;
import at.unisalzburg.dbresearch.apted.node.StringNodeData;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserSymbolDeclaration;
import com.github.javaparser.utils.Pair;
import flanagan.math.MaximisationFunction;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

class Cost implements CostModel<NodeData> {

    public float del(Node<NodeData> n) {
        return 0.5f;
    }
    public float ins(Node<NodeData> n) {
        return 0.5f;
    }
    public float ren(Node<NodeData> n1, Node<NodeData> n2) {
        return (n1.getNodeData().getLabel().equals(n2.getNodeData().getLabel())) ? 0.0f : 1f;
    }
}


class NodeData
{
    private Integer label;
    public NodeData(Integer label)
    {
        this.label = label;
    }
    public Integer getLabel()
    {
        return label;
    }
    public void setLabel(Integer a){this.label = a;}
    public String toString()
    {
        return label.toString();
    }
}

class Piece
{
    com.github.javaparser.ast.Node node;
    int hash;
    boolean isReplaced = false;
    boolean dependentOnOtherBlocks = false;

    @Override
    public String toString()
    {
        return dependentOnOtherBlocks ? node.toString()+"(dep)" : node.toString();
    }

    Piece(com.github.javaparser.ast.Node node) {
        this.node = node;
        this.hash = Utils.hashNode(node);
        node.walk(NameExpr.class, nameExpr -> {
            try {
                JavaParserFacade.clearInstances();
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
                    this.dependentOnOtherBlocks = true;
            }
            catch (Exception ignored) {
            }
            /// TODO not ignoring the exception, showing warning in console
        });
    }

    static boolean checkBranching(com.github.javaparser.ast.Node node)
    {
        return (node instanceof ForStmt || node instanceof WhileStmt || node instanceof ForEachStmt || node instanceof DoStmt || node instanceof IfStmt || node instanceof TryStmt || node instanceof ClassOrInterfaceDeclaration || node instanceof ReturnStmt);
    }

    static boolean isMultipleCondition(com.github.javaparser.ast.Node expression)
    {
        if (!(expression instanceof BinaryExpr))
            return false;
        BinaryExpr.Operator operator = ((BinaryExpr) expression).getOperator();
        return operator.compareTo(BinaryExpr.Operator.AND)==0 || operator.compareTo(BinaryExpr.Operator.OR)==0;
    }

    static boolean checkPiece(com.github.javaparser.ast.Node node) {
        if (isMultipleCondition(node))
            return false;
        if (node instanceof Expression &&
                node.getParentNode().isPresent() && isMultipleCondition(node.getParentNode().get()))
            return true;
        return (node instanceof ExpressionStmt ||
                (node instanceof Expression && node.getParentNode().isPresent()
                        && checkBranching(node.getParentNode().get())));
    }

    public int hashCode()
    {
        return hash;
    }

}
class Block
{
    List<Piece> list;
    BlockStmt root;

    Block(BlockStmt stmt) {
        this.root = stmt;
        this.list = new ArrayList<>();
        stmt.walk(com.github.javaparser.ast.Node.TreeTraversal.PREORDER, node -> {
           if (Piece.checkPiece(node))
               this.list.add(new Piece(node));
        });
    }

    @Override
    public String toString()
    {
        return list.toString();
    }

    Node<NodeData> algoGraph(int l, int r) {
        // [l, r)
        assert(l>=0 && l<list.size() && r>=l && r<=list.size());
        Queue<Pair<com.github.javaparser.ast.Node, Integer> > queue = new ArrayDeque<>();
        Node<NodeData> algoNode = new Node<>(new NodeData(root.getData(Main.NODE_ID)));
        for (int i=l; i<r; i++)
        {
            if (i>=list.size())
                break;
            com.github.javaparser.ast.Node pieceNode = list.get(i).node;
            Integer hash = list.get(i).node.getMetaModel().getTypeNameGenerified().hashCode();
            queue.add(new Pair<>(pieceNode, hash));
        }
        Map<Integer, List<Integer>> edges = new TreeMap<>();
        Map<Integer, Integer> hashByNodeId = new HashMap<>();
        Map<Integer, Boolean> used = new TreeMap<>();
        while (!queue.isEmpty())
        {
            Pair<com.github.javaparser.ast.Node, Integer> p = queue.poll();
            hashByNodeId.put(p.a.getData(Main.NODE_ID), p.b);
            AtomicBoolean broke = new AtomicBoolean(false);
            used.put(p.a.getData(Main.NODE_ID), true);
            p.a.walk(com.github.javaparser.ast.Node.TreeTraversal.PARENTS, parent->{
                if (parent instanceof Statement && !broke.get())
                {
                    if (!parent.getData(Main.NODE_ID).equals(root.getData(Main.NODE_ID))
                            && !used.getOrDefault(parent.getData(Main.NODE_ID), false)) {
                        queue.add(new Pair<>(parent, parent.getMetaModel().getTypeNameGenerified().hashCode()));
                        used.put(parent.getData(Main.NODE_ID), true);
                    }
                    List<Integer> list = edges.getOrDefault(parent.getData(Main.NODE_ID), new ArrayList<>());
                    list.add(p.a.getData(Main.NODE_ID));
                    edges.put(parent.getData(Main.NODE_ID), list);
                    broke.set(true);
                }
            });
        }
        Queue<Node<NodeData>> algoQ = new ArrayDeque<>();
        algoQ.add(algoNode);
        while (!algoQ.isEmpty())
        {
            Node<NodeData> q1 = algoQ.poll();
            for (Integer q2 : edges.getOrDefault(q1.getNodeData().getLabel(), new ArrayList<>()))
            {
                Node<NodeData> nd = new Node<>(new NodeData(q2));
                q1.addChild(nd);
                algoQ.add(nd);
            }
            q1.getNodeData().setLabel(hashByNodeId.getOrDefault(q1.getNodeData().getLabel(), -1));
        }
        return algoNode;
    }
}

class SimilarityFunction implements MaximisationFunction
{
    List<Node<NodeData>> graphs = new ArrayList<>();
    Map<Integer, Integer> pieceIndexOfVarDecl = new TreeMap<>();
    int[][] minEndPieceWithDeclsFromHere = new int[Main.blocks.size()][];
    Utils.SegmentTree.STNode[] minEndPieceSegmentTree = new Utils.SegmentTree.STNode[Main.blocks.size()];
    void preProcessUsagesAfterPossibleSegment(int blockIndex)
    {
        if (minEndPieceWithDeclsFromHere[blockIndex]!=null) /// SAFETY BLOCK
            return;
        minEndPieceWithDeclsFromHere[blockIndex] = new int[Main.blocks.get(blockIndex).list.size()];
        for (int piece=0; piece<Main.blocks.get(blockIndex).list.size(); piece++)
            minEndPieceWithDeclsFromHere[blockIndex][piece] = piece;
        for (int piece=0; piece<Main.blocks.get(blockIndex).list.size(); piece++) {
            com.github.javaparser.ast.Node pieceNode = Main.blocks.get(blockIndex).list.get(piece).node;
            int finalPiece = piece;
            pieceNode.walk(VariableDeclarationExpr.class, (variableDeclarator -> {
                pieceIndexOfVarDecl.put(variableDeclarator.getData(Main.NODE_ID), finalPiece);
            }));
            pieceNode.walk(VariableDeclarator.class, (variableDeclarator -> {
                pieceIndexOfVarDecl.put(variableDeclarator.getData(Main.NODE_ID), finalPiece);
            }));
        }
        for (int piece=0; piece<Main.blocks.get(blockIndex).list.size(); piece++) {
            com.github.javaparser.ast.Node pieceNode = Main.blocks.get(blockIndex).list.get(piece).node;
            int finalPiece = piece;
            pieceNode.walk(NameExpr.class, nameExpr -> {
                try {
                    JavaParserFacade.clearInstances();
                    ResolvedValueDeclaration declaration = nameExpr.resolve();
                    if (declaration instanceof JavaParserSymbolDeclaration) {
                        com.github.javaparser.ast.Node declNode = ((JavaParserSymbolDeclaration) declaration).getWrappedNode();
                        int declNodeId = declNode.getData(Main.NODE_ID);
                        int minEndPos = minEndPieceWithDeclsFromHere[blockIndex][pieceIndexOfVarDecl.getOrDefault(declNodeId, -1)];
                        minEndPieceWithDeclsFromHere[blockIndex][pieceIndexOfVarDecl.getOrDefault(declNodeId, -1)]
                                = Math.max(minEndPos, finalPiece);
                    }
                }
                catch (Exception ignored){}
            });
        }
        minEndPieceSegmentTree[blockIndex] = Utils.SegmentTree.constructSegmentTree(minEndPieceWithDeclsFromHere[blockIndex],
                0, minEndPieceWithDeclsFromHere[blockIndex].length-1);
    }

    boolean okWithUsagesAfterSegment(int blockIndex, int pieceIndex, int bound)
    {
        return Utils.SegmentTree.getMax(minEndPieceSegmentTree[blockIndex], pieceIndex, bound - 1) < bound;
    }

    Set<Integer> getBlocksToReplacePieces(long len)
    {
        graphs.clear();
        List<Integer> graphBlockIndexes = new ArrayList<>();
        for (Pair<Integer, Integer> p : Main.blockPieceIndexesToCompare) {
            int bound = Math.min(Main.blocks.get(p.a).list.size(), Math.toIntExact(p.b + len));
            if (okWithUsagesAfterSegment(p.a, p.b, bound)) {
                graphs.add(Main.blocks.get(p.a).algoGraph(p.b,
                        bound));
                graphBlockIndexes.add(p.a);
            }
        }
        Set<Integer> res = new TreeSet<>();
        boolean add0=false;
        for (int i=1; i<graphs.size(); i++) {
            float distance = Main.apted.computeEditDistance(graphs.get(0), graphs.get(i));
            if (distance<=Main.threshold) {
                res.add(graphBlockIndexes.get(i));
                add0 = true;
            }
        }
        if (add0)
            res.add(graphBlockIndexes.get(0));
        return res;
    }

    @Override
    public double function(double[] doubles) {
        long len = Math.round(doubles[0]);
        graphs.clear();
        long actualMaxLen = 0;
        for (Pair<Integer, Integer> p : Main.blockPieceIndexesToCompare) {
            int bound = Math.min(Main.blocks.get(p.a).list.size(), Math.toIntExact(p.b + len));
            for (int piece=p.b; piece<bound; piece++)
                if (Main.badPieces.get(p.a).contains(piece)) {
                    bound = piece;
                    break;
                }
            //check whether there are usages of variable, defined in segment, after segment
            if (okWithUsagesAfterSegment(p.a, p.b, bound)) {
                graphs.add(Main.blocks.get(p.a).algoGraph(p.b, bound));
                actualMaxLen = Math.max(actualMaxLen, bound - p.b);
            }
        }
        if (graphs.size()<=1 || actualMaxLen<len)
            return 0;
        float res = 0;
        int goodGraphs=1;
        for (int i=1; i<graphs.size(); i++)
        {
            float distance = Main.apted.computeEditDistance(graphs.get(0), graphs.get(i));
            if (distance<Main.threshold)
                goodGraphs++;
        }
        res = len*goodGraphs-goodGraphs-3-Main.threshold*goodGraphs-(len+Main.threshold*goodGraphs);
        return res;
    }
}