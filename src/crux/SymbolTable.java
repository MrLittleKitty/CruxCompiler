package crux;

import types.Type;

import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {

    private SymbolTable parent;
    private HashMap<String, Symbol> symbolTable;
    private int depth;

    private ArrayList<Symbol> symbolsInDeclarationOrder;

    public SymbolTable(SymbolTable parent, int depth) {
        this.parent = parent;
        this.depth = depth;
        this.symbolTable = new HashMap<>();
        symbolsInDeclarationOrder = new ArrayList<>();
    }

    public Symbol lookup(String name) throws SymbolNotFoundError {
        if (symbolTable.containsKey(name))
            return symbolTable.get(name);

        if (parent != null)
            return parent.lookup(name);

        throw new SymbolNotFoundError(name);
    }

    public Symbol insert(String name, Type type) throws RedeclarationError {

        Symbol symbol = new Symbol(name, type);

        if (symbolTable.containsKey(name))
            throw new RedeclarationError(symbol);

        symbolTable.put(name, symbol);
        symbolsInDeclarationOrder.add(symbol);
        return symbol;
    }

    public void setParentTable(SymbolTable parentTable) {
        this.parent = parentTable;
    }

    public SymbolTable getParentTable() {
        return parent;
    }

    public int getDepth() {
        return depth;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (parent != null)
            sb.append(parent.toString());

        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            buffer.append("  ");
        }

        String indent = buffer.toString();
        for (Symbol s : symbolsInDeclarationOrder) {
            sb.append(indent);
            sb.append(s.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}

class SymbolNotFoundError extends Error {
    private static final long serialVersionUID = 1L;
    private String name;

    SymbolNotFoundError(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}

class RedeclarationError extends Error {
    private static final long serialVersionUID = 1L;

    public RedeclarationError(Symbol sym) {
        super("Symbol " + sym + " being redeclared.");
    }
}
