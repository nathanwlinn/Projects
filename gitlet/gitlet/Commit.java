package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/** Commit file.
 * @author Nathan
 */
public class Commit implements Serializable {
    private String message;
    private Date date;
    private String parent;
    private String parent1;
    private String parent2;
    private HashMap<String, String> blob;
    private ArrayList<String> blobs;
    private String id;
    private String mergeID;
    private String name1;
    private String parentBranch;
    private HashMap<String, String> blobMap;
    private static File file2 = new File(System.getProperty("user.dir"));
    private static File commitfile = Utils.join(file2, ".gitlet/commits");
    private static File branchFile = Utils.join(file2, "branches");
    private static File branch = Utils.join(branchFile, "branches");

    
    Commit() {
        this.parent = "";
        this.blob = null;
        this.blobs = null;
        this.mergeID = "";
        this.parentBranch = "";
        this.id = "";
        this.date = null;
        this.message = "";
    }

    public Commit(String message1, Date date1,
                  HashMap<String, String> blob1, String parent1w) {
        this.message = message1;
        this.date = date1;
        this.parent = parent1w;
        blob = blob1;
        blobs = new ArrayList<>();
        this.parentBranch = "";
    }


    public Commit(Commit commit) {
        this.message = commit.getMessage();
        this.parent = commit.getParent();
        this.date = commit.getTimestamp();
        blob = new HashMap<>();
        blobs = new ArrayList<>();
        this.parentBranch = commit.getParentBranch();
    }
    
    public Commit(String message1, Date date1, HashMap<String,
            String> blob1, String parent11, String parent21) {
        this.message = message1;
        this.date = date1;
        this.parent = "";
        this.parent1 = parent11;
        this.parent2 = parent21;
        this.blob = blob1;
        this.mergeID = "";
        blobs = new ArrayList<>();
        this.parentBranch = "";
    }
    
    public Commit(String message1, Commit commit) {
        this.message = commit.getMessage();
        this.date = commit.getTimestamp();
        this.parent = commit.getParent();
        this.parent1 = commit.getParent1();
        this.parent2 = commit.getParent2();
        this.blob = commit.getBlob();
        blobs = commit.getBlobs();
        this.mergeID = commit.getMergeID();
        this.parentBranch = commit.getParentBranch();
    }
    
    public void setParentBranch(String branch1) {
        this.parentBranch = branch1;
    }
    
    public String getMergeID() {
        return this.mergeID;
    }
    
    public void setMergeID(String iD) {
        this.mergeID = iD;
    }
    
    public String getParentBranch() {
        return this.parentBranch;
    }

    
    public boolean isMerge() {
        if (getMessage().contains("Merged")) {
            return true;
        }
        return false;
    }

    
    public HashMap<String, String> getBlob() {
        return this.blob;
    }

    
    public void addBlob(String name2, File theblob, String iD) {
        blob.put(name2, iD);
        blobs.add(iD);
        Blob newBlob = new Blob(name1, Utils.readContents(theblob), iD);
        newBlob.saveBlob();
    }

    
    public void setID(String iD1) {
        this.id = iD1;
    }

    
    public String getName() {
        return this.name1;
    }

    
    public Iterator<Commit> commitIterator(Commit commit,
                                           CommitTree branch1) {
        return new CommitIterator(commit, branch1);
    }


    public HashMap<String, String> allBlobs(Commit commit, String givenName) {
        CommitTree thisBranch = Utils.readObject(branch, CommitTree.class);
        File branchFiles = Utils.join(branchFile, givenName);
        ArrayList<String> fileNames =
                new ArrayList<>(Utils.plainFilenamesIn(branchFiles));
        blobMap = new HashMap<>();
        if (commit.isMerge()) {
            if (commit.getBlob() != null) {
                if (!blobMap.containsKey(givenName)) {
                    commit.blobMap.put(givenName,
                            commit.getBlob().get(givenName));
                }
            }
            Commit parent1Copy = new Commit("copy1",
                    thisBranch.getTreeBranch().get(commit.getParent1()));
            Commit parent2Copy = new Commit("copy2",
                    thisBranch.getTreeBranch().get(commit.getParent2()));
            HashMap<String, String> parent1Blobs =
                    allBlobs(parent1Copy, parent1Copy.getParentBranch());
            HashMap<String, String> parent2Blobs =
                    allBlobs(parent2Copy, parent1Copy.getParentBranch());
        }
        while (commit != null) {
            if (commit.getBlob() != null) {
                for (String name : commit.getBlob().keySet()) {
                    if (!blobMap.containsKey(name)
                            && fileNames.contains(name)) {
                        blobMap.putIfAbsent(name, commit.getBlob().get(name));
                    }
                }
            }
            if (commit.getParent() != null) {
                commit = thisBranch.getTreeBranch().get(commit.getParent());
            } else {
                commit = null;
            }
        }
        return blobMap;
    }
    
    public void setParent(String name) {
        this.parent = name;
    }
    
    public String getID() {
        return this.id;
    }

    
    public ArrayList<String> getBlobs() {
        return blobs;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public Date getTimestamp() {
        return this.date;
    }
    
    public String getParent() {
        return this.parent;
    }
    
    public String getParent1() {
        return this.parent1;
    }
    
    public String getParent2() {
        return this.parent2;
    }

    
    public void updateDate() {
        this.date = new Date();
    }
    
    public void updateMessage(String newMessage) {
        this.message = newMessage;
    }

    
    public void writeBlobs(File main, File read) {
        if (!main.isFile()) {
            try {
                main.createNewFile();
            } catch (IOException | ClassCastException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
        Utils.writeContents(main, Utils.readContents(read));
    }

    
    private class CommitIterator implements Iterator<Commit> {
        private Commit commit;
        private CommitTree branch;
        private Commit temp;

        CommitIterator(Commit commit1, CommitTree branch1) {
            this.commit = commit1;
            this.branch = branch1;

        }
        @Override
        public boolean hasNext() {
            if (commit.getParent().equals("")) {
                return false;
            }
            return true;
        }
        @Override
        public Commit next() {
            temp = Utils.readObject(
                    Utils.join(commitfile, commit.getParent()), Commit.class);
            return temp;
        }

    }
}

