package chapter6;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public final class Code {
    private static final Map<String, String> destTable = createDestTable();
    private static final Map<String, String> compTable = createCompTable();
    private static final Map<String, String> jumpTable = createJumpTable();

    private String dest; 
    private String comp;
    private String jump;

    public Code(String instruction){ // constructor calling parsing method
        parseInstruction(instruction);
    }

    private void parseInstruction(String instruction){
        if(instruction.contains("=")){
            String[] splitInstruction = instruction.split("=");
            
            this.dest = splitInstruction[0];
            instruction = splitInstruction[1]; 
        }

        if(instruction.contains(";")){
            String[] splitInstruction = instruction.split(";");
            
            this.comp = splitInstruction[0];
            this.jump = splitInstruction[1];
        }else{
            this.comp = instruction;
        }

    }

    public String getDest(){
        return dest;
    }

    public void setDest(String dest){
        this.dest = dest;
    }

    public String getComp(){
        return comp;
    }

    public void setComp(String comp){
        this.comp = comp;
    }

    public String getJump(){
        return jump;
    }

    public void setJump(String jump){
        this.jump = jump;
    }    

    private static Map<String, String> createDestTable() {
        Map<String, String> table = new HashMap<>();
        table.put(null, "000");
        table.put("M", "001");
        table.put("D", "010");
        table.put("MD", "011");
        table.put("A", "100");
        table.put("AM", "101");
        table.put("AD", "110");
        table.put("AMD", "111");
        return Collections.unmodifiableMap(table);
    }

    private static Map<String, String> createCompTable() {
        Map<String, String> table = new HashMap<>();
        table.put("0", "0101010");
        table.put("1", "0111111");
        table.put("-1", "0111010");
        table.put("D", "0001100");
        table.put("A", "0110000");
        table.put("M", "1110000");
        table.put("!D", "0001101");
        table.put("!A", "0110001");
        table.put("!M", "1110001");
        table.put("-D", "0001111");
        table.put("-A", "0110011");
        table.put("-M", "1110011");
        table.put("D+1", "0011111");
        table.put("A+1", "0110111");
        table.put("M+1", "1110111");
        table.put("D-1", "0001110");
        table.put("A-1", "0110010");
        table.put("M-1", "1110010");
        table.put("D+A", "0000010");
        table.put("D+M", "1000010");
        table.put("D-A", "0010011");
        table.put("D-M", "1010011");
        table.put("A-D", "0000111");
        table.put("M-D", "1000111");
        table.put("D&A", "0000000");
        table.put("D&M", "1000000");
        table.put("D|A", "0010101");
        table.put("D|M", "1010101");
        return Collections.unmodifiableMap(table);
    }

    private static Map<String, String> createJumpTable() {
        Map<String, String> table = new HashMap<>();
        table.put(null, "000");
        table.put("JGT", "001");
        table.put("JEQ", "010");
        table.put("JGE", "011");
        table.put("JLT", "100");
        table.put("JNE", "101");
        table.put("JLE", "110");
        table.put("JMP", "111");
        return Collections.unmodifiableMap(table);
    }

    public static String getDestCode(String dest){
        return destTable.get(dest);
    }

    public static String getCompCode(String comp){
        return compTable.get(comp);
    }

    public static String getJumpCode(String jump){
        return jumpTable.get(jump);
    }

    
}