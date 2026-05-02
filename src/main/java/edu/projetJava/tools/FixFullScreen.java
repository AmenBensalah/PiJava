package edu.projetJava.tools;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FixFullScreen {
    public static void main(String[] args) {
        File dir = new File("src/main/java/edu/projetJava/controllers");
        if (!dir.exists()) return;
        
        Pattern pattern = Pattern.compile("^(\\s+)(stage|window)\\.setScene\\(.*");

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".java")) {
                try {
                    List<String> lines = Files.readAllLines(Paths.get(file.toURI()));
                    List<String> newLines = new ArrayList<>();
                    boolean modified = false;
                    
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);
                        newLines.add(line);
                        
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.matches()) {
                            String indent = matcher.group(1);
                            String stageVar = matcher.group(2);
                            
                            String nextLine = (i + 1 < lines.size()) ? lines.get(i + 1) : "";
                            if (!nextLine.contains("setFullScreen")) {
                                newLines.add(indent + stageVar + ".setFullScreen(true);");
                                modified = true;
                            }
                        }
                    }
                    
                    if (modified) {
                        Files.write(Paths.get(file.toURI()), newLines);
                        System.out.println("Fixed " + file.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
