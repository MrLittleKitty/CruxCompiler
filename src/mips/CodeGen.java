package mips;

import java.util.regex.Pattern;

import ast.*;
import sun.rmi.server.Activation;
import types.*;

public class CodeGen implements ast.CommandVisitor {

    private StringBuffer errorBuffer = new StringBuffer();
    private TypeChecker tc;
    private Program program;
    private ActivationRecord currentFunction;

    public CodeGen(TypeChecker tc) {
        this.tc = tc;
        this.program = new Program();
    }

    public boolean hasError() {
        return errorBuffer.length() != 0;
    }

    public String errorReport() {
        return errorBuffer.toString();
    }

    private class CodeGenException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public CodeGenException(String errorMessage) {
            super(errorMessage);
        }
    }

    public boolean generate(Command ast) {
        try {
            currentFunction = ActivationRecord.newGlobalFrame();
            ast.accept(this);
            return !hasError();
        } catch (CodeGenException e) {
            return false;
        }
    }

    public Program getProgram() {
        return program;
    }

    @Override
    public void visit(ExpressionList node) {
        for (Expression expression : node)
            expression.accept(this);
    }

    @Override
    public void visit(DeclarationList node) {
        for (Declaration declaration : node)
            declaration.accept(this);
    }

    @Override
    public void visit(StatementList node) {
        for (Statement statement : node) {
            statement.accept(this);

            //If the statement returns a value then we need to remove it from the stack because
            //  "Statements don't change the size of the stack"
            if (statement instanceof Call) {
                Type type = tc.getType((Command) statement);
                if (type.equivalent(new IntType()) || type.equivalent(new BoolType()))
                    program.popInt("$t0"); //Pop int for int type and bool type (1 or 0)
                else if (type.equivalent(new FloatType()))
                    program.popFloat("$f1"); //Pop float for float type
            }
        }
    }

    @Override
    public void visit(AddressOf node) {
        currentFunction.getAddress(program, "$t0", node.symbol()); //Get the address of the variable into $t0
        program.pushInt("$t0"); //Push $t0 onto the stack (address is just an int so we can push it this way)
    }

    @Override
    public void visit(LiteralBool node) {
        String storeValue;
        if (node.value() == LiteralBool.Value.TRUE)
            storeValue = "li $t0, 1";
        else
            storeValue = "li $t0, 0";

        program.appendInstruction(storeValue); //Store the bool value into a temp register (1 or 0)
        program.pushInt("$t0");
    }

    @Override
    public void visit(LiteralFloat node) {
        String loadFloat = "li.s $f1, %g";
        program.appendInstruction(String.format(loadFloat,
                node.value()));
        program.pushFloat("$f1");
    }

    @Override
    public void visit(LiteralInt node) {
        String moveToRegister = "li $t0, %d";
        program.appendInstruction(String.format(moveToRegister,
                node.value())); //Move the into value into the temp register
        program.pushInt("$t0");
    }

    @Override
    public void visit(VariableDeclaration node) {
        currentFunction.add(program, node);
    }

    @Override
    public void visit(ArrayDeclaration node) {
        currentFunction.add(program, node);
    }

    @Override
    public void visit(FunctionDefinition node) {
        ActivationRecord record = new ActivationRecord(node, currentFunction);
        currentFunction = record;

        //We need the position of where we are placing the label so that we can come back and put in the prologue
        int position;
        boolean isMain = node.function().name().equals("main");
        if (isMain)
            position = program.appendInstruction("main:");
        else {
            String label = "func." + node.function().name();
            position = program.appendInstruction(label + ":");
        }

        //Handle all instructions for the function body
        node.body().accept(this);

        //Prologue actually need to be outside the ifelse otherwise main can't have local variables
        //Now that the stack size has been calculated (by the StatementList visitor) we insert the prologue
        program.insertPrologue(position + 1, currentFunction.getLocalsSize());

        //Put on the label for being able to jump to the end sequence
        if (isMain)
            program.appendInstruction("main.end:");
        else {
            String label = "func." + node.function().name() + ".end";
            program.appendInstruction(label + ":");
        }

        //We have to pop the return value into the return value register ($v0)
        Type returnType = node.function().type();
        if (returnType.equivalent(new BoolType()) || returnType.equivalent(new IntType()))
            program.popInt("$v0");
        else if (returnType.equivalent(new FloatType()))
            program.popFloat("$v0");

        //Append the prologue at the end of ALL functions (even main needs local variables sometimes)
        program.appendEpilogue(currentFunction.getLocalsSize());

        if (isMain) //Append the exit sequence at the end of the main method
            program.appendExitSequence();

        currentFunction = record.parent();
    }

    private void handleArithmetic(Type type, String operation) {
        if (type.equivalent(new IntType())) {
            popLeftRightInts();
            String instruction = "%s $t2, $t1, $t0";
            instruction = String.format(instruction, operation);
            program.appendInstruction(instruction);
            program.pushInt("$t2");
        } else {
            String floatOp = operation + ".s";
            popLeftRightFloats();
            String instruction = "%s $f3, $f2, $f1";
            instruction = String.format(instruction, floatOp);
            program.appendInstruction(instruction);
            program.pushFloat("$f3");
        }
    }

    private void popLeftRightInts() {
        program.popInt("$t0"); //Right side into $t0
        program.popInt("$t1"); //Left side into $t1
    }

    private void popLeftRightFloats() {
        program.popFloat("$f1"); //Right side into $f1
        program.popFloat("$f2"); //Left side into $f2
    }

    @Override
    public void visit(Addition node) {
        node.leftSide().accept(this);
        node.rightSide().accept(this);

        Type type = tc.getType(node);
        handleArithmetic(type, "add");
    }

    @Override
    public void visit(Subtraction node) {
        node.leftSide().accept(this);
        node.rightSide().accept(this);

        Type type = tc.getType(node);
        handleArithmetic(type, "sub");
    }

    @Override
    public void visit(Multiplication node) {
        node.leftSide().accept(this);
        node.rightSide().accept(this);

        Type type = tc.getType(node);
        handleArithmetic(type, "mul");
    }

    @Override
    public void visit(Division node) {
        node.leftSide().accept(this);
        node.rightSide().accept(this);

        Type type = tc.getType(node);
        handleArithmetic(type, "div");
    }

    @Override
    public void visit(LogicalAnd node) {
        node.leftSide().accept(this);
        node.rightSide().accept(this);

        //Pass IntType into the helper method because booleans are really just one and zero (int)
        handleArithmetic(new IntType(), "and");
    }

    @Override
    public void visit(LogicalOr node) {
        node.leftSide().accept(this);
        node.rightSide().accept(this);

        //Pass IntType into the helper method because booleans are really just one and zero (int)
        handleArithmetic(new IntType(), "or");
    }

    @Override
    public void visit(LogicalNot node) {
        node.expression().accept(this);
        program.popInt("$t0"); //Pop the result of the expression into temp register
        String instruction = "nor $t1, $t0, $0"; //Not the register and store it into $t1
        program.appendInstruction(instruction);
        program.pushInt("$t1"); //Push the result of the not operation onto the stack
    }

    @Override
    public void visit(Comparison node) {
        throw new RuntimeException("Implement this");
    }

    @Override
    public void visit(Dereference node) {
        node.expression().accept(this); //This should push an address onto the stack
        program.popInt("$t0"); //Pop the address into temp register

        Type type = tc.getType(node);
        if (type.equivalent(new IntType()) || type.equivalent(new BoolType())) {
            String instruction = "lw $t1, 0($t0)"; //Load the value from the address into $t1
            program.appendInstruction(instruction);
            program.pushInt("$t1"); //Push the value we loaded onto the stack
        } else if (type.equivalent(new FloatType())) {
            String instruction = "lwc1 $f1, 0($t0)"; //Load the value from the address into $f1
            program.appendInstruction(instruction);
            program.pushFloat("$f1"); //Push the value we loaded onto the stack
        }
    }

    @Override
    public void visit(Index node) {
        node.base().accept(this); //This should push an address onto the stack
        node.amount().accept(this); //This should push an int (index) onto the stack

        program.popInt("$t0"); //Pop the index into $t0
        program.popInt("$t1"); //Pop the address into $t1
        //Hard coding 4 bytes because thats the size of all datatypes in our language right now
        String instruction = "li $t2, 4"; //We need to multiply the index by the num of bytes to get the offset
        program.appendInstruction(instruction);
        instruction = "mul $t3, $t2, $t0"; //Multiply together and store the new offset into $t3
        program.appendInstruction(instruction);
        instruction = "add $t4, $t3, $t1"; //Add the offset to the given address and store into $t4
        program.appendInstruction(instruction);
        program.pushInt("$t4"); //Push the new address (base + offset) onto the stack
    }

    @Override
    public void visit(Assignment node) {
        node.destination().accept(this); //This will be the address of where we are supposed to store the source
        node.source().accept(this); //This will be the value to store (int, bool (also an int), or float)

        Type type = tc.getType((Command) node.source());
        if (type.equivalent(new IntType()) || type.equivalent(new BoolType())) {
            program.popInt("$t1"); //Pop the int value into $t1
            program.popInt("$t0"); //Pop the destination address into $t0
            String instruction = "sw $t1, 0($t0)"; //Store the int in $t1 into the address held at $t0
            program.appendInstruction(instruction);
        } else if (type.equivalent(new FloatType())) {
            program.popInt("$f1"); //Pop the float value into $f1
            program.popInt("$t0"); //Pop the destination address into $t0
            String instruction = "swc1 $f1, 0($t0)"; //Store the float in $f1 into the address held at $t0
            program.appendInstruction(instruction);
        }
    }

    @Override
    public void visit(Call node) {
        //Caller sets up the call site
        node.arguments().accept(this);

        String instruction = "jal %s"; //Jumps to the label of the func and stores the return address in the $ra register
        if (node.function().name().equals("main"))
            instruction = String.format(instruction,
                    (node.function().name())); //We jump to the special label for the main function
        else
            instruction = String.format(instruction,
                    ("func." + node.function().name())); //Jump to the label of the function that we are calling
        program.appendInstruction(instruction);

        //Caller tears down the call site
        int argumentsSize = 0;
        for (Expression expression : node.arguments()) {
            Type type = tc.getType((Command) expression);
            argumentsSize += ActivationRecord.numBytes(type);
        }

        //Remove the stack space from the arguments
        if (argumentsSize > 0) {
            instruction = "addi $sp, $sp, %d";
            instruction = String.format(instruction,
                    argumentsSize); //This frees the space on the stack that the arguments took up
            program.appendInstruction(instruction);
        }

        //If there is a return value in the return register then we push it to the stack
        Type returnType = node.function().type();
        if (returnType.equivalent(new IntType()) || returnType.equivalent(new BoolType()))
            program.pushInt("$v0");
        else if (returnType.equivalent(new FloatType()))
            program.pushFloat("$v0");
    }

    @Override
    public void visit(IfElseBranch node) {
//        String ifLabel = program.requestLabel(currentFunction.name()+".if");
        String elseLabel = program.requestLabel(currentFunction.name() + ".else");
        String endLabel = program.requestLabel(currentFunction.name() + ".ifelse.end");

        //Visit the condition block and have its result pushed onto the stack. Then pop it into a temp register
        node.condition().accept(this);
        program.popInt("$t0");

        //Jump past the if block if the condition register is equal to 0 (false)
        String instruction = "beq $t0, $0, %s";
        instruction = String.format(instruction,
                elseLabel); //Jump to the else branch if the condition is false
        program.appendInstruction(instruction);

        //Handles all the instructions for the if block
        node.thenBlock().accept(this);
        instruction = "j %s";
        instruction = String.format(instruction,
                endLabel); //Jump to the end label after we finish the if block
        program.appendInstruction(instruction);

        //Start the else block
        program.appendInstruction(elseLabel + ":");
        node.elseBlock().accept(this);
        program.appendInstruction(endLabel + ":");
    }

    @Override
    public void visit(WhileLoop node) {
        String loopLabel = program.requestLabel(currentFunction.name() + ".while");
        String endLabel = program.requestLabel(currentFunction.name() + ".while.end");

        //Setup the label so that we can jump back to do the loop over again
        program.appendInstruction(loopLabel + ":");
        //Then accept the condition and have its result pushed onto the stack. Then pop it into a temp register
        node.condition().accept(this);
        program.popInt("$t0");

        //Jump past the loop block if the condition register is equal to 0 (false)
        String instruction = "beq $t0, $0, %s";
        instruction = String.format(instruction,
                endLabel); //Jump past the loop block if the condition is false
        program.appendInstruction(instruction);

        //Handle all the instructions of the while block
        node.body().accept(this);
        instruction = "j %s";
        instruction = String.format(instruction,
                loopLabel); //Jump to the loop label after we finish the loop so that it can run again (if needed)
        program.appendInstruction(instruction);

        //Add the end label so that if the loop condition is false we jump past all the loop block code
        program.appendInstruction(endLabel + ":");
    }

    @Override
    public void visit(Return node) {
        node.argument().accept(this);
        String jumpToFinish = "j %s"; //Jump to the code to handle return values and tearing down the epilogue
        if (currentFunction.name().equals("main"))
            jumpToFinish = String.format(jumpToFinish, "main.end");
        else
            jumpToFinish = String.format(jumpToFinish, "func." + currentFunction.name() + ".end");
        program.appendInstruction(jumpToFinish);
    }

    @Override
    public void visit(ast.Error node) {
        String message = "CodeGen cannot compile a " + node;
        errorBuffer.append(message);
        throw new CodeGenException(message);
    }
}
