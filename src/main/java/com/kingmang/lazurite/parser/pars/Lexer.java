package com.kingmang.lazurite.parser.pars;

import com.kingmang.lazurite.LZREx.LZRException;
import com.kingmang.lazurite.Main;
import com.kingmang.lazurite.core.KEYWORD;
import com.kingmang.lazurite.core.Types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kingmang.lazurite.parser.Standart.Standart;
import com.kingmang.lazurite.parser.Standart.range;
import com.kingmang.lazurite.runtime.LZR.LZRNumber;
import com.kingmang.lazurite.runtime.LZR.LZRString;
import com.kingmang.lazurite.runtime.Variables;


public final class Lexer {

    public static List<Token> tokenize(String input) {
        return new Lexer(input).tokenize();
    }
    private static final String OPERATOR_CHARS = "+-*/%()[]{}=<>!&|.,^~?:";
    private static final TokenType[] tokenTypes = TokenType.values();

    private static final String[] keywords = {
            "throw",
            "print",
            "println",
            "if",
            "else",
            "while",
            "for",
            "break",
            "continue",
            "func",
            "return",
            "using",
            "match",
            "case",
            "include",
            "class",
            "new",
            "enum"
    };


    //init keywords
    private static final Map<String, TokenType> KEYWORDS;
    static {
        KEYWORDS = new HashMap<>();
        for (int i = 0; i < keywords.length; i++) {
            if (i < tokenTypes.length) {
                KEYWORDS.put(keywords[i], tokenTypes[i]);
            } else {
                System.out.println("Not enough token types for all tokens");
                break;
            }
        }
        Variables();
        convertTypes();
        standart();

    }

    public static void Variables() {
        Variables.define("object", LZRNumber.of(Types.OBJECT));
        Variables.define("num", LZRNumber.of(Types.NUMBER));
        Variables.define("string", LZRNumber.of(Types.STRING));
        Variables.define("array", LZRNumber.of(Types.ARRAY));
        Variables.define("map", LZRNumber.of(Types.MAP));
        Variables.define("function", LZRNumber.of(Types.FUNCTION));
    }

    public static void convertTypes(){
        KEYWORD.put("str", args -> new LZRString(args[0].asString()));
        KEYWORD.put("num", args -> LZRNumber.of(args[0].asNumber()));
        KEYWORD.put("byte", args -> LZRNumber.of((byte)args[0].asInt()));
        KEYWORD.put("short", args -> LZRNumber.of((short)args[0].asInt()));
        KEYWORD.put("int", args -> LZRNumber.of(args[0].asInt()));
        KEYWORD.put("long", args -> LZRNumber.of((long)args[0].asNumber()));
        KEYWORD.put("float", args -> LZRNumber.of((float)args[0].asNumber()));
        KEYWORD.put("double", args -> LZRNumber.of(args[0].asNumber()));
    }

    private static void standart(){
        KEYWORD.put("echo", new Standart.ECHO());
        KEYWORD.put("readln", new Standart.INPUT());
        KEYWORD.put("length", new Standart.LEN());
        KEYWORD.put("getBytes", Standart.string::getBytes);
        KEYWORD.put("sprintf", new Standart.SPRINTF());
        KEYWORD.put("range", new range());
        KEYWORD.put("substring", new Standart.SUBSTR());
        KEYWORD.put("parseInt", Standart.PARSE::parseInt);
        KEYWORD.put("parseLong", Standart.PARSE::parseLong);
        KEYWORD.put("foreach", new Standart.FOREACH());
        KEYWORD.put("flatmap", new Standart.FLATMAP());
        KEYWORD.put("split", new Standart.split());
        KEYWORD.put("filter", new Standart.FILTER(false));
    }

    public static Set<String> getKeywords() {
        return KEYWORDS.keySet();
    }

    private final String input;
    private final int length;
    
    private final List<Token> tokens;
    private final StringBuilder buffer;
    
    private int pos;
    private int row, col;

    public Lexer(String input) {
        this.input = input;
        length = input.length();
        
        tokens = new ArrayList<>();
        buffer = new StringBuilder();
        row = col = 1;
    }
    
    public List<Token> tokenize() {
        while (pos < length) {
            final char current = peek(0);
            if (Character.isDigit(current)) tokenizeNumber();
            else if (isLZRIdentifier(current)) tokenizeWord();
            else if (current == '`') tokenizeExtendedWord();
            else if (current == '"') tokenizeText();
            else if (current == '#') {
                next();
                tokenizeHexNumber(1);
            }
            else if (OPERATOR_CHARS.indexOf(current) != -1) {
                tokenizeOperator();
            } else {
                // whitespaces
                next();
            }
        }
        return tokens;
    }
    
    private void tokenizeNumber() {
        clearBuffer();
        char current = peek(0);
        if (current == '0' && (peek(1) == 'x' || (peek(1) == 'X'))) {
            next();
            next();
            tokenizeHexNumber(2);
            return;
        }
        while (true) {
            if (current == '.') {
                if (buffer.indexOf(".") != -1) throw error("Invalid float number");
            } else if (!Character.isDigit(current)) {
                break;
            }
            buffer.append(current);
            current = next();
        }
        addToken(TokenType.NUMBER, buffer.toString());
    }
    
    private void tokenizeHexNumber(int skipped) {
        clearBuffer();
        char current = peek(0);
        while (isHexNumber(current) || (current == '_')) {
            if (current != '_') {

                buffer.append(current);
            }
            current = next();
        }
        final int length = buffer.length();
        if (length > 0) {
            addToken(TokenType.HEX_NUMBER, buffer.toString());
        }
    }

    private static boolean isHexNumber(char current) {
        return Character.isDigit(current)
                || ('a' <= current && current <= 'f')
                || ('A' <= current && current <= 'F');
    }
    
    private void tokenizeOperator() {
        char current = peek(0);
        if (current == '/') {
            if (peek(1) == '/') {
                next();
                next();
                tokenizeComment();
                return;
            } else if (peek(1) == '*') {
                next();
                next();
                tokenizeMultilineComment();
                return;
            }
        }
        clearBuffer();
        while (true) {
            final String text = buffer.toString();
            if (!text.isEmpty() && !OPERATORS.containsKey(text + current)) {
                addToken(OPERATORS.get(text));
                return;
            }
            buffer.append(current);
            current = next();
        }
    }
    
    private void tokenizeWord() {
        clearBuffer();
        buffer.append(peek(0));
        char current = next();
        while (true) {
            if (!isLZRIdentifierPart(current)) {
                break;
            }
            buffer.append(current);
            current = next();
        }
        
        final String word = buffer.toString();
        if (KEYWORDS.containsKey(word)) {
            addToken(KEYWORDS.get(word));
        } else {
            addToken(TokenType.WORD, word);
        }
    }

    private void tokenizeExtendedWord() {
        next();// skip `
        clearBuffer();
        char current = peek(0);
        while (true) {
            if (current == '`') break;
            if (current == '\0') throw error("Reached end of file while parsing extended word.");
            if (current == '\n' || current == '\r') throw error("Reached end of line while parsing extended word.");
            buffer.append(current);
            current = next();
        }
        next(); // skip closing `
        addToken(TokenType.WORD, buffer.toString());
    }
    
    private void tokenizeText() {
        next();// skip "
        clearBuffer();
        char current = peek(0);
        while (true) {
            if (current == '\\') {
                current = next();
                switch (current) {
                    case '"': current = next(); buffer.append('"'); continue;
                    case '0': current = next(); buffer.append('\0'); continue;
                    case 'b': current = next(); buffer.append('\b'); continue;
                    case 'f': current = next(); buffer.append('\f'); continue;
                    case 'n': current = next(); buffer.append('\n'); continue;
                    case 'r': current = next(); buffer.append('\r'); continue;
                    case 't': current = next(); buffer.append('\t'); continue;
                    case 'u':
                        int rollbackPosition = pos;
                        while (current == 'u') current = next();
                        int escapedValue = 0;
                        for (int i = 12; i >= 0 && escapedValue != -1; i -= 4) {
                            if (isHexNumber(current)) {
                                escapedValue |= (Character.digit(current, 16) << i);
                            } else {
                                escapedValue = -1;
                            }
                            current = next();
                        }
                        if (escapedValue >= 0) {
                            buffer.append((char) escapedValue);
                        } else {
                            // rollback
                            buffer.append("\\u");
                            pos = rollbackPosition;
                        }
                        continue;
                }
                buffer.append('\\');
                continue;
            }
            if (current == '"') break;
            if (current == '\0') throw error("Reached end of file while parsing text string.");
            buffer.append(current);
            current = next();
        }
        next(); // skip closing "
        
        addToken(TokenType.TEXT, buffer.toString());
    }

    private static final Map<String, TokenType> OPERATORS;
    static {
        OPERATORS = new HashMap<>();

        OPERATORS.put("+", TokenType.PLUS);
        OPERATORS.put("-", TokenType.MINUS);
        OPERATORS.put("*", TokenType.STAR);
        OPERATORS.put("/", TokenType.SLASH);
        OPERATORS.put("%", TokenType.PERCENT);
        OPERATORS.put("(", TokenType.LPAREN);
        OPERATORS.put(")", TokenType.RPAREN);
        OPERATORS.put("[", TokenType.LBRACKET);
        OPERATORS.put("]", TokenType.RBRACKET);
        OPERATORS.put("{", TokenType.LBRACE);
        OPERATORS.put("}", TokenType.RBRACE);
        OPERATORS.put("=", TokenType.EQ);
        OPERATORS.put("<", TokenType.LT);
        OPERATORS.put(">", TokenType.GT);
        OPERATORS.put(".", TokenType.DOT);
        OPERATORS.put(",", TokenType.COMMA);
        OPERATORS.put("^", TokenType.CARET);
        OPERATORS.put("~", TokenType.TILDE);
        OPERATORS.put("?", TokenType.QUESTION);
        OPERATORS.put(":", TokenType.COLON);

        OPERATORS.put("!", TokenType.EXCL);
        OPERATORS.put("&", TokenType.AMP);
        OPERATORS.put("|", TokenType.BAR);

        OPERATORS.put("==", TokenType.EQEQ);
        OPERATORS.put("!=", TokenType.EXCLEQ);
        OPERATORS.put("<=", TokenType.LTEQ);
        OPERATORS.put(">=", TokenType.GTEQ);

        OPERATORS.put("+=", TokenType.PLUSEQ);
        OPERATORS.put("-=", TokenType.MINUSEQ);
        OPERATORS.put("*=", TokenType.STAREQ);
        OPERATORS.put("/=", TokenType.SLASHEQ);
        OPERATORS.put("%=", TokenType.PERCENTEQ);
        OPERATORS.put("&=", TokenType.AMPEQ);
        OPERATORS.put("^=", TokenType.CARETEQ);
        OPERATORS.put("|=", TokenType.BAREQ);
        OPERATORS.put("::=", TokenType.COLONCOLONEQ);
        OPERATORS.put("<<=", TokenType.LTLTEQ);
        OPERATORS.put(">>=", TokenType.GTGTEQ);
        OPERATORS.put(">>>=", TokenType.GTGTGTEQ);

        OPERATORS.put("++", TokenType.PLUSPLUS);
        OPERATORS.put("--", TokenType.MINUSMINUS);

        OPERATORS.put("::", TokenType.COLONCOLON);

        OPERATORS.put("&&", TokenType.AMPAMP);
        OPERATORS.put("||", TokenType.BARBAR);

        OPERATORS.put("<<", TokenType.LTLT);
        OPERATORS.put(">>", TokenType.GTGT);
        OPERATORS.put(">>>", TokenType.GTGTGT);

        OPERATORS.put("@", TokenType.AT);
        OPERATORS.put("@=", TokenType.ATEQ);
        OPERATORS.put("..", TokenType.DOTDOT);
        OPERATORS.put("**", TokenType.STARSTAR);
        OPERATORS.put("^^", TokenType.CARETCARET);
        OPERATORS.put("?:", TokenType.QUESTIONCOLON);
        OPERATORS.put("??", TokenType.QUESTIONQUESTION);
    }
    
    private void tokenizeComment() {
        char current = peek(0);
        while ("\r\n\0".indexOf(current) == -1) {
            current = next();
        }
     }
    
    private void tokenizeMultilineComment() {
        char current = peek(0);
        while (true) {
            if (current == '*' && peek(1) == '/') break;
            if (current == '\0') throw error("Reached end of file while parsing multiline comment");
            current = next();
        }
        next(); // *
        next(); // /
    }

    private boolean isLZRIdentifierPart(char current) {
        return (Character.isLetterOrDigit(current) || (current == '_') || (current == '$'));
    }

    private boolean isLZRIdentifier(char current) {
        return (Character.isLetter(current) || (current == '_') || (current == '$'));
    }


    
    private void clearBuffer() {
        buffer.setLength(0);
    }
    
    private char next() {
        pos++;
        final char result = peek(0);
        if (result == '\n') {
            row++;
            col = 1;
        } else col++;
        return result;
    }
    
    private char peek(int relativePosition) {
        final int position = pos + relativePosition;
        if (position >= length) return '\0';
        return input.charAt(position);
    }
    
    private void addToken(TokenType type) {
        addToken(type, "");
    }
    
    private void addToken(TokenType type, String text) {
        tokens.add(new Token(type, text, row, col));
    }
    
    private LZRException error(String text) {
        return new LZRException("Lexer exeption","Lexer error");
    }
}
