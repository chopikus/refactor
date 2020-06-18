import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.printer.DotPrinter;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static List<File> filesToParse = new ArrayList<>();
    static void makeAST()
    {
        for (File sourceFile : filesToParse)
        {
            try {
                CompilationUnit cu = StaticJavaParser.parse(sourceFile);
                List<Node> astNodes = new ArrayList<>();
                cu.walk(astNodes::add);
                System.out.println("Amount of nodes: "+astNodes.size());
                DotPrinter printer = new DotPrinter(true);
                File printFile = new File("temp/"+sourceFile.getName()+".dot");
                if (!printFile.exists() && !printFile.getParentFile().mkdirs() && !printFile.createNewFile())
                    throw new Exception();
                FileWriter fileWriter = new FileWriter(printFile, false);
                fileWriter.write(printer.output(cu));
                fileWriter.close();
            } catch (Exception e) {
                System.out.println("Could not parse one of your files: "+sourceFile.getAbsolutePath());
                e.printStackTrace();
                System.exit(0);
            }
        }
    }
    static void parseArgs(String[] args)
    {
        if (args.length!=1)
        {
            System.out.println("You need to specify only path to directory!");
            System.exit(0);
        }
        else
        {
            File folder = new File(args[0]);
            if (folder.isDirectory()) {
                File[] listOfFiles = folder.listFiles();
                if (listOfFiles != null) {
                    for (File file : listOfFiles) {
                        if (file.isFile() && file.getName().
                                substring(file.getName().
                                        lastIndexOf('.') + 1).equals("java"))
                            filesToParse.add(file);
                    }
                }
            }
            else
            {
                filesToParse.add(folder);
                //cause it's not really a folder, it's a file
            }
        }
    }
    public static void main(String[] args) {
        parseArgs(args);
        makeAST();
    }
}