package ch10;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.io.File;

public class CompilationEngine {
    private List<String> tokens;
    private int tokenIndex;
    private BufferedWriter outputFile;

    public CompilationEngine(List<String> tokens, File outputFile) throws IOException{
        this.tokens = tokens;
        this.tokenIndex = 0;
        this.outputFile = new BufferedWriter(new FileWriter(outputFile));
    }

    private String getCurrentToken(){
        return tokens.get(tokenIndex);
    }

    private String getCurrentTokenAndAdvance() {
        String currentToken = getCurrentToken();
        tokenIndex++;
        return currentToken;
    }

    private String className;
    public void compileClass() throws IOException {
        writeOut("<class>\n");
        writeOut("<keyword> class </keyword>\n");
        
        getCurrentTokenAndAdvance(); // move on to class name
        className = getCurrentToken(); // save class name

        writeOutIdentifier();
        requireSymbol('{'); 
        compileClassVarDec();
        compileSubroutine();
        requireSymbol('}');
        writeOut("</class>\n");
    }

    public void compileClassVarDec() throws IOException {
        if (getCurrentToken().equals("}")) {
            return; // End of class variable declaration
        }
       
        if(isClassVarDec()){
            writeOut("<classVarDec>\n");
            writeOut("<keyword> " + getCurrentTokenAndAdvance() + "</keyword>\n");
            writeOutType();
            writeVarName();
            requireSymbol(';');
            writeOut("</classVarDec>\n");
            compileClassVarDec();
        }
    }

    public void compileSubroutine() throws IOException {
        if(isSubroutine()){
            writeOut("<subroutineDec>\n");
            writeOut("<keyword> " + getCurrentTokenAndAdvance() + " </keyword>\n"); 
            writeOutType();
            writeOutIdentifier();
            requireSymbol('(');
            compileParameterList();
            requireSymbol(')');
            writeOut("<subroutineBody>\n");
            requireSymbol('{');
            compileVarDec();
            compileStatements();
            requireSymbol('}');
            writeOut("</subroutineBody>\n");
            writeOut("</subroutineDec>\n");
            compileSubroutine();
        }
    }

    public void compileParameterList() throws IOException {
        writeOut("<parameterList>\n");

        if(isType()){
            writeOutType();
            writeOutIdentifier();

            while(getCurrentToken().equals(",")){
                writeOut("<symbol> , </symbol>\n");
                getCurrentTokenAndAdvance();
                writeOutType();
                writeOutIdentifier();
            }
        }

        writeOut("</parameterList>\n");
    }

    public void compileVarDec() throws IOException{
        if(isVarDec()){
            writeOut("<varDec>\n");
            writeOut("<keyword> " + getCurrentTokenAndAdvance() + " </keyword>\n");
            writeOutType();
            writeVarName();
            requireSymbol(';');
            writeOut("</varDec>\n");
            compileVarDec();
        }
    }

    public void compileStatements() throws IOException {
        writeOut("<statements>\n");

        while(isStatement()){
            String token = getCurrentTokenAndAdvance();
            switch(token){
                case "let":
                    compileLet();
                    break;
                case "if":
                    compileIf();
                    break;
                case "while":
                    compileWhile();
                    break;
                case "do":
                    compileDo();
                    break;
                case "return":
                    compileReturn();
                    break;
                default:
                    throw new RuntimeException("Invalid Statement token " + token);
            }
        }
        writeOut("</statements>\n");

    }

    public void compileDo() throws IOException {
        writeOut("<doStatement>\n");
        writeOut("<keyword> do </keyword>\n");
        compileSubroutineCall();
        requireSymbol(';');
        writeOut("</doStatement>\n");
    }

    public void compileLet() throws IOException {
        writeOut("<letStatement>\n");
        writeOut("<keyword> let </keyword>\n");
        writeOutIdentifier();

        if(getCurrentToken().equals("[")){
            requireSymbol('[');
            compileExpression();
            requireSymbol(']');
        }
        
        requireSymbol('=');
        compileExpression();
        requireSymbol(';');
        writeOut("</letStatement>\n");
    }

    public void compileWhile() throws IOException {
        writeOut("<whileStatement>\n");
        writeOut("<keyword> while </keyword>\n");
        requireSymbol('(');
        compileExpression();
        requireSymbol(')');
        requireSymbol('{');
        compileStatements(); 
        requireSymbol('}');
        writeOut("</whileStatement>\n");
    }

    public void compileReturn() throws IOException {
        writeOut("<returnStatement>\n");
        writeOut("<keyword> return </keyword>\n");

        if(!getCurrentToken().equals(";")){
            compileExpression();
        }

        requireSymbol(';');
        writeOut("</returnStatement>\n");
    }

    public void compileIf() throws IOException {
        writeOut("<ifStatement>\n");
        writeOut("<keyword> if </keyword>\n");

        requireSymbol('(');
        compileExpression();
        requireSymbol(')');
        requireSymbol('{');
        compileStatements(); 
        requireSymbol('}');

        if(getCurrentToken().equals("else")){
            writeOut("<keyword> else </keyword>\n");
            getCurrentTokenAndAdvance();
            requireSymbol('{');
            compileStatements(); 
            requireSymbol('}');
        }

        writeOut("</ifStatement>\n");
    }

    public void compileExpression() throws IOException {
        writeOut("<expression>\n");
        compileTerm();

        while (isOp()) {
            if (getCurrentToken().equals("<") || getCurrentToken().equals(">") || getCurrentToken().equals("&")  || getCurrentToken().equals("\"") ) {
                writeOut("<symbol> " + escapeSymbol(getCurrentTokenAndAdvance()) + " </symbol>\n");
            } else {
                writeOut("<symbol> " + getCurrentTokenAndAdvance() + " </symbol>\n");
            }
            compileTerm();
        }
        writeOut("</expression>\n");
    }

    public void compileTerm() throws IOException {
        writeOut("<term>\n");
        String currentToken = getCurrentToken();

        if(isIntegerConstant()) {
            writeOut("<integerConstant> " + getCurrentTokenAndAdvance() + " </integerConstant>\n");
        } else if (isStringConstant()) {
            compileStringConstant();
        } else if (isKeywordConstant()) {
            writeOut("<keyword> " + getCurrentTokenAndAdvance() + " </keyword>\n");
        } else if (currentToken.equals("(")) {
            requireSymbol('(');
            compileExpression();
            requireSymbol(')');
        } else if (isUnaryOp()) {
            writeOut("<symbol> " + getCurrentTokenAndAdvance() + " </symbol>\n");
            compileTerm();
        } else {
            //check the next token without advancing to it
            String nextToken = peek();

            if (nextToken.equals("[")) {
                writeOutIdentifier();
                requireSymbol('[');
                compileExpression();
                requireSymbol(']');
            } else if (nextToken.equals("(") || nextToken.equals(".")) {
                compileSubroutineCall();
            } else {
                writeOutIdentifier();
            }
        }

        writeOut("</term>\n");
    }

    public void compileExpressionList() throws IOException {
        writeOut("<expressionList>\n");

        if(!getCurrentToken().equals(")")){
            compileExpression();

            while (getCurrentToken().equals(",")){
                requireSymbol(',');
                compileExpression();
            }
        }
        writeOut("</expressionList>\n");
    }

    public void compileSubroutineCall() throws IOException {
        writeOutIdentifier();

        if(getCurrentToken().equals(".")){
            requireSymbol('.');
            writeOutIdentifier();
        }

        requireSymbol('(');
        compileExpressionList();
        requireSymbol(')');

    }

    protected void writeOut(String outputText) throws IOException {
        outputFile.write(outputText);
    }

    private void writeOutType() throws IOException {
        String currentToken = getCurrentToken();


        if (isType() || currentToken.equals("void")) {
            if(!currentToken.equals(className)){
                writeOut("<keyword> " + currentToken + " </keyword>\n");
            } else {
                writeOut("<identifier> " + currentToken + " </identifier>\n");
            }
            
        } else {
            writeOut("<identifier> " + currentToken + " </identifier>\n");
        }
        getCurrentTokenAndAdvance();
    }

    private void writeVarName() throws IOException {
        writeOutIdentifier();
        while(getCurrentToken().equals(",")){
            requireSymbol(',');
            writeOutIdentifier();
        }
    }

    private void writeOutIdentifier() throws IOException {
        writeOut("<identifier> " + getCurrentTokenAndAdvance() + " </identifier>\n");
    }

    private boolean isType() {
        String token = getCurrentToken();
        return token.equals("int") || token.equals("char") || token.equals("boolean") || token.equals(className);
    }

    private boolean isClassVarDec() {
        String token = getCurrentToken();
        return token.equals("static") || token.equals("field");
    }

    private boolean isSubroutine() {
        String token = getCurrentToken();
        return token.equals("constructor") || token.equals("function") || token.equals("method");
    }

    private boolean isVarDec() {
        return getCurrentToken().equals("var");
    }

    private boolean isStatement() {
        String token = getCurrentToken();
        return token.equals("let") || token.equals("if") || token.equals("while") ||
                token.equals("do") || token.equals("return");
    }

    private boolean isOp() {
        String token = getCurrentToken();
        return token.equals("+") || token.equals("-") || token.equals("*") ||
                token.equals("/") || token.equals("&") || token.equals("|") ||
                token.equals("<") || token.equals(">") || token.equals("=");
    }

    private boolean isIntegerConstant() {
        try {
            Integer.parseInt(getCurrentToken());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isStringConstant() {
        String token = getCurrentToken();
        return token.startsWith("\"");
    }

    private boolean isKeywordConstant() {
        String token = getCurrentToken();
        return token.equals("true") || token.equals("false") ||
                token.equals("null") || token.equals("this");
    }

    private boolean isUnaryOp() {
        String token = getCurrentToken();
        return token.equals("-") || token.equals("~");
    }

    private void requireSymbol(char symbol) throws IOException {
        String token = getCurrentTokenAndAdvance();

        if (!token.equals(Character.toString(symbol))) {
            throw new RuntimeException("Expected symbol: " + symbol + ", Found: " + token);
        }

        writeOut("<symbol> " + token + " </symbol>\n");
    }

    private String peek() {
        if (tokenIndex < tokens.size()) {
            tokenIndex++;
            int currentIndex = tokenIndex;
            String nextToken = tokens.get(currentIndex);
            tokenIndex--;
            return nextToken;
        } else {
            return null;
        }
    }


    private String escapeSymbol(String symbol) {
        if (symbol.equals("<")) {
            return "&lt;";
        } else if (symbol.equals(">")) {
            return "&gt;";
        } else if (symbol.equals("\"")) {
            return "&quot;";
        } else if (symbol.equals("&")) {
            return "&amp;";
        } else {
            return symbol;
        }
    }

    private void compileStringConstant() throws IOException {
        StringBuilder stringConstant = new StringBuilder();
    
        // Append the current token (with leading quote) to the string constant
        stringConstant.append(getCurrentTokenAndAdvance());
    
        // Keep appending tokens until a token with trailing quote is found
        while (!getCurrentToken().endsWith("\"")) {
            stringConstant.append(" ");
            stringConstant.append(getCurrentTokenAndAdvance());
        }
    
        // Append the last token (with trailing quote) to the string constant
        stringConstant.append(" ");
        stringConstant.append(getCurrentTokenAndAdvance());

        String constantValue = stringConstant.substring(1, stringConstant.length()-1);
    
        writeOut("<stringConstant> " + constantValue + " </stringConstant>\n");
    }
    
    protected void close() throws IOException{
        outputFile.close();
    }
}
