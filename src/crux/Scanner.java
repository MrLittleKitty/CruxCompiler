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

                //Skip over whitespace so the next character isn't just a whitespace
                while(Character.isWhitespace(nextChar)) {
                    nextChar = input.read();
                    charPos++;
                }

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

    }


    /* Invariants:
     *  1. call assumes that nextChar is already holding an unread character
     *  2. return leaves nextChar containing an untokenized character
     */
    public Token next()
    {
        //If we are at the end of the file then keep returning the EOF token
        if(nextChar == -1)
            return Token.EOF(lineNum,charPos);

        //If it starts with a digit then its a number
        if(Character.isDigit((char)nextChar)) {
            StringBuilder buffer = new StringBuilder();
            do {
                buffer.append((char)nextChar);
                readChar();
            } while(Character.isDigit((char)nextChar) || ('.' == (char)nextChar && !checkBuffer(buffer,'.')));

            if(checkBuffer(buffer,'.'))
                return Token.Float(lineNum,charPos-1,buffer.toString());
            else return Token.Int(lineNum,charPos-1,buffer.toString());
        }
        else if('_' == (char)nextChar) { //If it starts with an underscore then its an identifier

        }
        else if(Character.isLetter((char)nextChar)) { //If its a letter then its either a keyword or an identifier

        }
        else { //Its a symbol so either an error or a reserved symbol
            StringBuilder buffer = new StringBuilder();
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
