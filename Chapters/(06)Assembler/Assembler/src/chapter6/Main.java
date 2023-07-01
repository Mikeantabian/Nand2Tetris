package chapter6;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        if (args.length == 0) {
            logger.info("No file found");
            return;
        }

        Parser processor = new Parser();
        try {
            processor.processFile(args[0]);
            logger.info("File processed successfully.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, String.format("An error occurred while processing the file: %s", e.getMessage()), e);
        }
    }
}

