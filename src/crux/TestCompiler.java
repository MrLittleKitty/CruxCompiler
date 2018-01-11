package crux;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

public class TestCompiler {

    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        String line;
        do {
            System.out.println("Enter a String to tokenize");
            line = scanner.nextLine();

            crux.Scanner s = new crux.Scanner(new StringReader(line));

            Token t = s.next();
            while (!t.is(Token.Kind.EOF)) {
                System.out.println(t);
                t = s.next();
            }
            System.out.println(t);

        } while(!line.equalsIgnoreCase("quit"));

        System.out.println("Quitting tokenizer.");
    }
}
