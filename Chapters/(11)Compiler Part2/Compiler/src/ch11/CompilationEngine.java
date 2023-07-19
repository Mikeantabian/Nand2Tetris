package ch11;

import java.io.IOException;
import java.util.List;
import java.io.File;
import ch11.SymbolTable.Kind;

public class CompilationEngine {
    private List<String> tokens;
    private int tokenIndex;
    private VMWriter vmWriter;
    private SymbolTable symbolTable;
    private String className;
    private String CurrentSubroutineName;
    private int LabelIndex;

    public CompilationEngine(List<String> tokens, File outputFile) throws IOException{
        this.tokens = tokens;
        this.tokenIndex = 0;
        this.vmWriter = new VMWriter(outputFile);
        this.symbolTable = new SymbolTable();
        LabelIndex = 0;
    }

    private String getCurrentToken(){
        return tokens.get(tokenIndex);
    }

    private String getCurrentTokenAndAdvance() {
        String currentToken = getCurrentToken();
        tokenIndex++;
        return currentToken;
    }

    private String VMFunctionName() {
        
        if ( className != null && CurrentSubroutineName != null){
            return className + "." + CurrentSubroutineName;
        }
        return "";
    }

    private String getLabel() {
        return "Label_" + LabelIndex++;
    }

    public void compileClass() throws IOException {
        
        getCurrentTokenAndAdvance(); // move on to class name
        className = getCurrentTokenAndAdvance(); // save class name

        // writeOutIdentifier(); this was used to write the class name
        requireSymbol('{'); // this opens the body of the class
        compileClassVarDec();
        compileSubroutine();
        requireSymbol('}'); // this closes the body of the class

        vmWriter.close(); // temporary, because not sure for now
    }

    public void compileClassVarDec() throws IOException {
        if (getCurrentToken().equals("}")) {
            return; // End of class variable declaration
        }
       
        if(isClassVarDec()){
            Kind kindTemp = null;
            switch(getCurrentTokenAndAdvance()) {
                case "static":
                    kindTemp = Kind.STATIC;
                    break;
                case "field":
                    kindTemp = Kind.FIELD;
                    break;
            }
            String typeTemp = writeOutType(); // save type of variable
            // In case there are more than 1 variables defined with the same type and Kind: e.g. static int var1, var2, var3;
            do {
                String nameTemp = getCurrentTokenAndAdvance(); // save name of the variable
                symbolTable.Define(nameTemp,typeTemp,kindTemp);

                if (getCurrentToken().equals(",")) {
                    requireSymbol(',');
                }

            } while (!getCurrentToken().equals(";"));

            //writeVarName();
            requireSymbol(';');
            compileClassVarDec();
        }
    }

    public void compileSubroutine() throws IOException {
        if(isSubroutine()){
            symbolTable.StartSubroutine();
            
            // Subroutine example : Method int commission(int x);
            String SubroutineStyle = getCurrentTokenAndAdvance(); //Method, function, or constructor
            String SubroutineType = writeOutType(); // void, int, char, boolean or className;;
            CurrentSubroutineName = getCurrentTokenAndAdvance();

            // if subroutine is method, 'this' is its first argument
            if (SubroutineStyle.equals("method")) {
                symbolTable.Define("this", className, Kind.ARG);
            }    

            // check for parameters in subroutine
            requireSymbol('(');
            compileParameterList();
            requireSymbol(')');
    
            // go through body of subroutine
            requireSymbol('{');
            compileVarDec();
            compileFunctionPrep(SubroutineStyle);
            compileStatements();
            requireSymbol('}');
            
            compileSubroutine();
        }
    }

    public void compileParameterList() throws IOException {

        if(isType()){
            // Save first parameter as symbol
            String ParameterType1 = writeOutType();
            String ParameterName1 = getCurrentTokenAndAdvance();
            symbolTable.Define(ParameterName1, ParameterType1,Kind.ARG);

            // save the rest of the paremeters if there are any
            while(getCurrentToken().equals(",")){
                requireSymbol(',');
                String ParameterType = writeOutType();
                String ParameterName = getCurrentTokenAndAdvance();
                symbolTable.Define(ParameterName, ParameterType,Kind.ARG);
            }
        }


    }

    public void compileVarDec() throws IOException{
        if(isVarDec()){
            getCurrentTokenAndAdvance(); // Token here is 'var' just skip it
            String VarType = writeOutType(); // int, char, boolean, className

            // save all variables that are instantiated at the same time: var int v1, v2, v3;
            do {
                String nameTemp = getCurrentTokenAndAdvance(); // save name of the variable
                symbolTable.Define(nameTemp,VarType,Kind.VAR);

                if (getCurrentToken().equals(",")) {
                    requireSymbol(',');
                }

            } while (!getCurrentToken().equals(";"));

            requireSymbol(';');
            compileVarDec();
        }
    }

    public void compileFunctionPrep(String SubroutineStyle) throws IOException {
        String VMFunction = VMFunctionName();
        int varCount = symbolTable.VarCount(Kind.VAR);
        //Declare the VM function
        vmWriter.writeFunction(VMFunction, varCount);

        if (SubroutineStyle.equals("method")) {
            vmWriter.writePush("argument", 0);
            vmWriter.writePop("pointer", 0);
        } else if (SubroutineStyle.equals("constructor")) {
            int fieldCount = symbolTable.VarCount(Kind.FIELD);
            vmWriter.writePush("constant", fieldCount);
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop("pointer", 0);
        }
    }

    public void compileStatements() throws IOException {

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

    }

    public void compileDo() throws IOException {
        compileSubroutineCall();
        requireSymbol(';');
        vmWriter.writePop("temp",0);
    }

    public void compileLet() throws IOException {
        String LetVarName = getCurrentTokenAndAdvance(); // Name of the symbol being called by let;

        //verify for array
        if(getCurrentToken().equals("[")){
            requireSymbol('[');
            vmWriter.writePush(getSegment(symbolTable.KindOf(LetVarName)), symbolTable.IndexOf(LetVarName));
            compileExpression();
            requireSymbol(']');
            vmWriter.writeArithmetic("add"); //base + index for target address

            requireSymbol('=');
            compileExpression();

            //pop expression value to temp
            vmWriter.writePop("temp",0);
            //pop base+index into 'that'
            vmWriter.writePop("pointer",1);
            //pop expression value into *(base+index)
            vmWriter.writePush("temp",0);
            vmWriter.writePop("that",0);
        } else {
            requireSymbol('=');
            compileExpression();
            vmWriter.writePop(getSegment(symbolTable.KindOf(LetVarName)), symbolTable.IndexOf(LetVarName));
        }
        
        requireSymbol(';');
        
    }

    public void compileWhile() throws IOException {
        String TrueLabel = getLabel(); // condition is satisfied
        String FalseLabel = getLabel(); // condition is not satisfied

        vmWriter.writeLabel(TrueLabel);

        requireSymbol('(');
        compileExpression();
        requireSymbol(')');

        // if condition not satisfied, skip loop
        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(FalseLabel);

        requireSymbol('{');
        compileStatements(); 
        requireSymbol('}');
        // if condition is satisfied go back to top of the loop
        vmWriter.writeGoto(TrueLabel);
        //if condition not satisified exit loop and continue
        vmWriter.writeLabel(FalseLabel);
    }

    public void compileReturn() throws IOException {
        if(!getCurrentToken().equals(";")){
            compileExpression();
        } else {
            vmWriter.writePush("constant", 0);
        }
        requireSymbol(';');
        vmWriter.writeReturn();
    }

    public void compileIf() throws IOException {

        String gotoElse = getLabel();
        String finishStatement = getLabel();

        requireSymbol('(');
        compileExpression();
        requireSymbol(')');

        // if condition not satisfied, skip loop
        vmWriter.writeArithmetic("not");
        vmWriter.writeIf(gotoElse);

        requireSymbol('{');
        compileStatements(); 
        requireSymbol('}');
        // when 'if' statement properly executed
        vmWriter.writeGoto(finishStatement);
        //if skipped to else
        vmWriter.writeLabel(gotoElse);

        if(getCurrentToken().equals("else")){
            getCurrentTokenAndAdvance();
            requireSymbol('{');
            compileStatements(); 
            requireSymbol('}');
        }

        vmWriter.writeLabel(finishStatement);
    }

    public void compileExpression() throws IOException {
        compileTerm();
        while (isOp()) {
            String operator = getCurrentTokenAndAdvance();
            String Arithmetic = OpToArithmetic(operator);
            compileTerm();
            vmWriter.writeArithmetic(Arithmetic);
        }
    }

    public void compileTerm() throws IOException {
        String currentToken = getCurrentToken();

        if(isIntegerConstant()) {
            vmWriter.writePush("constant", Integer.parseInt(currentToken));
            getCurrentTokenAndAdvance();
        } else if (isStringConstant()) {
            compileStringConstant();
        } else if (isKeywordConstant()) {
            PushKeywordConstant();
            getCurrentTokenAndAdvance();
        } else if (currentToken.equals("(")) {
            requireSymbol('(');
            compileExpression();
            requireSymbol(')');
        } else if (isUnaryOp()) {
            String unaryOp = getCurrentTokenAndAdvance();
            compileTerm();
            if(unaryOp.equals("~")){
                vmWriter.writeArithmetic("not");
            } else {
                vmWriter.writeArithmetic("neg");
            }
        } else {
            //check the next token without advancing to it
            String nextToken = peek();

            if (nextToken.equals("[")) { // array 
                getCurrentTokenAndAdvance();
                requireSymbol('[');
                vmWriter.writePush(getSegment(symbolTable.KindOf(currentToken)), symbolTable.IndexOf(currentToken)); // push array base memory address
                compileExpression();
                requireSymbol(']');
                vmWriter.writeArithmetic("add"); // add array base address + array index : this is memory location of value being accessed by array
                vmWriter.writePop("pointer",1); // pop memory location into THAT pointer
                vmWriter.writePush("that",0);
            } else if (nextToken.equals("(") || nextToken.equals(".")) { // subroutine call
                compileSubroutineCall();
            } else {
                vmWriter.writePush(getSegment(symbolTable.KindOf(currentToken)), symbolTable.IndexOf(currentToken));
                getCurrentTokenAndAdvance();
            }
        }
    }

    public int compileExpressionList() throws IOException {
        int ArgCount = 0;

        if(!getCurrentToken().equals(")")){
            compileExpression();
            ArgCount = 1;

            while (getCurrentToken().equals(",")){
                requireSymbol(',');
                compileExpression();
                ArgCount++;
            }
        }
        return ArgCount;
    }

    public void compileSubroutineCall() throws IOException {
        String currentName = getCurrentTokenAndAdvance();
        int numArgs = 0;

        if(getCurrentToken().equals(".")){
            compileSubWithClassOrVar(currentName, numArgs);
        } else if (getCurrentToken().equals("(")) {
            compileMethodCall(currentName, numArgs);
        } else {
            throw new IllegalArgumentException("Wrong symbol: " + getCurrentToken());
        }
    }

    private void compileMethodCall(String name, int numArgs) throws IOException {
        requireSymbol('(');
        vmWriter.writePush("pointer",0); // push this as 1 arg for method
        numArgs = compileExpressionList() + 1; // add 1 for 'this' pointer which is 1st arg
        requireSymbol(')');
        vmWriter.writeCall(className + "." + name, numArgs);
    }

    private void compileSubWithClassOrVar(String callerName, int numArgs) throws IOException {
        requireSymbol('.');
        String SubName = getCurrentTokenAndAdvance();
        String type = symbolTable.TypeOf(callerName);

        if(type != null) {
            if(type.isEmpty()) {
            SubName = callerName + "." + SubName;
            } else if (type != null && isRegularType(type)) {
                throw new IOException("Not a class or object: " + type);
            } else {
                numArgs = 1;
                vmWriter.writePush(getSegment(symbolTable.KindOf(callerName)), symbolTable.IndexOf(callerName));
                SubName = type + "." + SubName;
            }
        } else {
            SubName = callerName + "." + SubName;
        }

        requireSymbol('(');
        numArgs += compileExpressionList();
        requireSymbol(')');
        vmWriter.writeCall(SubName,numArgs);
    }

    private boolean isRegularType(String type) {
        return type.equals("int") || type.equals("boolean") || type.equals("char") || type.equals("void");
    }

    private String getSegment (Kind kind) {
        switch (kind) {
            case ARG:
                return "argument";
            case VAR:
                return "local";
            case STATIC:
                return "static";
            case FIELD:
                return "this";
            default:
                return "none";
        }
    }

    private void PushKeywordConstant() throws IOException {
        String currentToken = getCurrentToken();

        switch (currentToken) {
            case "true":
                vmWriter.writePush("constant", 1);
                vmWriter.writeArithmetic("neg");
                break;
            case "false":
            case "null":
                vmWriter.writePush("constant",0);
                break;
            case "this":
                vmWriter.writePush("pointer",0);
                break;
            default:
                throw new IllegalArgumentException("Not a keyword constant: " + currentToken);
        }
    }


    private String writeOutType() throws IOException {
        String currentToken = getCurrentToken();
        String typeTemp;

        if (isType() || currentToken.equals("void")) {
            typeTemp = currentToken;
            getCurrentTokenAndAdvance();
            return typeTemp;
        }
        getCurrentTokenAndAdvance();
        return null;
    }

    private boolean isType() {
        String token = getCurrentToken();
        return token.equals("int") || token.equals("char") || token.equals("boolean") || token.equals(className) || JackTokenizer.tokenType(token).equals("identifier");
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

    }

    private String OpToArithmetic(String operator) {
        switch(operator){
            case "+":
                return "add";
            case "-":
                return "sub";
            case "=":
                return "eq";
            case ">":
                return "gt";
            case "<":
                return "lt";
            case "&":
                return "and";
            case "|":
                return "or";
            case "/":
                return "call Math.divide 2";
            case "*":
                return "call Math.multiply 2";
            default:
                throw new IllegalArgumentException("Wrong operator: " + operator);
        }
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

        // String constant officialy transformed to String without quotes
        String constantValue = stringConstant.substring(1, stringConstant.length()-1);
    
        // Push String Constant by appending each char one by one like before
        vmWriter.writePush("constant",constantValue.length());
        vmWriter.writeCall("String.new", 1); //create new String object in VM

        for (int i = 0; i < constantValue.length(); i++) {
            char c = constantValue.charAt(i);
            vmWriter.writePush("constant", (int)c);
            vmWriter.writeCall("String.appendChar", 2);
        }
    }
    
}
