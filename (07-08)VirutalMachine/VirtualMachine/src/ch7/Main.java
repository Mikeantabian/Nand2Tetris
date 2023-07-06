package ch7;

import java.io.File;
import java.io.IOException;

//Command-line instructions:
// cd Directory\containing\project\bin
// java packageName.MainClassName Directory\or\vmFile\path


public class Main{
    public static void main(String[] args){
        if (args.length != 1) {
            System.out.println("Usage: java VMTranslator <path_to_vm_file>");
            return;
        }

        String vmPath = args[0];

        try {
            CodeWriter codeWriter;

            File file = new File(vmPath);

            if (file.isDirectory()) {
                File[] vmFiles = file.listFiles((dir, name) -> name.endsWith(".vm"));

                if (vmFiles != null && vmFiles.length > 0) {
                    String asmDirectory = file.getAbsolutePath() + File.separator + file.getName() +".asm";
                    codeWriter = new CodeWriter(asmDirectory);
                    codeWriter.writeInit();
                    translate(vmFiles, codeWriter);
                    codeWriter.close();
                    
                } else {
                    System.out.println("No .vm files found in the specified directory.");
                }
            } else {
                String asmFile = vmPath.replace(".vm", ".asm");
                codeWriter = new CodeWriter(asmFile);
                translate(new File[] {file}, codeWriter);
                codeWriter.close();
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void translate(File[] vmFiles, CodeWriter codeWriter) {
        for (File vmFile : vmFiles) {
            try {
                Parser parser = new Parser(vmFile.getAbsolutePath());
                codeWriter.setFileName(vmFile.getName().replaceAll(".*/([^/]+)\\.vm", "$1"));

                while (parser.hasMoreCommands()) {
                    parser.advance();
                    String commandType = parser.getCommandType();

                    if (codeWriter.isArithmeticCommand(commandType)) {
                        codeWriter.writeArithmetic(parser.getCommand());
                    } else if (commandType.equals("push")) {
                        codeWriter.writePush(parser.getArg1(), parser.getArg2());
                    } else if (commandType.equals("pop")) {
                        codeWriter.writePop(parser.getArg1(), parser.getArg2());
                    } else if (commandType.equals("goto")){
                        codeWriter.writeGoto(parser.getArg1());;
                    } else if (commandType.equals("if-goto")){
                        codeWriter.writeIf(parser.getArg1());;
                    } else if (commandType.equals("label")){
                        codeWriter.writeLabel(parser.getArg1());
                    } else if (commandType.equals("function")){
                        codeWriter.writeFunction(parser.getArg1(), parser.getArg2());
                        codeWriter.setFunctionName(parser.getArg1());
                    } else if (commandType.equals("call")){
                        codeWriter.writeCall(parser.getArg1(),parser.getArg2());
                    } else if (commandType.equals("return")){
                        codeWriter.writeReturn();
                    }
                    
                }

                parser.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

