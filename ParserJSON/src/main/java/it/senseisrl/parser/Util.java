package it.senseisrl.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;


public class Util {

    private static final String[] arrayPriorita = new String[]{"blocker", "critical", "major", "minor", "trivial"};

    private static final String ONHOLD = "on hold";

    private static final String OPEN = "open";

    private static Map<String, String> kindIssueMap = new HashMap<>();

    private static Map<String, String> statoIssueMap = new HashMap<>();

    private static FileOutputStream fop = null;
    
    private static final Logger LOGGER = Logger.getLogger(Util.class);

    private Util() {
    }

    static {
        kindIssueMap.put("CR", "enhancement");
        kindIssueMap.put("Bug", "bug");
        kindIssueMap.put("Support", "task");

        statoIssueMap.put("Done", "closed");
        statoIssueMap.put("Invalid", "invalid");
        statoIssueMap.put("On hold", ONHOLD);
        statoIssueMap.put("New", "new");
        statoIssueMap.put("Do", OPEN);
        statoIssueMap.put("Doing", OPEN);
        statoIssueMap.put("Reviewing", OPEN);
        statoIssueMap.put("Review", OPEN);
        statoIssueMap.put("Test", ONHOLD);
        statoIssueMap.put("Testing", ONHOLD);
        statoIssueMap.put("Tested", ONHOLD);
        statoIssueMap.put("Release", ONHOLD);

    }

    public static String getPriorita(int indice) {
        return arrayPriorita[indice - 1];
    }

    public static String getKindIssue(String chiave) {

        return kindIssueMap.get(chiave);
    }

    public static String getStato(String chiave) {
        return statoIssueMap.get(chiave);
    }

    public static void scriviJSON(String issues) {
    
        try {

            File file;

        file = new File("C:\\db-1.0.json");

        fop = new FileOutputStream(file);

        if (!file.exists()) {
            file.createNewFile();
        }

        byte[] contentInBytes = issues.getBytes();
        fop.write(contentInBytes);
        fop.flush();
        fop.close();
        LOGGER.debug("File finale db-1.0.json creato sotto la cartella C. Zippare prima dell'UPLOAD.");

    }
    catch (Exception ex

    
        ) {           
         LOGGER.error("File non gestito correttamente ", ex);
    }

    
        finally {
            if (fop != null) {
            try {
                fop.close();
            } catch (IOException ex) {
                LOGGER.error("File non gestito correttamente ", ex);
            }
        }
    }

}
}
