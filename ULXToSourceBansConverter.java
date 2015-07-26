import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ULXToSourceBansConverter {
    public static void main(String[] args) {
        Path file = Paths.get("D:\\Users\\Nicholas\\Desktop\\NOTES\\ulx bans1.txt");
        try (InputStream in = Files.newInputStream(file); BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            int counter = 0;
            int level = 0;
            
            String line = null;
            VDFNode current = new VDFNode();
            current.setLevel(level);
            
            while ((line = reader.readLine()) != null) {
                counter++;
                line = line.trim();
                
                VDFNode tmp = new VDFNode();
                tmp.setParentNode(current);
                String key = null;
                String value = null;
                
                if (line.length() == 0) {
                    //System.out.println("EMPTY LINE");
                    continue;
                } else if (line.charAt(0) == "{".charAt(0)) {
                    level++;
                    //System.out.println("READ {");
                    
                    //System.out.println(current);
                    
                    VDFNode next = new VDFNode();
                    next.setParentNode(current);
                    next.setLevel(level);
                    current.addChild(next);
                    current = next;
                } else if (line.charAt(0) == "}".charAt(0)) {
                    level--;
                    //System.out.println("READ }");
                    current = current.getParentNode();
                } else if (line.charAt(0) == "\"".charAt(0)) {
                    //System.out.println("READ \"");
                    
                    int endQuotePos = 0;
                    Pattern pattern = Pattern.compile("(?<!\\\\)\"");
                    Matcher matcher = pattern.matcher(line.substring(1));
                    if (matcher.find()) {
                        endQuotePos = matcher.start() + 1;
                        key = line.substring(1, endQuotePos);
                        System.out.println("key: " + key);
                    } else {
                        throw new RuntimeException("Closing quote not found on line " + counter + ".");
                    }
                    
                    if (line.length() > endQuotePos + 1) {
                        line = line.substring(endQuotePos + 1);
                        System.out.println("endPos: " + line);
                        
                        pattern = Pattern.compile("(?<!\\\\)\"");
                        matcher = pattern.matcher(line.substring(1));
                        if (matcher.find()) {
                            endQuotePos = matcher.start() + 1;
                            value = line.substring(1, endQuotePos);
                        }
                        
                        tmp.setKey(key);
                        
                        if (value != null) {
                            VDFNode valueNode = new VDFNode();
                            valueNode.setParentNode(current);
                            valueNode.setLevel(level);
                            valueNode.setKey(value);
                            tmp.addChild(valueNode);
                        }
                    }
                } else {
                    //System.out.println("READ [A-Za-z0-9]");
                    
                    int endQuotePos = 0;
                    Pattern pattern = Pattern.compile("\\w");
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        endQuotePos = matcher.start();
                        key = line.substring(0, endQuotePos);
                        System.out.println("key2: " + key);
                    }
                    
                    if (line.length() > endQuotePos + 1) {
                        line = line.substring(endQuotePos + 1);
                        System.out.println("endPos2: " + line);
                        
                        pattern = Pattern.compile("\\w");
                        matcher = pattern.matcher(line.substring(1));
                        if (matcher.find()) {
                            endQuotePos = matcher.start() + 1;
                            value = line.substring(0, endQuotePos);
                        }
                        
                        tmp.setKey(key);
                        
                        if (value != null) {
                            VDFNode valueNode = new VDFNode();
                            valueNode.setParentNode(current);
                            valueNode.setLevel(level);
                            valueNode.setKey(value);
                            tmp.addChild(valueNode);
                        }
                    }
                }
                
                //System.out.println(key);
                
                current.addChild(tmp);
            }
            
            System.out.println(current);
        } catch (IOException x) {
            System.err.println(x);
        }
    }
}

/*class VDFDocument {
 private List<VDFNode> nodes = new ArrayList<>();
 
 public List<VDFNode> getNodes() {
 return this.nodes;
 }
 
 public VDFDocument addNode(VDFNode node) {
 nodes.add(node);
 return this;
 }
 
 public VDFDocument setNodes(List<VDFNode> nodes) {
 this.nodes = nodes;
 return this;
 }
 
 public String toString() {
 String string = "";
 for (VDFNode node : nodes) {
 string += node.toString() + "\n"'
 }
 return string.trim();
 }
 }*/

class VDFNode {
    private VDFNode parentNode = null;
    private String key = null;
    private List<VDFNode> value = null;
    private int level = 0;
    
    public VDFNode() { }
    
    public VDFNode getParentNode() {
        return parentNode;
    }
    
    public VDFNode setParentNode(VDFNode node) {
        this.parentNode = node;
        return this;
    }
    
    public String getKey() {
        return key;
    }
    
    public VDFNode setKey(String key) {
        this.key = key;
        return this;
    }
    
    public VDFNode setValue(String string) {
        value = new ArrayList<VDFNode>();
        VDFNode tmp = new VDFNode();
        tmp.setKey(string);
        value.add(tmp);
        
        return this;
    }
    
    public VDFNode addChild(VDFNode child) {
        if (value == null) {
            value = new ArrayList<VDFNode>();
        }
        value.add(child);
        return this;
    }
    
    public int getLevel() {
        return level;
    }
    
    public VDFNode setLevel(int level) {
        this.level = level;
        return this;
    }
    
    public String toString() {
        //if (key != null) 
        //System.out.println(key);
        //if (value != null)
        //System.out.println(value.toString());
        
        String string = "";
        
        String tabs = "";
        for (int i = 0; i < level; i++) {
            tabs += "\t";
        }
        
        String braceTabs = "";
        for (int i = 0; i < level - 1; i++) {
            braceTabs += "\t";
        }
        
        if (key != null) {
            string += tabs + "\"" + key + "\" ";
        }
        
        if (value != null) {
            if (value.size() > 0) {
                if (level > 0) {
                    string += braceTabs + "{";
                }
                for (VDFNode node : value) {
                    string += tabs + node.toString() + "\n";
                }
                if (level > 0) {
                    string += braceTabs + "}";
                }
                //} else if (value.size() == 1) {
                //    string += "\"" + value.get(0).getKey() + "\" \n";
            } else {
                string += "{}";
            }
        }
        
        return string;
    }
}
