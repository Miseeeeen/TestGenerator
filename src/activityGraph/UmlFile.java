package activityGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UmlFile {
    List<String> lines = new ArrayList<String>();
    int pointer = 0;

    public UmlFile(String path) {
        File file = new File(path);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String temp = null;

            while ((temp = reader.readLine()) != null) {
                lines.add(temp);
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("lack the file: " + path);
        }
    }

    public String currentLine() {
        return lines.get(pointer);
    }

    public void advance() {
        pointer++;
    }

    public boolean isEOF() {
        return pointer >= lines.size();
    }

}
