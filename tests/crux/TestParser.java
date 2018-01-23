package crux;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TestParser {

    @Test
    public void runTestCases() {

        ArrayList<String> output = new ArrayList<>();
        crux.Scanner s;
        for(int i = 1; i <= 15; i++) {
            output.clear();


            File in,out;
            if(i < 10) {
                in = new File("tests/parser/test0" + i + ".crx");
                out = new File("tests/parser/test0" + i + ".out");
            }
            else {
                in = new File("tests/parser/test" + i + ".crx");
                out = new File("tests/parser/test" + i + ".out");
            }

            try {
                s = new crux.Scanner(new FileReader(in));
                Parser p = new Parser(s);

                if(i == 9)
                    i = 9;

                String outputReport;
                p.parse();
                if (p.hasError())
                    outputReport = "Error parsing file.\n"+p.errorReport();
                else
                    outputReport = p.parseTreeReport();

                System.out.println("Report for file: "+in.getName());
                System.out.println(outputReport);

                String[] outputLines = outputReport.split("\n");

                for(String line : outputLines) {
                    if(!line.trim().isEmpty())
                        output.add(line);
                }

                List<String> lines = Files.readAllLines(out.toPath());
                for(int j = lines.size()-1; j >= 0; j--) {
                    if(lines.get(j).trim().isEmpty())
                        lines.remove(j);
                }

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
