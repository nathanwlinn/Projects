package gitlet;
import java.io.File;
import java.io.IOException;
/** Blob class 
 *  @author Nathan
 */
public class Blob {
    private String names;

    private byte[] contents;

    private String id;

    static final File BLOB = new File(".gitlet/blobs");

    Blob(String name, byte[] content, String iD) {
        this.names = name;
        this.contents = content;
        this.id = iD;
    }
    public String getId() {
        return this.id;
    }
    public String getName() {
        return this.names;
    }
    public byte[] getContent() {
        return this.contents;
    }
    public void saveBlob() {
        try {
            File blob = Utils.join(BLOB, getId());
            if (!blob.exists()) {
                blob.createNewFile();
            }
            Utils.writeContents(blob, contents);
        } catch (IOException | ClassCastException x) {
            throw new IllegalArgumentException(x.getMessage());
        }
    }
}
