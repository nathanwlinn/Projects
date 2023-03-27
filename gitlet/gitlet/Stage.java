package gitlet;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** stage file.
 * @author Nathan
 */
public class Stage implements Serializable {
    private HashMap<String, String> stageAdded;
    private static HashMap<String, String> stageRemoved;
    private static HashMap<String, String> modified1;
    private static HashMap<String, String> deleted1;
    private static HashMap<String, String> notAdded1;
    private static File main = new File(System.getProperty("user.dir"));
    private static File staging = Utils.join(main, "stagingArea");
    private static File removed = Utils.join(main, "removeFiles");
    private static File modified = Utils.join(main, "modifiedFiles");
    private static File branch = Utils.join(main, "branches");
    private static File branches = Utils.join(branch, "branches");
    private static File untrackedFile = Utils.join(main, "untrackedFiles");
    private static File permaBan = Utils.join(main, "rmCommitted");
    private static File blobs = Utils.join(main, ".gitlet.blobs");
    private static List<String> untracked;
    Stage() {
        this.stageAdded = new HashMap<>();
        this.stageRemoved = new HashMap<>();
        this.modified1 = new HashMap<>();
        deleted1 = new HashMap<>();
        notAdded1 = new HashMap<>();
        this.untracked = new ArrayList<>();
    }
    public static void findModified() {
        CommitTree branch1 = Utils.readObject(branches, CommitTree.class);
        Commit thisCommit = branch1.getBranches().get(branch1.getBranch());
        List<String> allFiles = Utils.plainFilenamesIn(main);
        List<String> fileNames = new ArrayList<>();
        for (File file : staging.listFiles()) {
            String name = file.getName();
            fileNames.add(name);
        }
        for (File file : removed.listFiles()) {
            String name = file.getName();
            fileNames.add(name);
        }
        for (Commit commit : branch1.getTreeBranch().values()) {
            for (File file : main.listFiles()) {
                if (file.isFile() && !fileNames.contains(file.getName())) {
                    String fileName = file.getName();
                    File mainFile = Utils.join(main, fileName);
                    File changed = Utils.join(modified, fileName);
                    String serial = Utils.sha1(Utils.readContents(file));
                    if (!changed.exists()) {
                        try {
                            changed.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        break;
                    }
                    if (commit.getBlob() == null) {
                        changed.delete();
                    } else if (commit.getBlob().containsKey(fileName)
                            && (!commit.getBlob().get(fileName).equals(serial)
                            && changed.exists())) {
                        Utils.writeContents(changed,
                                Utils.readContents(mainFile));
                    } else {
                        changed.delete();
                    }
                }
            }
        }
    }

    public static void findUntracked() {
        CommitTree branch1 = Utils.readObject(branches, CommitTree.class);
        Commit thisCommit = branch1.getHead();
        List<String> fileNames = new ArrayList<>();
        findModified();
        ArrayList<String> permaed = new ArrayList<>();
        ArrayList<String> modifiedFiles = new ArrayList<>(
                Utils.plainFilenamesIn(modified));
        for (File file : staging.listFiles()) {
            String name = file.getName();
            fileNames.add(name);
        }
        for (File file : removed.listFiles()) {
            String name = file.getName();
            fileNames.add(name);
        }
        for (Commit commit : branch1.getTreeBranch().values()) {
            if (commit.getBlob() != null) {
                for (String name : commit.getBlob().keySet()) {
                    fileNames.add(name);
                }
            }
        }
        for (File file : main.listFiles()) {
            if (file.isFile() && !fileNames.contains(file.getName())) {
                String fileName = file.getName();
                File newUntracked = Utils.join(untrackedFile, fileName);
                if (permaBan.listFiles() != null) {
                    for (String string : Utils.plainFilenamesIn(permaBan)) {
                        permaed.add(string);
                    }
                }
                if (!fileNames.contains(fileName)
                        || permaed.contains(fileName)) {
                    if (!newUntracked.exists()) {
                        try {
                            newUntracked.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Utils.writeContents(newUntracked, Utils.readContents(file));
                }
            }
        }
    }

    
    public Stage(Stage stage) {
        this.stageAdded = stage.getStageAdded();
    }
    
    public HashMap<String, String> getStageAdded() {
        return stageAdded;
    }
    
    public HashMap<String, String> getStageRemoved() {
        return stageRemoved;
    }

    
    public void addStage(Stage stage, String name, String iD) {
        stageAdded.put(name, iD);
    }
    /** helper. */
    public void clear() {
        for (File file: staging.listFiles()) {
            file.delete();
        }
    }
    /** helper. */
    public void saveStage() {
        try {
            for (String keys : stageAdded.keySet()) {
                File stage = Utils.join(staging, keys);
                stage.createNewFile();
                Utils.writeContents(stage, stageAdded.get(keys));
            }
        } catch (IOException | ClassCastException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    public File getStaging() {
        return staging;
    }
    
    public File getRemoval() {
        return removed;
    }
    
    public File getModded() {
        return modified;
    }
    
    public File getUntracking() {
        return untrackedFile;
    }
    
    public File getPermaed() {
        return permaBan;
    }
}
