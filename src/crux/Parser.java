package crux;

import ast.*;

import java.time.temporal.ValueRange;
import java.util.ArrayList;
import java.util.List;

import static crux.NonTerminal.*;

public class Parser {
    public static String studentName = "Eric Wolfe";
    public static String studentID = "76946154";
    public static String uciNetID = "eawolfe";

    // Grammar Rule Reporting ==========================================
    private int parseTreeRecursionDepth = 0;
    private StringBuffer parseTreeBuffer = new StringBuffer();

    private SymbolTable symbolTable;

    private void initSymbolTable() {
        symbolTable = new SymbolTable(null, 0);
        symbolTable.insert("readInt");
        symbolTable.insert("readFloat");
        symbolTable.insert("printBool");
        symbolTable.insert("printInt");
        symbolTable.insert("printFloat");
        symbolTable.insert("println");
    }

    private void enterScope() {
        symbolTable = new SymbolTable(symbolTable, symbolTable.getDepth() + 1);
        ;
    }

    private void exitScope() {
        symbolTable = symbolTable.getParentTable();
    }

    private Symbol tryResolveSymbol(Token ident) {
        assert (ident.is(Token.Kind.IDENTIFIER));
        String name = ident.lexeme();
        try {
            return symbolTable.lookup(name);
        } catch (SymbolNotFoundError e) {
            String message = reportResolveSymbolError(name, ident.lineNumber(), ident.charPosition());
            return new ErrorSymbol(message);
        }
    }

    private String reportResolveSymbolError(String name, int lineNum, int charPos) {
        String message = "ResolveSymbolError(" + lineNum + "," + charPos + ")[Could not find " + name + ".]";
        errorBuffer.append(message);
        errorBuffer.append("\n");
        errorBuffer.append(symbolTable.toString());
        errorBuffer.append("\n");
        return message;
    }

    private Symbol tryDeclareSymbol(Token ident) {
        assert (ident.is(Token.Kind.IDENTIFIER));
        String name = ident.lexeme();
        try {
            return symbolTable.insert(name);
        } catch (RedeclarationError re) {
            String message = reportDeclareSymbolError(name, ident.lineNumber(), ident.charPosition());
            return new ErrorSymbol(message);
        }
    }

    private String reportDeclareSymbolError(String name, int lineNum, int charPos) {
        String message = "DeclareSymbolError(" + lineNum + "," + charPos + ")[" + name + " already exists.]";
        errorBuffer.append(message + "\n");
        errorBuffer.append(symbolTable.toString() + "\n");
        return message;
    }

    public void enterRule(NonTerminal nonTerminal) {
//        String lineData = new String();
//        for (int i = 0; i < parseTreeRecursionDepth; i++) {
//            lineData += "  ";
//        }
//        lineData += nonTerminal.name();
//        //System.out.println("descending " + lineData);
//        parseTreeBuffer.append(lineData + "\n");
//        parseTreeRecursionDepth++;
    }

    private void exitRule(NonTerminal nonTerminal) {
//        parseTreeRecursionDepth--;
    }

    public String parseTreeReport() {
        return parseTreeBuffer.toString();
    }

    // Error Reporting ==========================================
    private StringBuffer errorBuffer = new StringBuffer();

    private String reportSyntaxError(NonTerminal nt) {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected a token from " + nt.name() + " but got " + currentToken.kind() + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }

    private String reportSyntaxError(Token.Kind kind) {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected " + kind + " but got " + currentToken.kind() + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }

    public String errorReport() {
        return errorBuffer.toString();
    }

    public boolean hasError() {
        return errorBuffer.length() != 0;
    }

    private class QuitParseException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public QuitParseException(String errorMessage) {
            super(errorMessage);
        }
    }

    private int lineNumber() {
        return currentToken.lineNumber();
    }

    private int charPosition() {
        return currentToken.charPosition();
    }

    // Parser ==========================================
    private Scanner scanner;
    private Token currentToken;

    public Parser(Scanner scanner) {
        this.scanner = scanner;
        currentToken = scanner.next();
    }

    public Command parse() {
        initSymbolTable();
        try {
            return program();
        } catch (QuitParseException q) {
            return new ast.Error(lineNumber(), charPosition(), "Could not complete parsing.");
        }
    }

    // Helper Methods ==========================================
    private boolean has(Token.Kind kind) {
        return currentToken.is(kind);
    }

    private boolean has(NonTerminal nt) {
        return nt.firstSet().contains(currentToken.kind());
    }

    private boolean accept(Token.Kind kind) {
        if (has(kind)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }

    private boolean accept(NonTerminal nt) {
        if (has(nt)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }

    private boolean expect(Token.Kind kind) {
        if (accept(kind))
            return true;
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
    }

    private boolean expect(NonTerminal nt) {
        if (accept(nt))
            return true;
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
    }

    private Token expectRetrieve(Token.Kind kind) {
        Token tok = currentToken;
        if (accept(kind))
            return tok;
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
    }

    private Token expectRetrieve(NonTerminal nt) {
        Token tok = currentToken;
        if (accept(nt))
            return tok;
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
    }

// Grammar Rules =====================================================

    // literal := INTEGER | FLOAT | TRUE | FALSE .
    public ast.Expression literal() {
        enterRule(LITERAL);

        Token tok = expectRetrieve(LITERAL);
        Expression expr = Command.newLiteral(tok);

        exitRule(LITERAL);
        return expr;
    }

    // designator := IDENTIFIER { "[" expression0 "]" } .
    public void designator() {
        enterRule(DESIGNATOR);

        Token variable = expectRetrieve(Token.Kind.IDENTIFIER);
        tryResolveSymbol(variable);

        while (accept(Token.Kind.OPEN_BRACKET)) {
            expression0();
            expect(Token.Kind.CLOSE_BRACKET);
        }

        exitRule(DESIGNATOR);
    }

    // type := IDENTIFIER .
    public void type() {
        enterRule(TYPE);
        expect(Token.Kind.IDENTIFIER);
        exitRule(TYPE);
    }

    // op0 := ">=" | "<=" | "!=" | "==" | ">" | "<" .
    public void op0() {
        enterRule(OP0);
        expect(OP0);
        exitRule(OP0);
    }

    // op1 := "+" | "-" | "or" .
    public void op1() {
        enterRule(OP1);
        expect(OP1);
        exitRule(OP1);
    }

    // op2 := "*" | "/" | "and" .
    public void op2() {
        enterRule(OP2);
        expect(OP2);
        exitRule(OP2);
    }

    // expression0 := expression1 [ op0 expression1 ] .
    public void expression0() {
        enterRule(EXPRESSION0);

        expression1();
        if (has(OP0)) {
            op0();
            expression1();
        }

        exitRule(EXPRESSION0);
    }

    // expression1 := expression2 { op1  expression2 } .
    public void expression1() {
        enterRule(EXPRESSION1);

        expression2();
        while (has(OP1)) {
            op1();
            expression2();
        }

        exitRule(EXPRESSION1);
    }

    // expression2 := expression3 { op2 expression3 } .
    public void expression2() {
        enterRule(EXPRESSION2);

        expression3();
        while (has(OP2)) {
            op2();
            expression3();
        }

        exitRule(EXPRESSION2);
    }

    // expression3 := "not" expression3
    //       | "(" expression0 ")"
    //       | designator
    //       | call-expression
    //       | literal .
    public void expression3() {
        enterRule(EXPRESSION3);

        if (accept(Token.Kind.NOT))
            expression3();
        else if (accept(Token.Kind.OPEN_PAREN)) {
            expression0();
            expect(Token.Kind.CLOSE_PAREN);
        } else if (has(DESIGNATOR))
            designator();
        else if (has(CALL_EXPRESSION))
            call_expression();
        else if (has(LITERAL))
            literal();
        else
            throw new QuitParseException(reportSyntaxError(EXPRESSION3));

        exitRule(EXPRESSION3);
    }

    // call-expression := "::" IDENTIFIER "(" expression-list ")" .
    public Call call_expression() {
        enterRule(CALL_EXPRESSION);

        expect(Token.Kind.CALL);

        Token functionName = expectRetrieve(Token.Kind.IDENTIFIER);
        tryResolveSymbol(functionName);

        expect(Token.Kind.OPEN_PAREN);
        expression_list();
        expect(Token.Kind.CLOSE_PAREN);

        exitRule(CALL_EXPRESSION);
    }

    // expression-list := [ expression0 { "," expression0 } ] .
    public void expression_list() {
        enterRule(EXPRESSION_LIST);

        if (has(EXPRESSION0)) {
            expression0();
            while (accept(Token.Kind.COMMA))
                expression0();
        }

        exitRule(EXPRESSION_LIST);
    }

    // parameter := IDENTIFIER ":" type .
    public Symbol parameter() {
        enterRule(PARAMETER);

        Token parameterName = expectRetrieve(Token.Kind.IDENTIFIER);
        Symbol parameter = tryDeclareSymbol(parameterName);

        expect(Token.Kind.COLON);
        type();

        exitRule(PARAMETER);
        return parameter;
    }

    // parameter-list := [ parameter { "," parameter } ] .
    public List<Symbol> parameter_list() {
        enterRule(PARAMETER_LIST);

        List<Symbol> parameters = new ArrayList<>();
        if (has(PARAMETER)) {
            parameters.add(parameter());
            while (accept(Token.Kind.COMMA))
                parameters.add(parameter());
        }

        exitRule(PARAMETER_LIST);
        return parameters;
    }

    // variable-declaration := "var" IDENTIFIER ":" type ";" .
    public VariableDeclaration variable_declaration() {
        enterRule(VARIABLE_DECLARATION);

        int lineNumber = currentToken.lineNumber();
        int charPos = currentToken.charPosition();
        expect(Token.Kind.VAR);

        Token variableName = expectRetrieve(Token.Kind.IDENTIFIER);
        Symbol symbol = tryDeclareSymbol(variableName);

        expect(Token.Kind.COLON);
        type();
        expect(Token.Kind.SEMICOLON);

        exitRule(VARIABLE_DECLARATION);
        return new VariableDeclaration(lineNumber, charPos, symbol);
    }

    // array-declaration := "array" IDENTIFIER ":" type "[" INTEGER "]" { "[" INTEGER "]" } ";" .
    public ArrayDeclaration array_declaration() {
        enterRule(ARRAY_DECLARATION);

        int lineNumer = currentToken.lineNumber();
        int charPos = currentToken.charPosition();
        expect(Token.Kind.ARRAY);

        Token arrayName = expectRetrieve(Token.Kind.IDENTIFIER);
        Symbol symbol = tryDeclareSymbol(arrayName);

        expect(Token.Kind.COLON);
        type();
        expect(Token.Kind.OPEN_BRACKET);
        expect(Token.Kind.INTEGER);
        expect(Token.Kind.CLOSE_BRACKET);
        while (accept(Token.Kind.OPEN_BRACKET)) {
            expect(Token.Kind.INTEGER);
            expect(Token.Kind.CLOSE_BRACKET);
        }
        expect(Token.Kind.SEMICOLON);

        exitRule(ARRAY_DECLARATION);
        return new ArrayDeclaration(lineNumer, charPos, symbol);
    }

    // function-definition := "func" IDENTIFIER "(" parameter-list ")" ":" type statement-block .
    public FunctionDefinition function_definition() {
        enterRule(FUNCTION_DEFINITION);

        int lineNumber = currentToken.lineNumber();
        int charPos = currentToken.charPosition();
        expect(Token.Kind.FUNC);

        Token functionNameToken = expectRetrieve(Token.Kind.IDENTIFIER);
        Symbol functionNameSymbol = tryDeclareSymbol(functionNameToken);

        expect(Token.Kind.OPEN_PAREN);

        //The parameters are in the same scope as the function body, so we enter a new scope at the parameters
        enterScope();

        List<Symbol> parameterSymbols = parameter_list();
        expect(Token.Kind.CLOSE_PAREN);
        expect(Token.Kind.COLON);
        type();
        //We use false because we don't want a new symbol table to be created for this scope
        //We already have one created that will include the parameters and the function body
        StatementList statementBody = statement_block(false);

        //We exit the scope after we leave the function body
        exitScope();

        exitRule(FUNCTION_DEFINITION);
        return new FunctionDefinition(lineNumber, charPos, functionNameSymbol, parameterSymbols, statementBody);
    }

    // declaration := variable-declaration
    //        | array-declaration
    //        | function-definition .
    public Declaration declaration() {
        enterRule(DECLARATION);

        Declaration declaration;

        if (has(VARIABLE_DECLARATION))
            declaration = variable_declaration();
        else if (has(ARRAY_DECLARATION))
            declaration = array_declaration();
        else if (has(FUNCTION_DEFINITION))
            declaration = function_definition();
        else
            throw new QuitParseException(reportSyntaxError(DECLARATION));

        exitRule(DECLARATION);
        return declaration;
    }

    // declaration-list := { declaration } .
    public DeclarationList declaration_list() {
        enterRule(DECLARATION_LIST);

        DeclarationList list = new DeclarationList(currentToken.lineNumber(), currentToken.charPosition());
        while (has(DECLARATION))
            list.add(declaration());

        exitRule(DECLARATION_LIST);
        return list;
    }

    // assignment-statement := "let" designator "=" expression0 ";" .
    public void assignment_statement() {
        enterRule(ASSIGNMENT_STATEMENT);

        expect(Token.Kind.LET);
        designator();
        expect(Token.Kind.ASSIGN);
        expression0();
        expect(Token.Kind.SEMICOLON);

        exitRule(ASSIGNMENT_STATEMENT);
    }

    // call-statement := call-expression ";" .
    public Call call_statement() {
        enterRule(CALL_STATEMENT);

        Call call = call_expression();
        expect(Token.Kind.SEMICOLON);

        exitRule(CALL_STATEMENT);
        return call;
    }

    // if-statement := "if" expression0 statement-block [ "else" statement-block ] .
    public void if_statement() {
        enterRule(IF_STATEMENT);

        expect(Token.Kind.IF);
        expression0();
        statement_block(true);

        if (accept(Token.Kind.ELSE))
            statement_block(true);

        exitRule(IF_STATEMENT);
    }

    // while-statement := "while" expression0 statement-block .
    public void while_statement() {
        enterRule(WHILE_STATEMENT);

        expect(Token.Kind.WHILE);
        expression0();
        statement_block(true);

        exitRule(WHILE_STATEMENT);
    }

    // return-statement := "return" expression0 ";" .
    public void return_statement() {
        enterRule(RETURN_STATEMENT);

        expect(Token.Kind.RETURN);
        expression0();
        expect(Token.Kind.SEMICOLON);

        exitRule(RETURN_STATEMENT);
    }

    // statement := variable-declaration
    //      | call-statement
    //      | assignment-statement
    //      | if-statement
    //      | while-statement
    //      | return-statement .
    public Statement statement() {
        enterRule(STATEMENT);

        Statement statement;
        if (has(VARIABLE_DECLARATION))
            statement = variable_declaration();
        else if (has(CALL_STATEMENT))
            statement = call_statement();
        else if (has(ASSIGNMENT_STATEMENT))
            statement = assignment_statement();
        else if (has(IF_STATEMENT))
            statement = if_statement();
        else if (has(WHILE_STATEMENT))
            statement = while_statement();
        else if (has(RETURN_STATEMENT))
            statement = return_statement();
        else
            throw new QuitParseException(reportSyntaxError(STATEMENT));

        exitRule(STATEMENT);
        return statement;
    }

    // statement-list := { statement } .
    public StatementList statement_list() {
        enterRule(STATEMENT_LIST);

        StatementList list = new StatementList(currentToken.lineNumber(),currentToken.charPosition());

        while (has(STATEMENT))
            list.add(statement());

        exitRule(STATEMENT_LIST);
        return list;
    }

    // statement-block := "{" statement-list "}" .
    public StatementList statement_block(boolean newSymbolTable) {
        enterRule(STATEMENT_BLOCK);

        expect(Token.Kind.OPEN_BRACE);

        if (newSymbolTable)
            enterScope();

        StatementList statements = statement_list();

        if (newSymbolTable)
            exitScope();

        expect(Token.Kind.CLOSE_BRACE);

        exitRule(STATEMENT_BLOCK);
        return statements;
    }

    // program := declaration-list EOF .
    public Command program() {
        enterRule(PROGRAM);

        DeclarationList list = declaration_list();
        expect(Token.Kind.EOF);

        exitRule(PROGRAM);
        return list;
    }

}
