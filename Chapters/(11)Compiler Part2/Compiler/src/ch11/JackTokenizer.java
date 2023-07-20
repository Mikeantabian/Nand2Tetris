package ch11;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.File;

public class JackTokenizer {
    private BufferedReader JackFile;
    private List<String> tokens;
    private int currentTokenIndex;
    private String currentTokens;

    private static final String SYMBOLS = "{}()[].,;+-*/&|<>=~";

    public JackTokenizer(File jackFile) throws IOException {
        JackFile = new BufferedReader(new FileReader(jackFile));
        tokens = new ArrayList<>();
        currentTokenIndex = -1;
        tokenize();
    }

    public boolean hasMoreCommands() {
        return currentTokenIndex < tokens.size() - 1;
    }

    public void advance() {
        if (hasMoreCommands()) {
            currentTokenIndex++;
        }
    }

    public String getCurrentCommand() {
        if (currentTokenIndex >= 0 && currentTokenIndex < tokens.size()) {
            currentTokens = tokens.get(currentTokenIndex);
            return currentTokens;
        }
        return null;
    }

    private void tokenize() throws IOException {
        String line;
        while ((line = JackFile.readLine()) != null) {
            line = removeComments(line).trim();
            if (!line.isEmpty()) {
                splitLineIntoTokens(line);
            }
        }
        JackFile.close();
    }

    private String removeComments(String line) {
        int commentStartIndex = line.indexOf("//");
        if (commentStartIndex != -1) {
            line = line.substring(0, commentStartIndex);
        }

        int multilineCommentStartIndex = line.indexOf("/*");
        while (multilineCommentStartIndex != -1) {
            int multilineCommentEndIndex = line.indexOf("*/", multilineCommentStartIndex + 2);
            if (multilineCommentEndIndex != -1) {
                line = line.substring(0, multilineCommentStartIndex) + line.substring(multilineCommentEndIndex + 2);
                multilineCommentStartIndex = line.indexOf("/*");
            } else {
                // Handle the case when the multiline comment extends to the next line
                line = line.substring(0, multilineCommentStartIndex);
                break;
            }
        }

        line = line.replaceAll("/\\/.+|\\/\\*(?:.|[\\n\\r])*?\\*\\/", "").replaceAll("\\/\\*\\*.*\\*\\/", "").replaceAll("^\\s*\\*.*", "");

        return line.trim();
    }

    private void splitLineIntoTokens(String line) {
        StringBuilder currentToken = new StringBuilder();
        int i = 0;
        while (i < line.length()) {
            char c = line.charAt(i);
            if (Character.isWhitespace(c) || SYMBOLS.indexOf(c) != -1) {
                if (currentToken.length() > 0) {
                    tokens.add(currentToken.toString());
                    currentToken.setLength(0);
                }
                if (SYMBOLS.indexOf(c) != -1) {
                    tokens.add(Character.toString(c));
                }
            } else {
                currentToken.append(c);
            }
            i++;
        }
        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString());
        }
    }

    public List<String> getTokens() {
        return tokens;
    }


    protected static String tokenType(String currentToken){
        if (keywords.contains(currentToken)) {
            return "keyword";
        } else if (symbols.contains(currentToken)) {
            return "symbol";
        } else if (currentToken.matches("\\d+")) {
            return "integerConstant";
        } else if (currentToken.startsWith("\"") && currentToken.endsWith("\"")) {
            return "stringConstant";
        } else {
            return "identifier";
        }
    }

    protected String getKeyword(){
        if (tokenType(currentTokens).equals("keyword")){
            return currentTokens;
        } else {
            throw new IllegalArgumentException("This token is not a keyword");
        }
    }

    protected String getSymbol(){
        if (tokenType(currentTokens).equals("symbol")){
            return currentTokens;
        } else {
            throw new IllegalArgumentException("This token is not a symbol");
        }
    }

    protected String getIdentifier(){
        if (tokenType(currentTokens).equals("identifier")){
            return currentTokens;
        } else {
            throw new IllegalArgumentException("This token is not an identifier");
        }
    }

    protected String getIntConstant(){
        if (tokenType(currentTokens).equals("integerConstant")){
            return currentTokens;
        } else {
            throw new IllegalArgumentException("This token is not an integer constant");
        }
    }

    protected String getStringConstant(){
        if (tokenType(currentTokens).equals("stringConstant")){
            return currentTokens;
        } else {
            throw new IllegalArgumentException("This token is not a string constant");
        }
    }


    private static final List<String> keywords = Arrays.asList("class", "constructor", "function",
            "method", "field", "static", "var", "int", "char", "boolean", "void", "true",
            "false", "null", "this", "let", "do", "if", "else", "while", "return");

    private static final List<String> symbols = Arrays.asList("{", "}", "(", ")", "[", "]", ".",
            ",", ";", "+", "-", "*", "/", "&", "|", "<", ">", "=", "~");


    protected void close() throws IOException{
        JackFile.close();
    }
}
