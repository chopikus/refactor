import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;

import java.util.*;

public class Metrica {
    private static final List<String> operatorsList = Arrays.asList("++", "--", "+", "-", "!", "~", "*", "/", "%", "<<", ">>>", "<", ">",
            ">=", "<=", "instanceof", "==", "!=", "&", "^", "|", "&&", "||", "?", ":", "=", "+=", "-=", "*=", "/=", "&=", "|=",
            "^=", "%=", "<<=", ">>=", ">>>=");
    private static final List<String> bollocksList = Arrays.asList("{", "}", "null", "(", ")", "[", "]");

    private double n1, n2, N1, N2, n, N, NS, V, D, E, T, B;
    private List<String> allMess = new ArrayList<>();
    private Set<String> operators = new TreeSet<>();
    private Set<String> operands = new TreeSet<>();

    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }
    private void dfs(Node node)
    {
        if (node instanceof UnaryExpr || node instanceof BinaryExpr || node instanceof AssignExpr
                || node instanceof VariableDeclarationExpr || node instanceof SimpleName)
        {
            node.getTokenRange().ifPresent(javaTokens -> javaTokens.forEach(t -> allMess.add(t.getText())));
            node.getTokenRange().ifPresent(javaTokens -> javaTokens.forEach(t -> System.out.println(t.getText())));
        }
        else
            for (Node child : node.getChildNodes())
                dfs(child);
    }

    public Metrica(CompilationUnit unit) {
        dfs(unit);
        for (String oper : allMess) {
            if (bollocksList.contains(oper))
            {
                continue;
            }
            if (operatorsList.contains(oper)) {
                operators.add(oper);
                N1++;
            } else {
                System.out.println(oper);
                operands.add(oper);
                N2++;
            }
        }
        n1 = operators.size();
        n2 = operands.size();
        n = n1 + n2;
        N = N1 + N2;
        System.out.println(String.format("%s/%s unique operators %s/%s unique operands", n1, N1, n2, N2));
        NS = n1 * log2(n1) + n2 * log2(n2);
        V = N * log2(n);
        if (n2 == 0)
            D = 0;
        else
            D = n1 / 2 * N2 / n2;
        E = D * V;
        T = E / 18;
        B = V / 3000;
    }

    public double getCodingTime() {
        return T;
    }
    public double getBugs() {
        return B;
    }
}
