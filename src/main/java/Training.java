import at.unisalzburg.dbresearch.apted.distance.APTED;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.Pair;
import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Training
{
    static ArrayList<Pair<Graph, Graph>> clones = new ArrayList<>();
    static ArrayList<Pair<Graph, Graph>> opposites = new ArrayList<>();
    static Cost cost = new Cost();
    static APTED<Cost, NodeData> apted = new APTED<>(cost);
    static void readFiles()
    {
        for (int j=1; j<=3; j++)
        for (int i=1; i<=50; i++)
        {
            File file1 = new File("training/clone"+j+"/"+i+".1.java");
            File file2 = new File("training/clone"+j+"/"+i+".2.java");
            try {
                System.out.println(file2.getAbsolutePath());
                CompilationUnit unit1 = StaticJavaParser.parse(file1);
                CompilationUnit unit2 = StaticJavaParser.parse(file2);
                clones.add(new Pair(new Graph(unit1, file1.getAbsolutePath(), true),
                                    new Graph(unit2, file2.getAbsolutePath(), true))
                            );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }

        for (int i=1; i<=150; i++)
        {
            File file1 = new File("training/opposite/"+i+".1.java");
            File file2 = new File("training/opposite/"+i+".2.java");
            try {
                CompilationUnit unit1 = StaticJavaParser.parse(file1);
                CompilationUnit unit2 = StaticJavaParser.parse(file2);
                opposites.add(new Pair(new Graph(unit1, file1.getAbsolutePath(), true),
                        new Graph(unit2, file2.getAbsolutePath(), true))
                );
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    static ArrayList<Float> F(ArrayList<Float> params)
    {
        ArrayList<Float> result = new ArrayList<>(params);
        for (int i=0; i<params.size(); i++)
        {
            long ran = Math.round(Math.random())%7-2;
            if (result.get(i)+ran>0)
                result.set(i, result.get(i)+ran);
        }
        return result;
    }

    static Float E(ArrayList<Float> params)
    {
        assert (params.size()==4);
        cost.setInsCost(params.get(0));
        cost.setRenCost(params.get(1));
        cost.setDelCost(params.get(2));
        int c1=0,c2=0;
        for (Pair<Graph, Graph> clone : clones) {
            double dist = apted.computeEditDistance(clone.a.algoRoot, clone.b.algoRoot);
            boolean flag = (dist <= params.get(3));
            if (flag)
                c1++;
            else
                c2++;
        }
        for (Pair<Graph, Graph> opposite : opposites) {
            double dist = apted.computeEditDistance(opposite.a.algoRoot, opposite.b.algoRoot);
            boolean flag = (dist <= params.get(3));
            if (!flag)
                c1++;
            else
                c2++;
        }
        System.out.println(c1+" "+c2);
        return (float) (c1-c2*c2);
    }

    static void simulatedAnnealing()
    {
        double Tmin = 0.1, Tmax = 1000;
        double T = Tmax;
        ArrayList<Float> params = new ArrayList<>();
        params.add(5.0f);
        params.add(2.0f);
        params.add(5.0f);
        params.add(120.0f);
        double i = 1;
        double res = E(params);
        System.out.println(res);
        while (T>Tmin)
        {
            ArrayList<Float> params2 = F(params);
            double res2 = E(params2);
            for (Float p : params)
                System.out.print(p+" ");
            System.out.println();
            System.out.println(T+" "+res);
            double diff = res2-res;
            if (diff>=0)
            {
                params = new ArrayList<>(params2);
                res = res2;
            }
            else
            {
                double P = Math.pow(Math.E, -diff/T);
                if (Math.random()<P)
                {
                    params = new ArrayList<>(params2);
                    res = res2;
                }
            }
            T = Tmax*0.1/i;
            i++;
        }
        double ans = E(params);
        System.out.println(ans);
    }
    static void main()
    {
        readFiles();
        /*for (Pair<Graph, Graph> clone : clones)
        {
            System.out.println(apted.computeEditDistance(clone.a.algoRoot, clone.b.algoRoot));
        }
        System.out.println("-------");
        for (Pair<Graph, Graph> opposite : opposites)
        {
            System.out.println(apted.computeEditDistance(opposite.a.algoRoot, opposite.b.algoRoot));
        }*/
        simulatedAnnealing();
    }
}