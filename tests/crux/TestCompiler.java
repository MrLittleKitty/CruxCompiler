package crux;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TestCompiler {

    @Test
    public void runTestCases() {

        ArrayList<String> output = new ArrayList<>();
        crux.Scanner s;
        for(int i = 1; i <= 30; i++) {
            output.clear();

            if(i == 26)
                i = 26;

            File in,out;
            if(i < 10) {
                in = new File("tests/files/test0" + i + ".crx");
                out = new File("tests/files/test0" + i + ".out");
            }
            else {
                in = new File("tests/files/test" + i + ".crx");
                out = new File("tests/files/test" + i + ".out");
            }

            try {
                s = new crux.Scanner(new FileReader(in));

                Token t = s.next();
                while (!t.is(Token.Kind.EOF)) {
                    output.add(t.toString());
                    t = s.next();
                }
                output.add(t.toString());

                List<String> lines = Files.readAllLines(out.toPath());

                Assert.assertEquals("The size of the two outputs is not equal for file "+in.getName(),lines.size(),output.size());
                for(int j = 0; j < lines.size(); j++) {
                    Assert.assertEquals("The output lines do not match for file "+in.getName(),lines.get(j), output.get(j));
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error accessing the source file: \"" + in.getAbsolutePath() + "\"");
            }
        }
    }
}
