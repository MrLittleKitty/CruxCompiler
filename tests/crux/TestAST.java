package crux;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TestAST {

    @Test
    public void runTestCases() {

        ArrayList<String> testOutput = new ArrayList<>();
        crux.Scanner s;
        for(int i = 1; i <= 10; i++) {
            testOutput.clear();


            File in,out;
            if(i < 10) {
                in = new File("tests/ast/test0" + i + ".crx");
                out = new File("tests/ast/test0" + i + ".out");
            }
            else {
                in = new File("tests/ast/test" + i + ".crx");
                out = new File("tests/ast/test" + i + ".out");
            }

            try {
                s = new crux.Scanner(new FileReader(in));
                Parser p = new Parser(s);

                String outputReport;

                ast.Command syntaxTree = p.parse();
                if (p.hasError()) {
                    outputReport = "Error parsing file " + in.getName() +"\n\n"+p.errorReport();
                }
                else {

                    ast.PrettyPrinter pp = new ast.PrettyPrinter();
                    syntaxTree.accept(pp);
                    outputReport = pp.toString();
                }

                System.out.println("Report for file: "+in.getName());
                System.out.println(outputReport);

                String[] outputLines = outputReport.split("\n");

                for(String line : outputLines) {
                    if(!line.trim().isEmpty())
                        testOutput.add(line);
                }

                List<String> actualLines = Files.readAllLines(out.toPath());
                for(int j = actualLines.size()-1; j >= 0; j--) {
                    if(actualLines.get(j).trim().isEmpty())
                        actualLines.remove(j);
                }

                Assert.assertEquals("The size of the two outputs is not equal for file "+in.getName(),actualLines.size(),testOutput.size());
                for(int j = 0; j < actualLines.size(); j++) {
                    Assert.assertEquals("The output lines do not match for file "+in.getName(),actualLines.get(j), testOutput.get(j));
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error accessing the source file: \"" + in.getAbsolutePath() + "\"");
            }
        }
    }

}
