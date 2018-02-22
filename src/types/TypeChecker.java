package types;

import java.util.HashMap;

import ast.*;

public class TypeChecker implements CommandVisitor {

    private static IntType INT = new IntType();
    private static FloatType FLOAT = new FloatType();
    private static BoolType BOOL = new BoolType();
    private static VoidType VOID = new VoidType();


    private HashMap<Command, Type> typeMap;
    private StringBuffer errorBuffer;

    /* Useful error strings:
     *
     * "Function " + func.name() + " has a void argument in position " + pos + "."
     * "Function " + func.name() + " has an error in argument in position " + pos + ": " + error.getMessage()
     *
     * "Function main has invalid signature."
     *
     * "Not all paths in function " + currentFunctionName + " have a return."
     *
     * "IfElseBranch requires bool condition not " + condType + "."
     * "WhileLoop requires bool condition not " + condType + "."
     *
     * "Function " + currentFunctionName + " returns " + currentReturnType + " not " + retType + "."
     *
     * "Variable " + varName + " has invalid type " + varType + "."
     * "Array " + arrayName + " has invalid base type " + baseType + "."
     */

    public TypeChecker() {
        typeMap = new HashMap<Command, Type>();
        errorBuffer = new StringBuffer();
    }

    private void reportError(int lineNum, int charPos, String message) {
        errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
        errorBuffer.append("[" + message + "]" + "\n");
    }

    private void put(Command node, Type type) {
        if (type instanceof ErrorType) {
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) type).getMessage());
        }
        typeMap.put(node, type);
    }

    public Type getType(Command node) {
        return typeMap.get(node);
    }

    public boolean check(Command ast) {
        ast.accept(this);
        return !hasError();
    }

    public boolean hasError() {
        return errorBuffer.length() != 0;
    }

    public String errorReport() {
        return errorBuffer.toString();
    }

    @Override
    public void visit(ExpressionList node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(DeclarationList node) {
        for (Declaration declaration : node) {
            declaration.accept(this);
        }
    }

    @Override
    public void visit(StatementList node) {
        for (Statement statement : node) {
            statement.accept(this);
        }
    }

    @Override
    public void visit(AddressOf node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LiteralBool node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LiteralFloat node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LiteralInt node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(VariableDeclaration node) {
        Type type = node.symbol().type();
        if (type.equivalent(INT) || type.equivalent(BOOL) || type.equivalent(FLOAT))
            typeMap.put(node, node.symbol().type());
        else
            typeMap.put(node, new ErrorType("Variable " + node.symbol().name() + " has invalid type "
                    + type.toString() + "."));
    }

    @Override
    public void visit(ArrayDeclaration node) {
        Type type = node.symbol().type();

        while (true) {
            if (type instanceof ArrayType) {
                ArrayType t = (ArrayType) type;
                type = t.base();
                continue;
            } else if (type.equivalent(INT) || type.equivalent(BOOL) || type.equivalent(FLOAT))
                typeMap.put(node, node.symbol().type());
            else
                typeMap.put(node, new ErrorType("Array " + node.symbol().name() + " has invalid base type "
                        + type.toString() + "."));
            break;
        }
    }

    @Override
    public void visit(FunctionDefinition node) {
        FuncType functionType = (FuncType) node.function().type();
        Type returnType = functionType.returnType();
        TypeList parameterTypeList = functionType.arguments();

        //If its the main function then check that is matches the correct signature
        if (node.function().name().equals("main")) {
            if (!returnType.equivalent(VOID) || !new TypeList().equivalent(parameterTypeList))
                typeMap.put(node, new ErrorType("Function main has invalid signature."));
            else
                typeMap.put(node, node.function().type());
        } else {
            //Otherwise make sure that there are no type errors in its parameters
            Type finalType = functionType;
            int position = 0;
            for (Type parameterType : parameterTypeList) {
                if (!parameterType.equivalent(INT) && !parameterType.equivalent(BOOL) && !parameterType.equivalent(FLOAT)) {
                    if (parameterType.equivalent(VOID))
                        finalType = new ErrorType("Function " + node.function().name() +
                                " has a void argument in position " + position + ".");
                    else if (parameterType instanceof ErrorType)
                        finalType = new ErrorType("Function " + node.function().name() +
                                " has an error in argument in position " + position + ": " + ((ErrorType) parameterType).getMessage());
                    break;
                }
                position++;
            }
            typeMap.put(node, finalType);
        }

        //Now we need to check the body of the function for errors like wrong return type, no returned value, etc.
        node.body().accept(this);
    }

    @Override
    public void visit(Comparison node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Addition node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Subtraction node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Multiplication node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Division node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LogicalAnd node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LogicalOr node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(LogicalNot node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Dereference node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Index node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Assignment node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Call node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(IfElseBranch node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(WhileLoop node) {

        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Return node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(ast.Error node) {
        put(node, new ErrorType(node.message()));
    }

    private boolean isValidValueType(Type type) {
        while (true) {
            if (type instanceof ArrayType) {
                ArrayType t = (ArrayType) type;
                type = t.base();
            } else if (type.equivalent(INT) || type.equivalent(BOOL) || type.equivalent(FLOAT))
                return true;
            else
                return false;
        }
    }
}
