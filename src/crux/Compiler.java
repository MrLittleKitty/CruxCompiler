package crux;
import java.io.FileReader;
import java.io.IOException;

public class Compiler {
    public static String studentName = "Eric Wolfe";
    public static String studentID = "76946154";
    public static String uciNetID = "eawolfe";

    public static void main(String[] args)
    {
        String sourceFile = args[0];
        Scanner s = null;

        try {
            s = new Scanner(new FileReader(sourceFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error accessing the source file: \"" + sourceFile + "\"");
            System.exit(-2);
        }

        Token t = s.next();
        while (!t.is(Token.Kind.EOF)) {
            System.out.println(t);
            t = s.next();
        }
        System.out.println(t);
    }
}
