package chapter6;

import java.util.HashMap;
import java.util.Map;

public final class SymbolTable {

    private SymbolTable(){}
    
    private static final Map<String, String> symtable;

    //The static initializer block is executed when the class is loaded, 
    //before any methods or variables are accessed. It allows you to perform initialization 
    //tasks that are required before the class can be used.
    static{
        symtable = new HashMap<>();
        initializeTable();
    }

    private static void initializeTable(){
        symtable.put("SP","0000000000000000");
        symtable.put("LCL","0000000000000001");
        symtable.put("ARG","0000000000000010");
        symtable.put("THIS","0000000000000011");
        symtable.put("THAT","0000000000000100");
        symtable.put("SCREEN","0100000000000000");
        symtable.put("KBD","0110000000000000");
    }

    //integer value will be given and it will be converted to Binary before inserting in the table
    public static void addEntry(String symbol, String value){
        symtable.put(symbol, value);
    }

    // Retrieves value associated to Symbol inputted
    public static String GetAddress(String symbol){
        return symtable.get(symbol);
    }

    // Verifies if table contains inserted Symbol
    public static Boolean contains(String symbol){
        return symtable.containsKey(symbol);
    }
}
