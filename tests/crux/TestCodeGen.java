package crux;

import mips.CodeGen;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class TestCodeGen {

    @Test
    public void runTestCases() {

        ArrayList<String> testOutput = new ArrayList<>();
        crux.Scanner s;
//        for (int i = 1; i <= 22; i++) {
        for (int i = 1; i <= 1; i++) {
            testOutput.clear();

            File in, out;
            if (i < 10) {
                in = new File("tests/mips/test0" + i + ".crx");
                out = new File("tests/mips/test0" + i + ".out");
            } else {
                in = new File("tests/mips/test" + i + ".crx");
                out = new File("tests/mips/test" + i + ".out");
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
                    else {
                        CodeGen cg = new CodeGen(tc);
                        cg.generate(syntaxTree);
                        if (cg.hasError())
                            outputReport = "Error generating code for file " + in.getName() + "\n" + cg.errorReport();
                        else {
//                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                            PrintStream ps = new PrintStream(baos, true, "utf-8");
//                            cg.getProgram().print(ps);
//                            outputReport = new String(baos.toByteArray(), StandardCharsets.UTF_8);
//                            ps.close();
                            String asmFilename = in.getAbsolutePath().replace(".crx", ".asm");

                            mips.Program prog = cg.getProgram();
                            File asmFile = new File(asmFilename);
                            PrintStream ps = new PrintStream(asmFile);
                            prog.print(ps);
                            ps.close();
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error accessing the source file: \"" + in.getAbsolutePath() + "\"");
            }
        }
    }
}
