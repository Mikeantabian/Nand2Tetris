package ch7;

import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {
    private FileWriter asmFile;
    private int labelCount;
    private int returnAddressIndex;
    private String currentFileName;
    private String currentFunctionName;

    public CodeWriter(String pathToAsmFile) throws IOException{
        asmFile = new FileWriter(pathToAsmFile);
        labelCount = 0;
        currentFileName = "";
        currentFunctionName = "";
    }

    public void writeInit() throws IOException{
        // Write bootstrap code
        asmFile.write("@256\n");
        asmFile.write("D=A\n");
        asmFile.write("@SP\n");
        asmFile.write("M=D\n");
        writeCall("Sys.init", 0);
    }

    public void setFileName(String fileName){
        currentFileName = fileName;
    }

    public void setFunctionName(String functionName){
        currentFunctionName = functionName;
    }

    public Boolean isArithmeticCommand(String command){
        return command.equals("add") || command.equals("sub") || command.equals("neg") ||
           command.equals("eq") || command.equals("gt") || command.equals("lt") ||
           command.equals("and") || command.equals("or") || command.equals("not");
    }

    public void writeArithmetic(String command) throws IOException{
        switch(command){
            case "add":
                commandTranslator.translateAdd(asmFile);
                break;
            case "sub":
                commandTranslator.translateSub(asmFile);
                break;
            case "neg":
                commandTranslator.translateNeg(asmFile);
                break;
            case "eq":
                commandTranslator.translateEQ(asmFile, labelCount);
                labelCount++;
                break;
            case "gt":
                commandTranslator.translateGT(asmFile, labelCount);
                labelCount++;
                break;
            case "lt":
                commandTranslator.translateLT(asmFile, labelCount);
                labelCount++;
                break;
            case "and":
                commandTranslator.translateAND(asmFile);
                break;
            case "or":
                commandTranslator.translateOR(asmFile);
                break;
            case "not":
                commandTranslator.translateNot(asmFile);
                break;
            default:
                throw new IllegalArgumentException("Unsupported command: " + command);
        }

    }

    public void writePush(String Segment, int index) throws IOException{
        switch(Segment){
            case "local":
                commandTranslator.translatePushMainSegments(asmFile, index, "LCL");
                break;
            case "argument":
                commandTranslator.translatePushMainSegments(asmFile, index, "ARG");
                break;
            case "this":
                commandTranslator.translatePushMainSegments(asmFile,index, "THIS");
                break;
            case "that":
                commandTranslator.translatePushMainSegments(asmFile,index, "THAT");
                break;
            case "static":
                commandTranslator.translatePushStatic(asmFile, index, currentFileName);
                break;
            case "constant":
                commandTranslator.translatePushConstant(asmFile,index);
                break;
            case "pointer":
                commandTranslator.translatePushPointers(asmFile, index);
                break;
            case "temp":
                commandTranslator.translatePushTemp(asmFile, index);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Segment: " + Segment);
        }
        asmFile.write("@SP\n");
        asmFile.write("AM=M+1\n"); 
        asmFile.write("A=A-1\n"); 
        asmFile.write("M=D\n");
    }

    public void writePop(String Segment, int index) throws IOException{
        switch(Segment){
            case "local":
                commandTranslator.translatePopMainSegments(asmFile, index, "LCL");
                break;
            case "argument":
                commandTranslator.translatePopMainSegments(asmFile, index, "ARG");
                break;
            case "this":
                commandTranslator.translatePopMainSegments(asmFile,index, "THIS");
                break;
            case "that":
                commandTranslator.translatePopMainSegments(asmFile,index, "THAT");
                break;
            case "static":
                commandTranslator.translatePopStatic(asmFile, index, currentFileName);
                break;
            case "constant":
                commandTranslator.translatePopConstant(asmFile, index);
                break;
            case "pointer":
                commandTranslator.translatePopPointer(asmFile, index);
                break;
            case "temp":
                commandTranslator.translatePopTemp(asmFile, index);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Segment: " + Segment);
        }
        asmFile.write("@R13\n"); // Store the target address in R13 as a temporary variable
        asmFile.write("M=D\n");
        asmFile.write("@SP\n");
        asmFile.write("AM=M-1\n"); // Decrement the stack pointer
        asmFile.write("D=M\n"); // Get the value from the top of the stack
        asmFile.write("@R13\n");
        asmFile.write("A=M\n"); // Load the target address from R13 into A
        asmFile.write("M=D\n"); // Store the value in the target address
    }

    public void writeLabel(String label) throws IOException{
        commandTranslator.translateLabel(asmFile, currentFunctionName, label);
    }

    public void writeGoto(String label) throws IOException{
        commandTranslator.translateGoto(asmFile, currentFunctionName, label);
        asmFile.write("0;JMP\n");
    }

    public void writeIf(String label) throws IOException{
        asmFile.write("@SP\n");
        asmFile.write("AM=M-1\n");
        asmFile.write("D=M\n");
        commandTranslator.translateGoto(asmFile, currentFunctionName, label);
        asmFile.write("D;JNE\n");
    }

    public void writeCall(String functionName, int numArgs) throws IOException{
        commandTranslator.translateCall(asmFile, functionName, numArgs, returnAddressIndex);
        returnAddressIndex++;
    }

    public void writeFunction(String functionName, int numLocals) throws IOException{
        asmFile.write("(" + functionName + ")\n");
        for(int i = 0; i<numLocals; i++){
            writePush("constant", 0);
        }
    }

    public void writeReturn() throws IOException{
        commandTranslator.translateReturn(asmFile);
    }

    public void close() throws IOException {
        asmFile.close();
    }
}
