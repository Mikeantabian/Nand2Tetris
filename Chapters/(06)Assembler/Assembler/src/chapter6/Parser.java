package chapter6;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;



public class Parser {
    private static final String COMMENT_PREFIX = "//";
    private int MemStartPos = 16;

    public void processFile(String fileName) throws IOException {
        List<String> lines = readLinesFromFile(fileName); // read all the lines
        List<String> processedLines = processLines(lines); // process the lines
        String outputFileName = generateOutputFileName(fileName); // generate new file name
        writeLinesToFile(outputFileName, processedLines); // write processed lines on new file
    }

    //reads lines from a file
    private List<String> readLinesFromFile(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        return Files.readAllLines(path);
    }

    private List<String> processLines(List<String> lines) {
        List<String> processedLines = new ArrayList<>();
        int lineNumber = 0;

        //first pass through for labels and preprocess
        ListIterator<String> iterator = lines.listIterator();
        while (iterator.hasNext()) {
            String line = iterator.next();
            line = preprocessLine(line);
            if (isInvalidLine(line)) {
                iterator.remove();
                continue;
            }
            if (isLabelDeclaration(line)) {
                processLabelDeclaration(line, lineNumber);
                iterator.remove();
                continue;
            }
            processedLines.add(line);
            lineNumber++;
        }

        List<String> finalLines = new ArrayList<>();
        //second pass for instructions
        for(String line2: processedLines){
            if(isInvalidLine(line2)){
                continue;
            }
            if(!isLabelDeclaration(line2)){
                String processedLine = processInstruction(line2);
                if(processedLine != null){
                    finalLines.add(processedLine);
                }
            }
        }
        return finalLines;
    }

    //gets rid of whitespaces,newlines,tabs, and comments
    private String preprocessLine(String line) {
        line = line.replace("/n", "")
                   .replace("/t", "")
                   .replace(" ", "")
                   .replace("/r", "")
                   .trim();
        //this removes comments that don't start from the beginning
        int commentIndex = line.indexOf(COMMENT_PREFIX);
        if(commentIndex != -1){
            line = line.substring(0, commentIndex);
        }
        return line;
    }

    //returns true if line is empty or starts with a comment
    private boolean isInvalidLine(String line) {
        return line.isEmpty()||line.startsWith(COMMENT_PREFIX);
    }

    private boolean isLabelDeclaration(String line) {
        return line.startsWith("(") && line.endsWith(")");
    }

    private void processLabelDeclaration(String line, int lineNumber) {
        String label = line.substring(1, line.length()-1);
        SymbolTable.addEntry(label, convertIntToBinary(lineNumber));
    }

    private String processInstruction(String line) {
        if(line.startsWith("@")){
            return processAInstruction(line);
        }else{
            return processCInstruction(line);
        }
    }

    private String processAInstruction(String line) {
        String symbol = line.substring(1);
        if(SymbolTable.contains(symbol)){
            return SymbolTable.GetAddress(symbol);
        }else if(containsOnlyDigits(symbol)){ //if the A regsiter is just numbers e.g. @12
            return convertStringToBinary(symbol); // just return the binary representation one-shot
        }else if(isRLabel(symbol)){
            return convertStringToBinary(symbol.substring(1)); //if @R0-R15 return digit after R
        }else{
            SymbolTable.addEntry(symbol, convertIntToBinary(MemStartPos++));
            return SymbolTable.GetAddress(symbol);
        }
    }

    private String processCInstruction(String line) {
        Code code = new Code(line);
        String comp = Code.getCompCode(code.getComp());
        String dest = Code.getDestCode(code.getDest());
        String jump = Code.getJumpCode(code.getJump());

        return "111" + comp + dest + jump;
    }

    private void writeLinesToFile(String fileName, List<String> lines) throws IOException {
        Path path = Paths.get(fileName);
        Files.write(path, lines);
    }

    private String convertStringToBinary(String address) {
        
        int decimalNumber = Integer.parseInt(address);
        String binaryString = Integer.toBinaryString(decimalNumber);
        int leadingZeros = 16 - binaryString.length();

        StringBuilder binaryBuilder = new StringBuilder();
        for (int i = 0; i < leadingZeros; i++) {
            binaryBuilder.append('0');
        }
        binaryBuilder.append(binaryString);

        return binaryBuilder.toString();
    }

    private String convertIntToBinary(int address) {
        
        String binaryString = Integer.toBinaryString(address);
        int leadingZeros = 16 - binaryString.length();

        StringBuilder binaryBuilder = new StringBuilder();
        for (int i = 0; i < leadingZeros; i++) {
            binaryBuilder.append('0');
        }
        binaryBuilder.append(binaryString);

        return binaryBuilder.toString();

    }

    private String generateOutputFileName(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        //lastIndexOf returns -1 if "." isn't found
        if(dotIndex != -1){
            return fileName.substring(0,dotIndex) + ".hack";
        }else{
            return fileName + ".hack";
        }
    }

    private boolean containsOnlyDigits(String symbol) {
        if (symbol == null || symbol.isEmpty()) {
            return false; // Return false for null or empty strings
        }
    
        for (int i = 0; i < symbol.length(); i++) {
            if (!Character.isDigit(symbol.charAt(i))) {
                return false; // Return false if any non-digit character is found
            }
        }
    
        return true; // Return true if all characters are digits
    }

    private boolean isRLabel(String symbol){
        if (symbol == null || symbol.length() < 2 || symbol.charAt(0) != 'R') {
            return false;
        }

        String afterR = symbol.substring(1);

        if (!containsOnlyDigits(afterR)) {
            return false;
        }

        int referenceValue = Integer.parseInt(afterR);
        // we can only go up to R16 becaus after 16, we use those addresses for new symbols
        return referenceValue < 16;

    }
    
}


