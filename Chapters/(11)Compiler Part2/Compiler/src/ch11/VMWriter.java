package ch11;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;


public class VMWriter {
    
    private BufferedWriter vmWriter;
    
    public VMWriter() {} //default constructor
    
    public VMWriter(File vmWriter) throws IOException{
        this.vmWriter = new BufferedWriter(new FileWriter(vmWriter));
    }


    private Boolean isArithmeticCommand(String command){
        return command.equals("add") || command.equals("sub") || command.equals("neg") ||
           command.equals("eq") || command.equals("gt") || command.equals("lt") ||
           command.equals("and") || command.equals("or") || command.equals("not") || 
           command.equals("call Math.divide 2") || command.equals("call Math.multiply 2");
    }

    private Boolean isSegmentCommand(String command){
        return command.equals("constant") || command.equals("argument") || command.equals("local") ||
           command.equals("static") || command.equals("this") || command.equals("that") ||
           command.equals("pointer") || command.equals("temp");
    }

    protected void writeCommand(String command) throws IOException{
        vmWriter.write(command + "\n");
    }
    
    protected void writeArithmetic(String command) throws IOException{
        
        if (isArithmeticCommand(command)) {
            vmWriter.write(command + "\n");
        } else {
            throw new IllegalArgumentException("Unsupported command: " + command);
        }

    }

    protected void writePush(String Segment, int index) throws IOException{
        if (isSegmentCommand(Segment)) {
            vmWriter.write("push " + Segment + " " + index + "\n");
        } else {
            throw new IllegalArgumentException("Unsupported Segment: " + Segment);
        }
    }

    protected void writePop(String Segment, int index) throws IOException{
        if (isSegmentCommand(Segment)) {
            vmWriter.write("pop " + Segment + " " + index + "\n");
        } else {
            throw new IllegalArgumentException("Unsupported Segment: " + Segment);
        }
    }

    protected void writeLabel(String label) throws IOException{
        vmWriter.write("label " +  label + "\n");
    }

    protected void writeGoto(String label) throws IOException{
        vmWriter.write("goto " +  label + "\n");
    }

    protected void writeIf(String label) throws IOException{
        vmWriter.write("if-goto " +  label + "\n");
    }

    protected void writeCall(String functionName, int numArgs) throws IOException{
        vmWriter.write("call " +  functionName + " " + Integer.toString(numArgs) + "\n");
    }

    protected void writeFunction(String functionName, int numLocals) throws IOException{
        vmWriter.write("function " +  functionName + " " + Integer.toString(numLocals) + "\n");
    }

    protected void writeReturn() throws IOException{
        vmWriter.write("return" + "\n");
    }

    protected void close() throws IOException {
        vmWriter.close();
    }
}
