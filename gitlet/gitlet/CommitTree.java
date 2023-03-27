package gitlet;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

/** commit tree class.
 * @author Nathan
 */
public class CommitTree implements Serializable {
    private LinkedHashMap<String, Commit> treeBranch = new LinkedHashMap<>();
    private HashMap<String, Commit> branches = new HashMap<>();
    private static Stage mainstage = new Stage();
    private String directory = System.getProperty("user.dir");
    private static String file1 = System.getProperty("user.dir");
    private static File file2 = new File(System.getProperty("user.dir"));
    private static File branchFile = Utils.join(file2, "branches");
    private static File branches1 = Utils.join(branchFile, "branches");
    private static Path blobpath = Paths.get(file1, ".gitlet", "blobs");
    private static File blobs1 = new File(blobpath.toString());
    private Commit head;
    private String branch = new String();
    private String headBranch;
    private String active;
    public CommitTree(Commit commit, String branch1) {
        this.head = commit;
        this.branch = branch1;
        this.headBranch = branch1;
        this.active = branch1;
        treeBranch.put(commit.getID(), commit);
        branches.put(branch1, head);
        File current = Utils.join(branchFile, branch1);
        current.mkdirs();

    }

    public CommitTree(String branchName) {
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists");
        }
        branches.put(branchName, head);

    }

    public void title(String dataType) {
        System.out.println(String.format("=== %s ===", dataType));
    }

    public void add(Commit commit, CommitTree main) {
        treeBranch.put(commit.getID(), commit);
    }


    public void branchCommit(Commit commit) {
        this.active = headBranch;
    }

    public void setHead(Commit commit) {
        this.head = commit;
        headBranch = Utils.sha1(Utils.serialize(commit));
    }

    public String getBranch() {
        return this.branch;
    }

    public String getHeadBranch() {
        return this.headBranch;
    }

    public Commit getHead() {
        return this.head;
    }

    public Commit getBranchHead(String name) {
        return treeBranch.get(name);
    }

    
    public Commit getCurrentBranchHead(String name) {
        return branches.get(name);
    }

    
    public void setUp(Commit commit, CommitTree branch1) {
        branch1.add(commit, branch1);
        branch1.setHead(commit);
        branch1.branchCommit(commit);
        branches.put(branch1.getBranch(), commit);
    }

    public LinkedHashMap<String, Commit> getTreeBranch() {
        return this.treeBranch;
    }

    
    public HashMap<String, Commit> getBranches() {
        return this.branches;
    }

    
    public void setBranch(String branch1) {
        this.branch = branch1;
    }

    
    public void checkoutHelper(Commit thisCommit,
                               CommitTree thisBranch,
                               String currentBranch) {
        HashMap<String, String> blobs2 = thisCommit.getBlob();
        Utils.writeContents(branches1, Utils.serialize(thisBranch));
        File newBranch = Utils.join(branchFile, thisBranch.getBranch());
        Commit newCommit = thisBranch.getHead();
        ArrayList<String> branchFiles = new ArrayList<>(
                Utils.plainFilenamesIn(newBranch));
        if (blobs2 != null) {
            for (String key : blobs2.keySet()) {
                File currBlob = Utils.join(blobs1, blobs2.get(key));
                File mainBlob = Utils.join(file2, key);
                if (currBlob.isFile()) {
                    newCommit.writeBlobs(mainBlob, currBlob);
                }
            }
        }
        for (File file3 : file2.listFiles()) {
            if (!branchFiles.contains(file3.getName()) && file3.isFile()) {
                file3.delete();
            }
        }
        ArrayList<String> directory1 = new ArrayList<>(
                Utils.plainFilenamesIn(file2));
        for (File file3 : newBranch.listFiles()) {
            File copy = Utils.join(file2, file3.getName());
            if (!directory1.contains(file3.getName())) {
                try {
                    copy.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Utils.writeContents(copy, Utils.readContents(file3));
        }
        thisBranch.setHead(thisCommit);
        return;
    }

    
    public void mergeHelper(HashMap<String, String> givenBlobs,
                            HashMap<String, String> currentBlobs,
                            HashMap<String, String> splitBlobs,
                            HashSet<String> totalBlobs,
                            HashMap<String, String> mergeBlobs,
                            String branchName) {
        String give = "";
        String curren = "";
        String spli = "";
        totalBlobs.addAll(givenBlobs.keySet());
        totalBlobs.addAll(currentBlobs.keySet());
        if (splitBlobs == null) {
            spli = "";
        } else {
            totalBlobs.addAll(splitBlobs.keySet());
        }
        for (String blobName : totalBlobs) {
            if (givenBlobs.containsKey(blobName)) {
                give = givenBlobs.get(blobName);
            } else {
                give = "";
            }
            if (currentBlobs.containsKey(blobName)) {
                curren = currentBlobs.get(blobName);
            } else {
                curren = "";
            }
            if (splitBlobs != null) {
                if (splitBlobs.containsKey(blobName)) {
                    spli = splitBlobs.get(blobName);
                }
            }
            if (give.equals(spli) && give.equals(curren)) {
                mergeBlobs.put(blobName, give);
            } else if (give.equals("") && curren.equals(spli)
                    && (!curren.equals(give))) {
                File removal = Utils.join(file2, blobName);
                File removestage = Utils.join(mainstage.getRemoval(), blobName);
                if (!removestage.exists()) {
                    try {
                        removestage.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Utils.writeContents(removestage, Utils.readContents(removal));
                Utils.restrictedDelete(removal);
            } else if (!give.equals(spli) && !curren.equals(spli)
                    && give.equals(curren)) {
                mergeBlobs.put(blobName, curren);
            } else if (give.equals(spli) && !curren.equals(spli)) {
                mergeBlobs.put(blobName, curren);
            } else if (!give.equals(spli) && curren.equals(spli)
                    && !give.equals("")) {
                mergeBlobs.put(blobName, give);
                mergeBig(branchName, blobName);
            } else if (!give.equals(spli) && !curren.equals(spli)
                    && !give.equals(curren)) {
                mergeBlobs.put(blobName, give);
                writeContents(give, curren, blobName);
            }
        }
    }

    
    public void mergeBig(String branchName, String blobName) {
        File thisBranchName = Utils.join(branchFile, branchName);
        File branchFile1 = Utils.join(thisBranchName, blobName);
        File file3 = Utils.join(file2, blobName);
        File stageFile = Utils.join(mainstage.getStaging(), blobName);
        if (!file3.exists()) {
            try {
                file3.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Utils.writeContents(file3, Utils.readContents(branchFile1));
        Utils.writeContents(stageFile, Utils.readContents(branchFile1));
    }

    
    public void writeContents(String givenBlob,
                              String currentBlob,
                              String blobName) {
        File givenFile = Utils.join(blobs1, givenBlob);
        File currentFile = Utils.join(blobs1, currentBlob);
        File file3 = Utils.join(file2, blobName);
        File staging = Utils.join(mainstage.getStaging(), blobName);
        String givenString = "";
        String currString = "";
        if (givenFile.exists() && !givenBlob.equals("")) {
            givenString = Utils.readContentsAsString(givenFile);
        }
        if (currentFile.exists() && !currentBlob.equals("")) {
            currString = Utils.readContentsAsString(currentFile);
        }
        Formatter out = new Formatter();
        out.format("<<<<<<< HEAD%n%s=======%n%s>>>>>>>%n",
                currString, givenString);
        Utils.writeContents(file3, out.toString());
        Utils.writeContents(staging, Utils.readContents(file3));
        System.out.println("Encountered a merge conflict.");
    }

    
    public void mergeHelper2(Commit mergeCommit,
                              String branchName,
                              CommitTree thisBranch) {
        mergeCommit.setParentBranch(branchName);
        String mergeID = mergeCommit.getParent1().substring(0, 7) + " "
                + mergeCommit.getParent2().substring(0, 7);
        String iD = Utils.sha1(Utils.serialize(mergeCommit));
        mergeCommit.setID(iD);
        thisBranch.getTreeBranch().put(iD, mergeCommit);
        for (File file3 : mainstage.getStaging().listFiles()) {
            File newBranch = Utils.join(branchFile, thisBranch.getBranch());
            File branchFile1 = Utils.join(newBranch, file3.getName());
            if (!branchFile1.exists()) {
                try {
                    branchFile1.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Utils.writeContents(branchFile1, Utils.readContents(file3));
            file3.delete();
        }
        for (File file3 : mainstage.getRemoval().listFiles()) {
            String fileName = file3.getName();
            File permaed = Utils.join(mainstage.getPermaed(), fileName);
            if (!permaed.exists()) {
                try {
                    permaed.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Utils.writeContents(permaed, Utils.readContents(file3));
            file3.delete();
        }
        mergeCommit.setMergeID(mergeID);
        mergeCommit.setParent(thisBranch.getBranches().get
                (thisBranch.getBranch()).getID());
    }
}
