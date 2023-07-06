package ch7;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class Parser {
    private BufferedReader vmFile;
    private String currentCommand;

    protected Parser(String pathToVmFile) throws IOException{
        vmFile = new BufferedReader(new FileReader(pathToVmFile));
    }

    protected Boolean hasMoreCommands() throws IOException{
         return vmFile.ready();
    }

    protected void advance() throws IOException{
        currentCommand = vmFile.readLine().replaceAll("/\\/.+|//.*|\\n|\\r", "").trim();
    }

    protected String getCommand(){
        return currentCommand;
    }
    
    protected String getCommandType(){
        String[] commandParts = currentCommand.split("\\s");
        return commandParts[0];
    }

    protected String getArg1(){
        String[] commandParts = currentCommand.split("\\s");
        return commandParts[1];
    }

    protected int getArg2(){
        String[] commandParts = currentCommand.split("\\s");
        return Integer.parseInt(commandParts[2]);
    }

    protected void close() throws IOException{
        vmFile.close();
    }
}
