package works.bjh.tools.java.file.checksum;

import lombok.Getter;

@SuppressWarnings("WeakerAccess")
public final class Result {
    @SuppressWarnings("FieldCanBeLocal")
    @Getter
    private String filePath;
    @SuppressWarnings("FieldCanBeLocal")
    @Getter
    private String hash;

    public Result(String filePath, String hash) {
        this.filePath = filePath;
        this.hash = hash;
    }
}
