package borMi.dataStructure;

import java.util.HashMap;
import java.util.Set;

public class TestCase extends HashMap<String, String> {
    public String toString() {
        String result = "";

        for (String key : this.keySet()) {
            result += key + ": " + (this.get(key).equals("true") ? "T" : "F") + "\r\n";
        }

        return result.substring(0, result.length() - 1);
    }
    
    public boolean isTrue(String name) {
        return this.get(name).equals("true");
    }
    
    public Set<String> variableSet() {
        return this.keySet();
    }

}

