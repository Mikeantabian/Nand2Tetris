package ch7;

import java.io.FileWriter;
import java.io.IOException;

public class commandTranslator {

    //////////////////////////Arithmetic Portion///////////////////////////////

    protected static void translateAdd(FileWriter asmFile) throws IOException {
        // Retrieving value from the stack
        asmFile.write("@SP\n");
        asmFile.write("AM=M-1\n");
        asmFile.write("D=M\n");
        asmFile.write("A=A-1\n");
        // Perfom addition
        asmFile.write("M=D+M\n");
    }

    protected static void translateSub(FileWriter asmFile) throws IOException {
        // Retrieving value from the stack
        asmFile.write("@SP\n");
        asmFile.write("AM=M-1\n");
        asmFile.write("D=M\n");
        asmFile.write("A=A-1\n");
        // Perfom substraction
        asmFile.write("M=M-D\n");
    }
    
    protected static void translateNeg(FileWriter asmFile) throws IOException {
        asmFile.write("@SP\n");
        asmFile.write("A=M-1\n");
        //negate the value at the top of the stack
        asmFile.write("M=-M\n");
    }

    protected static void translateEQ(FileWriter asmFile, int labelCount) throws IOException {
        // Retrieving value from the stack
        asmFile.write("@SP\n");
        asmFile.write("AM=M-1\n");
        asmFile.write("D=M\n");
        asmFile.write("A=A-1\n");
        // Perfom comparison
        asmFile.write("D=M-D\n");
        //M = -1 if true, 0 if false
        asmFile.write("@True"+ labelCount + "\n");
        asmFile.write("D;JEQ\n");
        asmFile.write("@SP\n");
        asmFile.write("A=M-1\n");
        asmFile.write("M=0\n");
        asmFile.write("@END"+ labelCount + "\n");
        asmFile.write("0;JMP\n");
        asmFile.write("(True"+ labelCount +")\n");
        asmFile.write("@SP\n");
        asmFile.write("A=M-1\n");
        asmFile.write("M=-1\n");
        asmFile.write("(END"+ labelCount +")\n");
    }

    protected static void translateGT(FileWriter asmFile, int labelCount) throws IOException {
        // Retrieving value from the stack
        asmFile.write("@SP\n");
        asmFile.write("AM=M-1\n");
        asmFile.write("D=M\n");
        asmFile.write("A=A-1\n");
        // Perfom comparison
        asmFile.write("D=M-D\n");
        //M = -1 if true, 0 if false
        asmFile.write("@True"+ labelCount + "\n");
        asmFile.write("D;JGT\n");
        asmFile.write("@SP\n");
        asmFile.write("A=M-1\n");
        asmFile.write("M=0\n");
        asmFile.write("@END"+ labelCount + "\n");
        asmFile.write("0;JMP\n");
        asmFile.write("(True"+ labelCount +")\n");
        asmFile.write("@SP\n");
        asmFile.write("A=M-1\n");
        asmFile.write("M=-1\n");
        asmFile.write("(END"+ labelCount +")\n");
    }

    protected static void translateLT(FileWriter asmFile, int labelCount) throws IOException {
        // Retrieving value from the stack
        asmFile.write("@SP\n");
        asmFile.write("AM=M-1\n");
        asmFile.write("D=M\n");
        asmFile.write("A=A-1\n");
        // Perfom comparison
        asmFile.write("D=M-D\n");
        //M = -1 if true, 0 if false
        asmFile.write("@True"+ labelCount + "\n");
        asmFile.write("D;JLT\n");
        asmFile.write("@SP\n");
        asmFile.write("A=M-1\n");
        asmFile.write("M=0\n");
        asmFile.write("@END"+ labelCount + "\n");
        asmFile.write("0;JMP\n");
        asmFile.write("(True"+ labelCount +")\n");
        asmFile.write("@SP\n");
        asmFile.write("A=M-1\n");
        asmFile.write("M=-1\n");
        asmFile.write("(END"+ labelCount +")\n");
    }

    protected static void translateAND(FileWriter asmFile) throws IOException {
        // Retrieving value from the stack
        asmFile.write("@SP\n");
        asmFile.write("AM=M-1\n");
        asmFile.write("D=M\n");
        asmFile.write("A=A-1\n");
        // Perfom logical AND
        asmFile.write("M=D&M\n");
    }

    protected static void translateOR(FileWriter asmFile) throws IOException {
        // Retrieving value from the stack
        asmFile.write("@SP\n");
        asmFile.write("AM=M-1\n");
        asmFile.write("D=M\n");
        asmFile.write("A=A-1\n");
        // Perfom logical Or
        asmFile.write("M=D|M\n");
    }

    protected static void translateNot(FileWriter asmFile) throws IOException {
        asmFile.write("@SP\n");
        asmFile.write("A=M-1\n");
        //Not the value at the top of the stack
        asmFile.write("M=!M\n");
    }

    //////////////////////////////////Push Semgents//////////////////////////////////
    
    protected static void translatePushMainSegments(FileWriter asmFile, int index, String Name) throws IOException {
        asmFile.write("@"+ Name +"\n"); // Load the base address of the segment
        asmFile.write("D=M\n"); // Store the base address in the D register
        asmFile.write("@"+ index + "\n"); //index address
        asmFile.write("D=D+A\n"); // Calculate the memory address (base address + index)
        asmFile.write("A=D\n"); // Load the value at the calculated memory address into the D register
        asmFile.write("D=M\n");

    }

    protected static void translatePushPointers(FileWriter asmFile,int index) throws IOException {
        String type;
        switch (index) {
            case 0:
                type = "THIS";
                break;
            case 1:
                type = "THAT";
                break;
            default:
                throw new IllegalArgumentException("Invalid value. Only 0 or 1 are allowed.");
        }
        
        asmFile.write("@"+ type + "\n");
        asmFile.write("D=M\n");
    }

    protected static void translatePushStatic(FileWriter asmFile,int index, String fileName) throws IOException {
        asmFile.write("@" + fileName + "." + index +"\n");
        asmFile.write("D=M\n");

    }

    protected static void translatePushConstant(FileWriter asmFile, int index) throws IOException {
        asmFile.write("@"+ index + "\n");
        asmFile.write("D=A\n");

    }

    protected static void translatePushTemp(FileWriter asmFile, int index) throws IOException {
        if(index >= 0 && index <= 7){
            asmFile.write("@5\n"); // Load the base address of the segment
            asmFile.write("D=A\n"); // Store the base address in the D register
            asmFile.write("@"+ index + "\n"); //index address
            asmFile.write("D=D+A\n"); // Calculate the memory address (base address + index)
            asmFile.write("A=D\n"); 
            asmFile.write("D=M\n");// Load the value at the calculated memory address into the D register
        } else{
            throw new IllegalArgumentException("Invalid temp index: " + index);
        }
    }

    ////////////////////////////////Pop Segments////////////////////////////////////

    protected static void translatePopMainSegments(FileWriter asmFile, int index, String Name) throws IOException {
        asmFile.write("@" + Name + "\n"); // Load the base address of the segment into A
        asmFile.write("D=M\n");
        asmFile.write("@" + index + "\n"); // Load the index 'i' into register D
        asmFile.write("D=D+A\n"); // Calculate the target address: base address + index
        
    }

    protected static void translatePopStatic(FileWriter asmFile, int index, String fileName) throws IOException {
        asmFile.write("@" + fileName + "." + index +"\n"); // Load the address of the static variable into A
        asmFile.write("D=A\n");// Load the value from the static variable's RAM address into D
    }

    protected static void translatePopConstant(FileWriter asmFile, int index) throws IOException {
        asmFile.write("@"+ index + "\n");
        asmFile.write("D=A\n");
    }

    protected static void translatePopPointer(FileWriter asmFile, int index) throws IOException {
        String type;
        switch (index) {
            case 0:
                type = "THIS";
                break;
            case 1:
                type = "THAT";
                break;
            default:
                throw new IllegalArgumentException("Invalid value. Only 0 or 1 are allowed.");
        }
        
        asmFile.write("@"+ type + "\n");
        asmFile.write("D=A\n");
    }

    protected static void translatePopTemp(FileWriter asmFile, int index) throws IOException {
        if(index >= 0 && index <= 7){
            asmFile.write("@5\n"); // Load the base address of the segment
            asmFile.write("D=A\n"); // Store the base address in the D register
            asmFile.write("@"+ index + "\n"); //index address
            asmFile.write("D=D+A\n"); // Calculate the target memory address (base address + index)
        } else{
            throw new IllegalArgumentException("Invalid temp index: " + index);
        }
    }


    ////////////////////////////////Label////////////////////////////////////
    protected static void translateLabel(FileWriter asmFile, String currentFunctionName, String label) throws IOException{
        String labelName = "";

        if(!currentFunctionName.isBlank()){
            labelName += currentFunctionName + "$";
        }

        labelName += label;
        asmFile.write("("+ labelName +")\n");
    }

    ////////////////////////////////Goto////////////////////////////////////
    protected static void translateGoto(FileWriter asmFile, String currentFunctionName, String label) throws IOException{
        String labelName = "";

        if(!currentFunctionName.isBlank()){
            labelName += currentFunctionName + "$";
        }

        labelName += label;
        asmFile.write("@"+ labelName +"\n");
    }

    ////////////////////////////////Call////////////////////////////////////

    protected static void translateCall(FileWriter asmFile, String functionName, int numArgs, int returnAddressIndex) throws IOException{
        asmFile.write("// call " + functionName + " " + numArgs + "\n");
        asmFile.write("@RETURN_ADDRESS" + returnAddressIndex + "\n");
        
        //pushing the return address onto the stack
        asmFile.write("D=A\n");
        asmFile.write("@SP\n");
        asmFile.write("A=M\n");
        asmFile.write("M=D\n");
        asmFile.write("@SP\n");
        asmFile.write("M=M+1\n");

        //pushing the LCL base address onto the stack
        pushCode(asmFile, "LCL");
        //pushing the ARG base address onto the stack
        pushCode(asmFile, "ARG");
        //pushing THIS base address onto the stack
        pushCode(asmFile, "THIS");
        //pushing THAT base address onto the stack
        pushCode(asmFile, "THAT");

        //setting up the ARG (argument) segment before making a function call
        asmFile.write("@SP\n");
        asmFile.write("D=M\n"); 
        asmFile.write("@"+numArgs+"\n"); 
        asmFile.write("D=D-A\n");
        asmFile.write("@5\n");
        asmFile.write("D=D-A\n");
        asmFile.write("@ARG\n");
        asmFile.write("M=D\n");

        //set up the LCL (local) segment before making a function call
        asmFile.write("@SP\n");
        asmFile.write("D=M\n");
        asmFile.write("@LCL\n");
        asmFile.write("M=D\n");

        //jump (JMP) to the memory location specified by the functionName
        asmFile.write("@"+functionName+"\n");
        asmFile.write("0;JMP\n");

        //Return address label
        asmFile.write("(RETURN_ADDRESS" + returnAddressIndex + ")\n");

    }

    protected static void pushCode(FileWriter asmFile, String segment)throws IOException{
        //code for pushing a segment without index onto the stack
        asmFile.write("@"+segment+"\n");
        asmFile.write("D=M\n");
        asmFile.write("@SP\n");
        asmFile.write("A=M\n");
        asmFile.write("M=D\n");
        asmFile.write("@SP\n");
        asmFile.write("M=M+1\n");
    }

    ////////////////////////////////Return////////////////////////////////////

    protected static void translateReturn(FileWriter asmFile)throws IOException{
        asmFile.write("// return\n");
        //store the base address of the local segment into R13 register.
        asmFile.write("@LCL\n");
        asmFile.write("D=M\n");
        asmFile.write("@R13\n");
        asmFile.write("M=D\n");
        
        //store the return address into R14 register.
        asmFile.write("@5\n");
        asmFile.write("D=A\n");
        asmFile.write("@R13\n");
        asmFile.write("A=M-D\n");
        asmFile.write("D=M\n");
        asmFile.write("@R14\n");
        asmFile.write("M=D\n");
        
        //store the return value into the base memory location of ARG.
        asmFile.write("@SP\n");
        asmFile.write("AM=M-1\n");
        asmFile.write("D=M\n");
        asmFile.write("@ARG\n");
        asmFile.write("A=M\n");
        asmFile.write("M=D\n");
        
        //computes the new value for the stack pointer (SP) by adding 1 to the value stored at ARG
        asmFile.write("@ARG\n");
        asmFile.write("D=M+1\n");
        asmFile.write("@SP\n");
        asmFile.write("M=D\n");

        //Pop THAT, THIS, ARG, LCL
        popCode(asmFile, "THAT");
        popCode(asmFile, "THIS");
        popCode(asmFile, "ARG");
        popCode(asmFile, "LCL");

        //Obtain the return address from R14 and JMP to it
        asmFile.write("@R14\n");
        asmFile.write("A=M\n");
        asmFile.write("0;JMP\n");

    }
    
    protected static void popCode(FileWriter asmFile, String segment)throws IOException{
        //code for popping a segment without index onto the stack
        asmFile.write("@R13\n");
        asmFile.write("AM=M-1\n");
        asmFile.write("D=M\n");
        asmFile.write("@"+segment+"\n");
        asmFile.write("M=D\n");
    }
}
