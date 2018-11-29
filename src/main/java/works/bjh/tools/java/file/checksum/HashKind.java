package works.bjh.tools.java.file.checksum;

public enum HashKind {
    MD5("MD5");
    private String algorithm;

    HashKind(String algorithm) {
        this.algorithm = algorithm;
    }
}
