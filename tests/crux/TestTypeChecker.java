package crux;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TestTypeChecker {

    @Test
    public void runTestCases() {

        ArrayList<String> testOutput = new ArrayList<>();
        crux.Scanner s;
        for (int i = 13; i <= 13; i++) {
            testOutput.clear();


            File in, out;
            if (i < 10) {
                in = new File("tests/types/test0" + i + ".crx");
                out = new File("tests/types/test0" + i + ".out");
            } else {
                in = new File("tests/types/test" + i + ".crx");
                out = new File("tests/types/test" + i + ".out");
            }

            try {
                s = new crux.Scanner(new FileReader(in));
                Parser p = new Parser(s);

                String outputReport;

                ast.Command syntaxTree = p.parse();
                if (p.hasError()) {
                    outputReport = "Error parsing file " + in.getName() + "\n\n" + p.errorReport();
                } else {
                    types.TypeChecker tc = new types.TypeChecker();
                    tc.check(syntaxTree);
                    if (tc.hasError())
                        outputReport = "Error type-checking file.\n" + tc.errorReport();
                    else
                        outputReport = "Crux Program is has no type errors.";
                }

                System.out.println("Report for file: " + in.getName());
                System.out.println(outputReport);

                String[] outputLines = outputReport.split("\n");

                for (String line : outputLines) {
                    if (!line.trim().isEmpty())
                        testOutput.add(line);
                }

                List<String> actualLines = Files.readAllLines(out.toPath());
                for (int j = actualLines.size() - 1; j >= 0; j--) {
                    if (actualLines.get(j).trim().isEmpty())
                        actualLines.remove(j);
                }

                Assert.assertEquals("The size of the two outputs is not equal for file " + in.getName(), actualLines.size(), testOutput.size());
                for (int j = 0; j < actualLines.size(); j++) {
                    Assert.assertEquals("The output lines do not match for file " + in.getName(), actualLines.get(j), testOutput.get(j));
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error accessing the source file: \"" + in.getAbsolutePath() + "\"");
            }
        }
    }

}
