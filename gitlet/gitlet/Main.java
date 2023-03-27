package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/** Main class for gitlet.
 *  @author Nathan Lin
 */
public class Main {
    private static Stage mainstage = new Stage();
    private static String file = System.getProperty("user.dir");
    private static Path mainpath = Paths.get(file, ".gitlet");
    private static Path commitpath = Paths.get(file, ".gitlet", "commits");
    private static Path blobpath = Paths.get(file, ".gitlet", "blobs");
    private static Path stagepath = Paths.get(file, ".gitlet", ".stages");
    private static Path treepath = Paths.get(file, ".gitlet", ".CommitTree");
    private static File blobs = new File(blobpath.toString());
    private static File mainFile = new File(mainpath.toString());
    private static File file2 = new File(System.getProperty("user.dir"));
    private static File branch = Utils.join(file2, "branches");
    private static File branches = Utils.join(branch, "branches");
    private static File commit =  Utils.join(file2, "commits");;



    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        } else if (args[0].equals("init")) {
            init();
            return;
        } else if (!Files.exists(mainpath)) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        } else if (args[0].equals("add")) {
            add(args[1]);
            return;
        } else if (args[0].equals("commit")) {
            commit(args[1]);
            return;
        } else if (args[0].equals("log")) {
            System.out.println(log());
            return;
        }
        if (args[0].equals("global-log")) {
            System.out.println(globalLog());
            return;
        }
        if (args[0].equals("checkout")) {
            checkout(args);
            return;
        }
        if (args[0].equals("rm")) {
            rm(args[1]);
            return;
        }
        if (args[0].equals("rm-branch")) {
            rmBranch(args[1]);
            return;
        }
        if (args[0].equals("find")) {
            System.out.println(find(args[1]));
            return;
        }
        if (args[0].equals("status")) {
            status();
            return;
        }
        if (args[0].equals("branch")) {
            makeBranch(args[1]);
            return;
        }
        if (args[0].equals("reset")) {
            reset(args[1]);
            return;
        }
        if (args[0].equals("merge")) {
            merge(args[1]);
        } else {
            System.out.println("No command with that name exists.");
        }
        return;
    }
    /** initializes git. */
    public static void init() {
        if (Files.exists(mainpath)) {
            System.out.println(
                    "A Gitlet version-control system "
                            + "already exists in the current directory");
            return;
        } else {
            file2.mkdirs();
            mainstage.getStaging().mkdirs();
            mainstage.getRemoval().mkdirs();
            mainstage.getModded().mkdirs();
            mainstage.getUntracking().mkdirs();
            mainstage.getPermaed().mkdirs();
            commit.mkdirs();
            blobs.mkdirs();
            branch.mkdirs();
            Commit newCommit = new Commit("initial commit",
                    new Date(0), new HashMap<>(), null);
            String iD = Utils.sha1(Utils.serialize(newCommit));
            newCommit.setID(iD);
            CommitTree branch1 = new CommitTree(newCommit, "master");
            Utils.writeContents(branches, Utils.serialize(branch1));
            newCommit.setParentBranch(branch1.getBranch());
            File initialCommit = Utils.join(commit, "" + iD);
            Utils.writeContents(initialCommit, Utils.serialize(newCommit));
            try {
                initialCommit.createNewFile();
                branches.createNewFile();
            } catch (IOException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }
    
    public static void add(String fileName) {
        File main = Utils.join(file2, fileName);
        File stage = new File(stagepath.toString());
        File untracked = Utils.join(mainstage.getUntracking(), fileName);
        if (!main.exists()) {
            System.out.println("File does not exist");
        }
        CommitTree thisBranch = Utils.readObject(branches, CommitTree.class);
        Commit newCommit = new Commit(thisBranch.getHead());
        newCommit.setParentBranch(thisBranch.getBranch());
        mainstage.findModified();
        mainstage.findUntracked();
        List<String> removedFiles = Utils.plainFilenamesIn
                (mainstage.getRemoval());
        List<String> modifiedFiles = Utils.plainFilenamesIn
                (mainstage.getModded());
        List<String> untrackedFiles = Utils.plainFilenamesIn
                (mainstage.getUntracking());
        if (untrackedFiles.isEmpty()
                && modifiedFiles.isEmpty()
                && removedFiles.isEmpty()) {
            return;
        }
        if (removedFiles.contains(fileName)) {
            File deleted = Utils.join(mainstage.getRemoval(), fileName);
            deleted.delete();
            return;
        }
        for (File file1 : mainstage.getModded().listFiles()) {
            String name = file1.getName();
            if (name.equals(fileName)) {
                file1.delete();
            }
        }
        if (untrackedFiles.contains(fileName)) {
            untracked.delete();
        }
        File copy = Utils.join(mainstage.getStaging(), fileName);
        String copyID = Utils.sha1(Utils.serialize(copy));
        if (!copy.exists()) {
            try {
                copy.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Utils.writeContents(copy, Utils.readContents(main));
        if (newCommit.getBlob().values().contains(copyID)) {
            copy.delete();
        }
        untracked.delete();
        mainstage.addStage(mainstage, fileName, copyID);
    }

    
    public static void commit(String message) {
        CommitTree thisBranch = Utils.readObject(branches, CommitTree.class);
        if (mainstage.getStaging().listFiles().length == 0
                && mainstage.getRemoval().listFiles().length == 0) {
            System.out.println("No changes added to the commit");
        }
        if (message.equals("") || message.equals(null)) {
            System.out.println("Please enter a commit message");
        }
        String name = thisBranch.getBranch();
        Commit parent = thisBranch.getBranches().get(name);
        String parentID = parent.getID();
        Commit newCommit = new Commit(thisBranch.getHead());
        newCommit.setParent(parentID);
        newCommit.updateDate();
        newCommit.updateMessage(message);
        newCommit.setParentBranch(thisBranch.getBranch());
        for (File file1 : mainstage.getStaging().listFiles()) {
            File branchName = Utils.join(branch, thisBranch.getBranch());
            File branchFile = Utils.join(branchName, file1.getName());
            if (!branchFile.exists()) {
                try {
                    branchFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Utils.writeContents(branchFile, Utils.readContents(file1));
            newCommit.addBlob(file1.getName(), file1,
                    Utils.sha1(Utils.readContents(file1)));
            file1.delete();
        }
        for (File file1 : mainstage.getRemoval().listFiles()) {
            String fileName = file1.getName();
            File permaed = Utils.join(mainstage.getPermaed(), fileName);
            if (!permaed.exists()) {
                try {
                    permaed.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Utils.writeContents(permaed, Utils.readContents(file1));
            file1.delete();
        }
        String newID = Utils.sha1(Utils.serialize(newCommit));
        newCommit.setID(newID);
        thisBranch.setUp(newCommit, thisBranch);
        Utils.writeContents(branches, Utils.serialize(thisBranch));
        File commited = Utils.join(commit, newID);
        try {
            commited.createNewFile();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
        Utils.writeObject(commited, thisBranch.getHead());
    }
    
    public static String log() {
        CommitTree branch1 = Utils.readObject(branches, CommitTree.class);
        Commit newCommit = branch1.getBranches().get(branch1.getBranch());
        Iterator iter = newCommit.commitIterator(newCommit, branch1);
        String space = " ";
        Formatter print = new Formatter();
        while (newCommit != null) {
            print.format("===%n");
            print.format("commit%s%s%n", space, newCommit.getID());
            if (newCommit.isMerge()) {
                print.format("Merge:%s%s%n", space, newCommit.getMergeID());
            }
            print.format("%s %ta %<tb %<te %<tT %<tY %<tz%n", "Date:",
                    newCommit.getTimestamp());
            print.format("%s%n", newCommit.getMessage());
            print.format("%n");
            if (newCommit.getParent() != null) {
                File commitParent = Utils.join(commit, newCommit.getParent());
                newCommit = Utils.readObject(commitParent, Commit.class);
            } else {
                newCommit = null;
            }
        }
        return print.toString();
    }
    
    public static String globalLog() {
        CommitTree branch1 = Utils.readObject(branches, CommitTree.class);
        String space = " ";
        Formatter print = new Formatter();
        ArrayList<String> keyList = new ArrayList<>();
        for (String keys : branch1.getTreeBranch().keySet()) {
            keyList.add(keys);
        }
        for (int i = (keyList.size() - 1); i >= 0; i--) {
            Commit commit1 = branch1.getTreeBranch().get(keyList.get(i));
            print.format("===%n");
            print.format("commit%s%s%n", space, commit1.getID());
            if (commit1.isMerge()) {
                print.format("Merge:t%s%s%n", space, commit1.getID());
            }
            print.format("%s %ta %<tb %<te %<tT %<tY %<tz%n", "Date:",
                    commit1.getTimestamp());
            print.format("%s%n", commit1.getMessage());
            print.format("%n");
            keyList.remove(i);
        }
        return print.toString();
    }
    
    public static void checkout(String... args) {
        CommitTree thisBranch = Utils.readObject(branches, CommitTree.class);
        String currentBranch = thisBranch.getBranch();
        Commit thisCommit = thisBranch.getHead();
        HashMap<String, String> blobs1 = thisCommit.getBlob();
        if (args.length == 2) {
            if (!thisBranch.getBranches().containsKey(args[1])) {
                System.out.println("No such branch exists.");
                return;
            }
            if (currentBranch.equals(args[1])) {
                System.out.println("No need to checkout the current branch.");
                return;
            }
            thisBranch.setBranch(args[1]);
            thisBranch.checkoutHelper(thisCommit, thisBranch, currentBranch);
        }
        if ((args.length == 3)) {
            if (blobs1.containsKey(args[2])) {
                String iD = blobs1.get(args[2]);
                File blob = Utils.join(blobs, iD);
                File mainBlob = Utils.join(file2, args[2]);
                thisCommit.writeBlobs(mainBlob, blob);
            } else {
                throw new IllegalArgumentException(
                        "File does not exist in that commit.");
            }
            return;
        }
        if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
            }
            Commit newCommit = thisBranch.getBranchHead(args[1]);
            if (newCommit == null) {
                System.out.println("No commit with that id exists.");
                return;
            }
            blobs1 = newCommit.getBlob();
            if (blobs1.containsKey(args[3])) {
                String iD = blobs1.get(args[3]);
                File blob = Utils.join(blobs, iD);
                File mainBlob = Utils.join(file2, args[3]);
                newCommit.writeBlobs(mainBlob, blob);
            } else {
                System.out.println("File does not exist in that commit.");
            }
            return;
        }
    }

    
    public static void rm(String fileName) {
        File main = Utils.join(file2, fileName);
        CommitTree thisBranch = Utils.readObject(branches, CommitTree.class);
        Commit thisCommit = thisBranch.getCurrentBranchHead(
                thisBranch.getBranch());
        File branchFile = Utils.join(branch, thisBranch.getBranch());
        ArrayList<String> fileNames = new ArrayList<>(
                Utils.plainFilenamesIn(file2));
        for (File file1 : mainstage.getModded().listFiles()) {
            String name = file1.getName();
            if (name.equals(fileName)) {
                file1.delete();
            }
        }
        for (File file1 : mainstage.getStaging().listFiles()) {
            if (file1.getName().equals(fileName)) {
                File copy = Utils.join(file2, file1.getName());
                try {
                    copy.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Utils.writeContents(copy, Utils.readContents(main));
                file1.delete();
                return;
            }
        }
        if (thisCommit.getBlob() == null) {
            System.out.println("No reason to remove the file");
            return;
        }
        for (String key: thisCommit.getBlob().keySet()) {
            if (key.equals(fileName)) {
                File copy = Utils.join(mainstage.getRemoval(), fileName);
                File branchCopy = Utils.join(branchFile, fileName);
                File deletedBlob = Utils.join(
                        blobs, thisCommit.getBlob().get(key));
                if (!copy.exists()) {
                    try {
                        copy.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (!main.exists()) {
                    Utils.writeContents(copy, Utils.readContents(branchCopy));
                } else {
                    Utils.writeContents(copy, Utils.readContents(main));
                }
                Utils.restrictedDelete(main);
                branchCopy.delete();
                deletedBlob.delete();
                return;
            }
        }
        System.out.println("No reason to remove the file");
    }

    
    public static void rmBranch(String branchname) {
        CommitTree branch1 = Utils.readObject(branches, CommitTree.class);
        String currentBranch = branch1.getBranch();
        if (!branch1.getBranches().containsKey(branchname)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (currentBranch.equals(branchname)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branch1.getBranches().remove(branchname);
        Utils.writeContents(branches, Utils.serialize(branch1));
    }

    
    public static String find(String message) {
        CommitTree branch1 = Utils.readObject(branches, CommitTree.class);
        String toReturn = "";
        for (String key: branch1.getTreeBranch().keySet()) {
            Commit thisCommit = branch1.getTreeBranch().get(key);
            if (thisCommit.getMessage().equals(message)) {
                toReturn += key + "\n";
            }
        }
        if (toReturn.equals("")) {
            toReturn += "Found no commit with that message.";
        }
        return toReturn;
    }
    /** returns status. */
    public static void status() {
        CommitTree branch1 = Utils.readObject(branches, CommitTree.class);
        branch1.title("Branches");
        ArrayList<String> branchKeySet = new ArrayList<>(
                branch1.getBranches().keySet());
        for (int i = branchKeySet.size() - 1; i >= 0; i--) {
            if (branchKeySet.get(i).equals("master")) {
                System.out.print("*");
            }
            System.out.println(branchKeySet.get(i));
        }
        System.out.println();
        branch1.title("Staged Files");
        for (File file1: mainstage.getStaging().listFiles()) {
            String name = file1.getName();
            System.out.println(name);
        }
        System.out.println();
        branch1.title("Removed Files");
        for (File file1: mainstage.getRemoval().listFiles()) {
            String name = file1.getName();
            System.out.println(name);
        }
        System.out.println();
        branch1.title("Modifications Not Staged For Commit");
        System.out.println();
        branch1.title("Untracked Files");
        System.out.println();

    }

    
    public static void makeBranch(String branchName) {
        CommitTree thisBranch = Utils.readObject(branches, CommitTree.class);
        if (thisBranch.getBranches().containsKey(branchName)) {
            System.out.println("A branch with that name already exists");
        }
        thisBranch.getBranches().put(branchName, thisBranch.getHead());
        File otherBranch = Utils.join(branch, branchName);
        otherBranch.mkdirs();
        File file1 = Utils.join(branch, thisBranch.getBranch());
        for (File current : file1.listFiles()) {
            File newFile = Utils.join(otherBranch, current.getName());
            if (!newFile.isFile()) {
                try {
                    newFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Utils.writeContents(newFile, Utils.readContents(current));
        }
        Utils.writeContents(branches, Utils.serialize(thisBranch));
    }
    
    public static void reset(String commitID) {
        mainstage.findModified();
        mainstage.findUntracked();
        CommitTree thisBranch = Utils.readObject(branches, CommitTree.class);
        ArrayList<String> blobNum = new ArrayList<String>(
                Utils.plainFilenamesIn(blobs));
        ArrayList<String> fileNum = new ArrayList<>(
                Utils.plainFilenamesIn(file2));
        ArrayList<String> untracked = new ArrayList<>(
                Utils.plainFilenamesIn(mainstage.getUntracking()));
        ArrayList<String> modifiedFiles = new ArrayList<>(
                Utils.plainFilenamesIn(mainstage.getModded()));
        if (!untracked.isEmpty() || !modifiedFiles.isEmpty()) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
            return;
        }
        Commit headCommit = thisBranch.getHead();
        Commit findCommit = thisBranch.getTreeBranch().get(commitID);
        if (findCommit == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        HashMap<String, String> blobs1 = findCommit.getBlob();
        HashMap<String, String> headBlobs = headCommit.getBlob();
        for (String workingDir : headBlobs.keySet()) {
            if (!blobs1.containsKey(workingDir)) {
                File file1 = Utils.join(file2, workingDir);
                Utils.restrictedDelete(file1);
            }
        }
        for (File file1 : mainstage.getStaging().listFiles()) {
            file1.delete();
        }
        for (String blobName : blobs1.keySet()) {
            String fileName = blobs1.get(blobName);
            File commitBlob = Utils.join(blobs, fileName);
            File newBlob = Utils.join(file2, blobName);
            if (!newBlob.isFile()) {
                try {
                    newBlob.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (commitBlob.isFile()) {
                Utils.writeContents(newBlob, Utils.readContents(commitBlob));
            }
        }
        thisBranch.setHead(findCommit);
        thisBranch.getBranches().put(thisBranch.getBranch(), findCommit);
        Utils.writeContents(branches, Utils.serialize(thisBranch));
    }

    public static void merge(String branchName) {
        CommitTree thisBranch = Utils.readObject(branches, CommitTree.class);
        if (!thisBranch.getBranches().
                containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        Commit currentCommit = thisBranch.getBranches().
                get(thisBranch.getBranch());
        Commit givenCommit = thisBranch.getBranches().get(branchName);
        Commit splitpoint = getCommonAncestor(currentCommit, givenCommit);
        if (splitpoint == null) {
            System.out.println("Cannot merge a branch with itself");
            return;
        }
        if (mainstage.getStaging().listFiles().length != 0
                || mainstage.getRemoval().listFiles().length != 0) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        HashMap<String, String> givenBlobs =
                givenCommit.allBlobs(givenCommit, branchName);
        HashMap<String, String> currentBlobs = currentCommit.allBlobs
                (currentCommit, thisBranch.getBranch());
        HashMap<String, String> splitBlobs = splitpoint.getBlob();
        HashSet<String> totalBlobs = new HashSet<>();
        HashMap<String, String> mergeBlobs = new HashMap<>();

        thisBranch.mergeHelper(givenBlobs, currentBlobs,
                splitBlobs, totalBlobs, mergeBlobs, branchName);
        String message = String.format("Merged %s into %s.",
                branchName, thisBranch.getBranch());
        Commit mergeCommit = new Commit(message, new Date(), mergeBlobs,
                thisBranch.getBranches().get(branchName).getID(),
                thisBranch.getBranches().get(thisBranch.getBranch()).getID());
        thisBranch.mergeHelper2(mergeCommit, branchName, thisBranch);
        thisBranch.setUp(mergeCommit, thisBranch);
        Utils.writeContents(branches, Utils.serialize(thisBranch));
        String iD = Utils.sha1(Utils.serialize(mergeCommit));
        File commited = Utils.join(commit, iD);
        try {
            commited.createNewFile();
        } catch (IOException | ClassCastException excp) {
            throw new IllegalArgumentException(excp.getMessage());
        }
        Utils.writeObject(commited, thisBranch.getHead());
    }

    
    public static Commit getCommonAncestor(Commit current, Commit given) {
        CommitTree thisBranch = Utils.readObject(branches, CommitTree.class);
        ArrayList<String> headParents = new ArrayList<>();
        ArrayList<String> headParents1 = new ArrayList<>();
        ArrayList<String> headParents2 = new ArrayList<>();
        ArrayList<String> givenParents = new ArrayList<>();
        givenParents.add(given.getID());
        ArrayList<String> givenParents1 = new ArrayList<>();
        givenParents1.add(given.getID());
        ArrayList<String> givenParents2 = new ArrayList<>();
        givenParents2.add(given.getID());
        Commit copy = new Commit("merge copy", current);
        Commit givencopy = new Commit("given copy", given);
        common1(headParents, headParents1, headParents2,
                current, given, copy, givencopy);
        common2(givenParents, givenParents1, givenParents2,
                headParents, headParents1,
                headParents2, given, givencopy);
        String id;
        if (given.isMerge()) {
            if (givenParents2.size() > givenParents1.size()) {
                id = givenParents1.get(givenParents1.size() - 1);
            } else {
                id = givenParents1.get(givenParents2.size() - 1);
            }
        } else {
            id = givenParents.get(givenParents.size() - 1);
        }
        if (thisBranch.getTreeBranch().containsKey(id)) {
            return thisBranch.getTreeBranch().get(id);
        } else {
            return null;
        }
    }

    
    public static void common1(ArrayList<String> headParents,
                               ArrayList<String> headParents1,
                               ArrayList<String> headParents2,
                               Commit current, Commit given,
                               Commit copy, Commit givencopy) {
        headParents.add(current.getID());
        headParents1.add(current.getID());
        headParents2.add(current.getID());
        if (!current.isMerge()) {
            while (!current.isMerge()) {
                if (current.getParent() != null) {
                    File commitParent = Utils.join(commit, current.getParent());
                    current = Utils.readObject(commitParent, Commit.class);
                    headParents.add(current.getID());
                } else {
                    break;
                }
            }
        } else {
            while (current.isMerge()) {
                if (current.getParent1() != null) {
                    File commitParent = Utils.join
                            (commit, current.getParent1());
                    current = Utils.readObject(commitParent,
                            Commit.class);
                    headParents1.add(current.getID());
                } else {
                    break;
                }
            }
            while (copy.isMerge()) {
                if (copy.getParent2() != null) {
                    File commitParent = Utils.join(commit, copy.getParent1());
                    copy = Utils.readObject(commitParent, Commit.class);
                    headParents2.add(copy.getID());
                } else {
                    break;
                }
            }
        }
    }

    public static void common2(ArrayList<String> givenParents,
                               ArrayList<String> givenParents1,
                               ArrayList<String> givenParents2,
                               ArrayList<String> headParents,
                               ArrayList<String> headParents1,
                               ArrayList<String> headParents2,
                               Commit given, Commit givencopy) {
        if (!given.isMerge()) {
            while (!given.isMerge()) {
                if (given.getParent() != null) {
                    File commitParent = Utils.join(commit, given.getParent());
                    given = Utils.readObject(commitParent, Commit.class);
                    if (headParents.contains(given.getID())
                            || headParents2.contains(given.getID())) {
                        givenParents.add(given.getID());
                        break;
                    }
                    givenParents.add(given.getID());
                } else {
                    break;
                }
            }
        } else {
            while (given.isMerge()) {
                if (given.getParent1() != null) {
                    File commitParent = Utils.join
                            (commit, given.getParent1());
                    given = Utils.readObject
                            (commitParent, Commit.class);
                    if (headParents.contains(given.getID())
                            || headParents1.contains(given.getID())
                            || headParents2.contains(given.getID())) {
                        givenParents1.add(given.getID());
                        break;
                    }
                    givenParents1.add(given.getID());
                } else {
                    break;
                }
            }
            while (givencopy.isMerge()) {
                if (givencopy.getParent2() != null) {
                    File commitParent = Utils.join
                            (commit, givencopy.getParent1());
                    givencopy = Utils.readObject
                            (commitParent, Commit.class);
                    givenParents2.add(givencopy.getID());
                    if (headParents.contains(givencopy.getID())
                            || headParents1.contains(givencopy.getID())
                            || headParents2.contains(givencopy.getID())) {
                        givenParents2.add(givencopy.getID());
                        break;
                    }
                    givenParents2.add(given.getID());
                } else {
                    break;
                }
            }
        }
    }
}



