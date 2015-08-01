import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ULXToSourceBansConverter {
    public static void main(String[] args) {
        // SELECT GROUP_CONCAT(CONCAT("admins.put(\"", authid, "\",", aid, ");\n")) FROM steam_sourcebans.sb_admins;
        Map<String, Integer> admins = new HashMap<>();
        admins.put("STEAM_ID_SERVER",0);
        admins.put("STEAM_0:1:7366484",1);
        
        try {
            PrintWriter writer = new PrintWriter("insert-ulx-bans.sql", "UTF-8");
            VDFNode document = VDFNode.parseVDF("D:\\Users\\Nicholas\\Desktop\\NOTES\\ulx bans1.txt");
            for (Map.Entry<String, VDFNode> keygroup : document.getChildren().entrySet()) {
                String steamID = keygroup.getKey();
                String reason = "";
                String admin = "";
                Long unban = 0L;
                Long time = 0L;
                String name = "";
                String modifiedAdmin = "";
                
                VDFNode attributesNode = keygroup.getValue();
                for (Map.Entry<String, VDFNode> attributes : attributesNode.getChildren().entrySet()) {
                    switch (attributes.getKey()) {
                        case "reason":
                            reason = attributes.getValue().getValue();
                            break;
                            
                        case "admin":
                            admin = attributes.getValue().getValue();
                            break;
                            
                        case "unban":
                            unban = Long.parseLong(attributes.getValue().getValue());
                            break;
                            
                        case "time":
                            time = Long.parseLong(attributes.getValue().getValue());
                            break;
                            
                        case "name":
                            name = attributes.getValue().getValue();
                            break;
                            
                        case "modified_admin":
                            modifiedAdmin = attributes.getValue().getValue();
                            break;
                    }
                }
                
                String adminSteamID = "";
                adminSteamID = admin.substring(admin.lastIndexOf("(") + 1, admin.lastIndexOf(")"));
                
                Integer adminID = admins.get(adminSteamID);
                if (adminID == null) {
                    throw new RuntimeException("Admin ID for SteamID " + adminSteamID + " does not exist.");
                }
                
                Long length = unban - time;
                
                Long removedBy = null;
                String removeType = null;
                Long removedOn = null;
                
                if (unban.equals(0L)) {
                    unban = time;
                    length = 0L;
                } else {
                    removedBy = 0L;
                    removeType = "E";
                    removedOn = unban;
                }
                
                writer.println("INSERT INTO `steam_sourcebans`.`sb_bans`(`ip`,`authid`,`name`,`created`,`ends`,`length`,`reason`,`aid`,`adminIp`,`sid`,`country`,`RemovedBy`,`RemoveType`,`RemovedOn`,`type`,`ureason`) VALUES (" + 
                               "\"\", " + 
                               "\"" + steamID + "\", " + 
                               "\"" + name + "\", " + 
                               "\"" + time + "\", " + 
                               "\"" + unban + "\", " + 
                               "\"" + length + "\", " + 
                               "\"" + reason + "\", " + 
                               adminID + ", " + 
                               "\"\", " + 
                               "41, " + 
                               "null, " + 
                               removedBy + ", " + 
                               (removeType != null ? "\"" + removeType + "\"" : removeType) + ", " + 
                               removedOn + ", " + 
                               "0, " + 
                               "null" + 
                               ");");
            }
            writer.close();
            System.out.println("Finished!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class VDFNode {
    private VDFNode parent = null;
    private Map<String, VDFNode> children = null;
    private int level = 0;
    
    public VDFNode() { }
    
    public VDFNode getParent() {
        return parent;
    }
    
    public VDFNode setParent(VDFNode parent) {
        this.parent = parent;
        return this;
    }
    
    public Map<String, VDFNode> getChildren() {
        return children;
    }
    
    public String getValue() {
        for (Map.Entry<String, VDFNode> child : children.entrySet()) {
            return child.getKey();
        }
        return "";
    }
    
    public VDFNode setValue(String string) {
        children = new LinkedHashMap<String, VDFNode>();
        children.put(string, null);
        return this;
    }
    
    public VDFNode addChild(String key, VDFNode value) {
        if (children == null) {
            children = new LinkedHashMap<String, VDFNode>();
        }
        children.put(key, value);
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
        String string = "";
        
        if (parent == null) {
            for (Map.Entry<String, VDFNode> entry : children.entrySet()) {
                string += "\"" + entry.getKey() + "\" " + entry.getValue().toString();
            }
        } else if (children != null) {
            String tabs = "";
            String braceTabs = "";
            for (int i = 0; i < level; i++) {
                tabs += "\t";
                if (i != level - 1) {
                    braceTabs += "\t";
                }
            }
            
            if (children.size() == 1) {
                for (Map.Entry<String, VDFNode> entry : children.entrySet()) {
                    if (entry.getValue() == null) {
                        return "\t\"" + entry.getKey() + "\"\n";
                    }
                }
                string += "\n";
            } else {
                string += "\n";
            }
            
            string += braceTabs + "{\n";
            for (Map.Entry<String, VDFNode> entry : children.entrySet()) {
                string += tabs + "\"" + entry.getKey() + "\" " + entry.getValue().toString();
            }
            string += braceTabs + "}\n";
        }
        
        return string;
    }
    
    public static VDFNode parseVDF(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println(path + " does not exist.");
            return null;
        }
        
        if (!(file.isFile() && file.canRead())) {
            System.out.println(file.getName() + " cannot be read from.");
            return null;
        }
        
        try {
            InputStreamReader fis = new InputStreamReader(new FileInputStream(path), "UTF-8");
            
            VDFNode current = new VDFNode();
            int level = 0;
            
            boolean quotedStringOpen = false;
            boolean unboundStringOpen = false;
            
            String key = null;
            String value = null;
            StringBuffer buffer = new StringBuffer();
            
            char lastPointer = '\0';
            char pointer = 0;
            while (pointer != -1) {
                int b = fis.read();
                if (b == -1) {
                    break;
                }
                
                pointer = (char) b;
                
                String pointerString = Character.toString(pointer);
                
                if (quotedStringOpen) {
                    if (("\"".equals(pointerString) && !"\\".equals(Character.toString(lastPointer))) || 
                        "\r".equals(pointerString) || 
                        "\n".equals(pointerString)
                       ) {
                        if (key == null) {
                            key = buffer.toString();
                            buffer = new StringBuffer();
                        } else if (value == null) {
                            value = buffer.toString();
                            buffer = new StringBuffer();
                        }
                        
                        if (key != null && value != null) {
                            VDFNode child = new VDFNode();
                            child.setParent(current);
                            child.setValue(value);
                            child.setLevel(level);
                            current.addChild(key, child);
                            
                            key = null;
                            value = null;
                        }
                        
                        quotedStringOpen = false;
                    } else {
                        buffer.append(pointer);
                    }
                } else if (unboundStringOpen) {
                    if (pointerString.matches("\\s")) {
                        if (key == null) {
                            key = buffer.toString();
                            buffer = new StringBuffer();
                        } else if (value == null) {
                            value = buffer.toString();
                            buffer = new StringBuffer();
                        }
                        
                        if (key != null && value != null) {
                            VDFNode child = new VDFNode();
                            child.setParent(current);
                            child.setValue(value);
                            child.setLevel(level);
                            current.addChild(key, child);
                            
                            key = null;
                            value = null;
                        }
                        
                        unboundStringOpen = false;
                    } else {
                        buffer.append(pointer);
                    }
                } else {
                    if (pointerString.equals("{")) {
                        if (key == null) {
                            System.out.println(current);
                            throw new RuntimeException("Keygroup began without key.");
                        }
                        
                        level++;
                        
                        VDFNode child = new VDFNode();
                        child.setParent(current);
                        child.setLevel(level);
                        current.addChild(key, child);
                        current = child;
                        
                        key = null;
                    } else if (pointerString.equals("}")) {
                        if (key != null || value != null) {
                            System.out.println(current);
                            throw new RuntimeException("Key without matching value in keygroup.");
                        }
                        
                        level--;
                        
                        current = current.getParent();
                    } else if (pointerString.matches("\"")) {
                        quotedStringOpen = true;
                    } else if (pointerString.matches("\\S")) {
                        buffer.append(pointer);
                        unboundStringOpen = true;
                    }
                }
                
                lastPointer = pointer;
            }
            
            return current;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
