package mips;

import java.util.HashMap;

import crux.Symbol;
import types.*;

public class ActivationRecord {
    private static int fixedFrameSize = 2 * 4;
    private ast.FunctionDefinition func;
    private ActivationRecord parent;
    private int stackSize;
    private HashMap<Symbol, Integer> locals;
    private HashMap<Symbol, Integer> arguments;

    public static ActivationRecord newGlobalFrame() {
        return new GlobalFrame();
    }

    public static int numBytes(Type type) {
        if (type instanceof BoolType)
            return 4;
        if (type instanceof IntType)
            return 4;
        if (type instanceof FloatType)
            return 4;
        if (type instanceof ArrayType) {
            ArrayType aType = (ArrayType) type;
            return aType.extent() * numBytes(aType.base());
        }
        throw new RuntimeException("No size known for " + type);
    }

    protected ActivationRecord() {
        this.func = null;
        this.parent = null;
        this.stackSize = 0;
        this.locals = null;
        this.arguments = null;
    }

    public ActivationRecord(ast.FunctionDefinition fd, ActivationRecord parent) {
        this.func = fd;
        this.parent = parent;
        this.stackSize = 0;
        this.locals = new HashMap<Symbol, Integer>();

        // map this function's parameters
        this.arguments = new HashMap<Symbol, Integer>();
        int offset = 0;
        for (int i = fd.arguments().size() - 1; i >= 0; --i) {
            Symbol arg = fd.arguments().get(i);
            arguments.put(arg, offset);
            offset += numBytes(arg.type());
        }
    }

    public String name() {
        return func.symbol().name();
    }

    public ActivationRecord parent() {
        return parent;
    }

    public int getLocalsSize() {
        return stackSize;
    }

    public void add(Program program, ast.VariableDeclaration var) {
        allocateLocalSpace(program, var.symbol());
    }

    public void add(Program program, ast.ArrayDeclaration array) {
        //Arrays aren't valid local variables in functions (according to grammar) but they are allocated just like locals
        allocateLocalSpace(program, array.symbol());
    }

    private void allocateLocalSpace(Program program, Symbol symbol) {
        int space = ActivationRecord.numBytes(symbol.type());
//        String instruction = "subu &sp, &sp, %d";
//        program.appendInstruction(String.format(instruction,
//                space)); //The amount of space we are allocating on the stack for this local variable
        int offset = -12 - stackSize;
        locals.put(symbol, offset);
        stackSize += space;
    }

    public void getAddress(Program program, String register, Symbol sym) {
        Integer offset;
        if (locals.containsKey(sym))
            offset = locals.get(sym);
        else if (arguments.containsKey(sym))
            offset = arguments.get(sym);
        else {
            parent.getAddress(program, register, sym);
            return;
        }

        //This means its one of the parameters
        String instruction = "addi %s, $fp, %s";
        program.appendInstruction(String.format(instruction,
                register, //The register that we are going to load the address into
                offset)); //The offset of the parameter variable that they are referencing
    }
}

class GlobalFrame extends ActivationRecord {
    public GlobalFrame() {
    }

    private String getUniqueGlobalLabel(String name) {
        return "cruxdata." + name;
    }

    @Override
    public void add(Program program, ast.VariableDeclaration var) {
        program.appendData(reserveSpace(var.symbol(),
                ActivationRecord.numBytes(var.symbol().type())));
    }

    @Override
    public void add(Program program, ast.ArrayDeclaration array) {
        program.appendData(reserveSpace(array.symbol(),
                ActivationRecord.numBytes(array.symbol().type())));
    }

    private String reserveSpace(Symbol symbol, int space) {
        String instruction = "%s: .space %d";
        return String.format(instruction,
                getUniqueGlobalLabel(symbol.name()),
                space);
    }

    @Override
    public void getAddress(Program program, String register, Symbol sym) {
        String instruction = "la %s, %s";
        //Load the address into the the given register. For global data we just give the unique label address of the symbol
        program.appendInstruction(String.format(instruction,
                register, //The register that we are going to store the address into
                getUniqueGlobalLabel(sym.name()))); //The label of the given global symbol
    }
}
