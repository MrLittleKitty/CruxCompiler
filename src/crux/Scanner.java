package crux;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

public class Scanner implements Iterable<Token> {
    public static String studentName = "Eric Wolfe";
    public static String studentID = "76946154";
    public static String uciNetID = "eawolfe";

    private int lineNum;  // current line count
    private int charPos;  // character offset for current line
    private int nextChar = 0; // contains the next char (-1 == EOF)
    private Reader input;

    Scanner(Reader reader)
    {
        this.input = reader;
        this.lineNum = 1;
        this.charPos = 0;
        readChar();
    }

	private void readChar() {
        //If the nextChar is ever -1, then we reached the end of the file and there is no reason to keep reading
        if(nextChar != -1) {
            try {
                nextChar = input.read();
                charPos++;

                while ((char) nextChar == Character.LINE_SEPARATOR) {
                    lineNum++;
                    nextChar = input.read();
                    charPos = 1;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void nextLine() {
        //This will just read and throw away every character until we get to the line separator
        while(nextChar != -1 && (char)nextChar != Character.LINE_SEPARATOR) {
            try {
                nextChar = input.read();
                charPos++;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //If we reach the end of the file before we reach a line separator then we just stop because the file is over
        if(nextChar == -1)
            return;

        //Set the linenumber to the next line and the char pos to zero
        lineNum++;
        charPos = 0;

        //Read the next char (handles empty lines and such)
        readChar();
    }


    /* Invariants:
     *  1. call assumes that nextChar is already holding an unread character
     *  2. return leaves nextChar containing an untokenized character
     */
    public Token next()
    {
        //This handles line comments that are two '/' characters in a row
        while('/' == (char)nextChar) { //We use while so that we can handle multiple commented lines
            readChar();
            //If its two '/' in a row then its a comment and we skip the whole line
            if('/' ==(char)nextChar)
                nextLine();
            else //Otherwise its only one '/' and its the division token
                return new Token(lineNum,charPos-1,"/");
        }

        //If we are at the end of the file then keep returning the EOF token
        if(nextChar == -1)
            return Token.EOF(lineNum,charPos);

        //Skip over whitespace until we get to the next token
        while(Character.isWhitespace((char)nextChar))
            readChar();

        //If it starts with a digit then its a number
        if(Character.isDigit((char)nextChar)) {
            StringBuilder buffer = new StringBuilder();
            do {
                buffer.append((char)nextChar);
                readChar();
            } while(Character.isDigit((char)nextChar) || ('.' == (char)nextChar && !checkBuffer(buffer,'.')));

            if(checkBuffer(buffer,'.'))
                return Token.Float(lineNum,charPos-1,buffer.toString());

            return Token.Int(lineNum,charPos-1,buffer.toString());
        }
        else if(Character.isLetter((char)nextChar) || '_' == (char)nextChar) {
            //If its a letter (or underscore) then its either an identifier or a keyword
            StringBuilder buffer = new StringBuilder();
            do {
                buffer.append((char)nextChar);
                readChar();
            } while(Character.isLetterOrDigit((char)nextChar) || '_' == (char)nextChar);

            if(Token.isLexeme(buffer.toString()))
                return new Token(lineNum,charPos-1,buffer.toString());

            return Token.Identifier(lineNum,charPos-1,buffer.toString());
        }
        else { //Its a symbol so either an error or a reserved symbol
            StringBuilder buffer = new StringBuilder();
            buffer.append((char)nextChar);
            readChar();

            //If the symbol token isn't a single char lexeme or the start of a two char lexeme then its an error
            if(!Token.isLexeme(buffer.toString())) {
                return Token.Error(lineNum,charPos-1);
            }

            //Add the second char to the buffer so we can see if its a two char lexeme
            buffer.append((char)nextChar);

            //If its a two char lexeme then read the next char and return the token
            if(Token.isLexeme(buffer.toString())) {
                readChar();
                return new Token(lineNum,charPos-1,buffer.toString());
            }
            else { //This means its a one char lexeme
                //Since we have already done the readChar() for the second char, don't do another one.
                //Just return a token with the first char in the buffer
                buffer.setLength(buffer.length()-1); //cuts the last character off the buffer
                return new Token(lineNum,charPos-1,buffer.toString());
            }
        }
    }

    private boolean checkBuffer(StringBuilder buffer, char forChar) {
        for(int i = 0; i < buffer.length(); i++) {
            if(buffer.charAt(i) == forChar)
                return true;
        }
        return false;
    }

    @Override
    public Iterator<Token> iterator() {
        return null;
    }

    private class TokenIterator implements Iterator<Token> {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Token next() {
            return null;
        }
    }
}
