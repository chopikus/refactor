import com.github.javaparser.JavaParser;

import static com.github.javaparser.ParseStart.COMPILATION_UNIT;
import static com.github.javaparser.Providers.provider;

public class Main {
    public static void main(String[] args) {
        new JavaParser().parse(COMPILATION_UNIT, provider("class X{int a; }")).ifSuccessful(cu ->
                System.out.println(cu)
        );
    }
}