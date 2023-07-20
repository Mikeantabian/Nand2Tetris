package ch11;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private static HashMap<String, SymbolEntry> ClassScopeTable; // Static and Field
    private static HashMap<String, SymbolEntry> SubroutineScopeTable; // Argument and Var
    private Map<Kind, Integer> KindCounts; // To track counts of each Kind
    
    //Creates a new empty symbol table
    public SymbolTable(){
        ClassScopeTable = new HashMap<String, SymbolEntry>(); // <Symbol, index>
        SubroutineScopeTable = new HashMap<String, SymbolEntry>();
        KindCounts = new HashMap<Kind, Integer>();
        for ( Kind kind : Kind.values()) { 
            KindCounts.put(kind, 0);
        }
    }

    // Enum to represent the different kinds of identifiers
    protected enum Kind {
        STATIC,
        FIELD,
        ARG,
        VAR,
        NONE
    }

    private static class SymbolEntry {
        private String type;
        private Kind kind;
        private int index;
        
        public SymbolEntry(String type, Kind kind, int index) {
            this.type = type;
            this.kind = kind;
            this.index = index;
        }
        
        public String getType() {
            return type;
        }
        
        public Kind getKind() {
            return kind;
        }
        
        public int getIndex() {
            return index;
        }
    }

    //  Starts a new subroutine scope resets the subroutine’s symbol table
    protected void StartSubroutine(){
       SubroutineScopeTable.clear();
       resetCount(Kind.ARG);
       resetCount(Kind.VAR);
    }

    // Defines a new identifier of a given name, type, and kind and assigns it a running index. 
    // STATIC and FIELD identifiers have a class scope, while ARG and VAR identifiers have a subroutine scope
    protected void Define(String name, String type, Kind kind){
        int index;
        if (kind.equals(Kind.FIELD) || kind.equals(Kind.STATIC)) {
            index = VarCount(kind);
            SymbolEntry symbol = new SymbolEntry(type, kind, index);
            ClassScopeTable.put(name, symbol);
        } else if (kind.equals(Kind.ARG) || kind.equals(Kind.VAR)) {
            index = VarCount(kind);
            SymbolEntry symbol = new SymbolEntry(type, kind, index);
            SubroutineScopeTable.put(name, symbol);
        }
        incrementCount(kind);
    }

    // Returns the number of variables of the given kind already defined in the current scope.
    protected int VarCount(Kind kind) {
        // Retrieve the count from kindCounts map for the specified kind
        return KindCounts.getOrDefault(kind, 0);
    }

    private void resetCount(Kind kind) {
        KindCounts.put(kind, 0);
    }

    private void incrementCount(Kind kind) {
        KindCounts.put(kind, VarCount(kind) + 1);
    }

    // Returns the kind of the named identifier in the current scope. 
    // If the identifier is unknown in the current scope, returns NONE.
    protected Kind KindOf(String name) {
        SymbolEntry ClassSymbol = ClassScopeTable.get(name);
        if( ClassSymbol != null) {
            return ClassSymbol.getKind();
        }
        SymbolEntry SubSymbol = SubroutineScopeTable.get(name);
        if ( SubSymbol != null) {
            return SubSymbol.getKind();
        }

        return Kind.NONE;
    }

    // Returns the type of the named identifier in the current scope.
    protected String TypeOf(String name) {
        SymbolEntry ClassSymbol = ClassScopeTable.get(name);
        if( ClassSymbol != null) {
            return ClassSymbol.getType();
        }
        SymbolEntry SubSymbol = SubroutineScopeTable.get(name);
        if ( SubSymbol != null) {
            return SubSymbol.getType();
        }

        return null;
    }

    //  Returns the index assigned to the named identifier.
    protected int IndexOf(String name) {
        SymbolEntry ClassSymbol = ClassScopeTable.get(name);
        if( ClassSymbol != null) {
            return ClassSymbol.getIndex();
        }
        SymbolEntry SubSymbol = SubroutineScopeTable.get(name);
        if ( SubSymbol != null) {
            return SubSymbol.getIndex();
        }

        return -1;
    }

}
