package ch10;

import java.io.File;
import java.io.IOException;
import java.util.List;

//How to run 

// cd Path\To\Project\Directory\bin
// java packageName.className Path\to\jack\file

public class JackAnalyzer {
    public static void main(String[] args){
        if (args.length != 1) {
            System.out.println("Usage: java JackAnalyzer <path_to_vm_file>");
            return;
        }

        String filePath = args[0];
        processFiles(filePath);
    }

    private static void processFiles(String filePath){
        
        try {
            File file = new File(filePath);

            if (file.isDirectory()){
                File[] fileList = file.listFiles((dir, name) -> name.endsWith(".jack"));
                for(File f : fileList){
                    if(f != null){
                        processJackFile(f);
                    }
                }
            } else {
                processJackFile(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processJackFile(File file) throws IOException{
        
        JackTokenizer tokenizer = new JackTokenizer(file);
        List<String> tokens = tokenizer.getTokens();

        String xmlFilePath = file.getAbsolutePath().replace(".jack", "T.xml");
        File xmlFile = new File(xmlFilePath);
        
        CompilationEngine compiler = new CompilationEngine(tokens, xmlFile);
        compiler.compileClass();
        compiler.close();
    }
}
