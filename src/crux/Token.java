package crux;

import java.util.HashSet;

public class Token {
    public static String studentName = "Eric Wolfe";
    public static String studentID = "76946154";
    public static String uciNetID = "eawolfe";

    public static enum Kind {
        AND("and"),
        OR("or"),
        NOT("not"),
        LET("let"),
        VAR("var"),
        ARRAY("array"),
        FUNC("func"),
        IF("if"),
        ELSE("else"),
        WHILE("while"),
        TRUE("true"),
        FALSE("false"),
        RETURN("return"),

        OPEN_PAREN("("),
        CLOSE_PAREN(")"),
        OPEN_BRACE("{"),
        CLOSE_BRACE("}"),
        OPEN_BRACKET("["),
        CLOSE_BRACKET("]"),
        ADD("+"),
        SUB("-"),
        MUL("*"),
        DIV("/"),
        GREATER_EQUAL(">="),
        LESSER_EQUAL("<="),
        NOT_EQUAL("!="),
        EQUAL("=="),
        GREATER_THAN(">"),
        LESS_THAN("<"),
        ASSIGN("="),
        COMMA(","),
        SEMICOLON(";"),
        COLON(":"),
        CALL("::"),

        IDENTIFIER(),
        INTEGER(),
        FLOAT(),
        ERROR(),
        EOF();

        private String lexeme;

        Kind() {
            lexeme = null;
        }

        Kind(String lexeme) {
            this.lexeme = lexeme;
        }

        public boolean hasStaticLexeme() {
            return lexeme != null;
        }

        public boolean matches(String lexeme) {
            return lexeme.equals(this.lexeme);
        }
    }

    //A HashSet containing all the static lexemes will make it quick for the tokenizer to know if something is a lexeme
    private static final HashSet<String> LexemeSet = new HashSet<>();

    static {
        Kind[] kinds = Kind.values();
        for (int i = 0; i < kinds.length - 5; i++) {
            LexemeSet.add(kinds[i].lexeme);
        }
    }

    public static boolean isLexeme(String lexeme) {
        return LexemeSet.contains(lexeme);
    }

    private int lineNum;
    private int charPos;
    private Kind kind;
    private String lexeme = "";

    private Token(int lineNum, int charPos) {
        this.lineNum = lineNum;
        this.charPos = charPos;

        this.kind = Kind.ERROR;
        this.lexeme = "No Lexeme Given";
    }

    public Token(String lexeme, int lineNum, int charPos) {
        this.lineNum = lineNum;
        this.charPos = charPos;

        this.lexeme = lexeme;
        this.kind = Kind.ERROR;

        Kind[] values = Kind.values();

        //We do length-5 so that we never iterate through the Kinds that have dynamic lexemes
        for (int i = 0; i < values.length - 5; i++) {
            if (values[i].matches(lexeme)) {
                this.kind = values[i];
                break;
            }
        }
    }

    public int lineNumber() {
        return lineNum;
    }

    public int charPosition() {
        return charPos;
    }

    public String lexeme() {
        return lexeme;
    }

    public Kind kind() {
        return this.kind;
    }

    public boolean is(Kind kind) {
        return this.kind.equals(kind);
    }

    public String toString() {
        if (this.kind == Kind.IDENTIFIER || this.kind == Kind.FLOAT || this.kind == Kind.INTEGER || this.kind == Kind.ERROR)
            return this.kind.name() + "(" + this.lexeme + ")(lineNum:" + this.lineNum + ", charPos:" + this.charPos + ")";
        return this.kind.name() + "(lineNum:" + this.lineNum + ", charPos:" + this.charPos + ")";
    }

    public static Token Float(String lexeme, int lineNum, int charPos) {
        Token t = new Token(lineNum, charPos);
        t.kind = Kind.FLOAT;
        t.lexeme = lexeme;
        return t;
    }

    public static Token Integer(String lexeme, int lineNum, int charPos) {
        Token t = new Token(lineNum, charPos);
        t.kind = Kind.INTEGER;
        t.lexeme = lexeme;
        return t;
    }

    public static Token Identifier(String lexeme, int lineNum, int charPos) {
        Token t = new Token(lineNum, charPos);
        t.kind = Kind.IDENTIFIER;
        t.lexeme = lexeme;
        return t;
    }

    public static Token EOF(int lineNum, int charPos) {
        Token t = new Token(lineNum, charPos);
        t.kind = Kind.EOF;
        return t;
    }

    public static Token Error(String message, int lineNum, int charPos) {
        Token t = new Token(lineNum, charPos);
        t.kind = Kind.ERROR;
        t.lexeme = message;
        return t;
    }
}
