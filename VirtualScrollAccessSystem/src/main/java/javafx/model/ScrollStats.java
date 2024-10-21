package javafx.model;

public class ScrollStats {
    private String name;
    private String uploaderName;
    private int uploadCount;
    private int downloadCount;
    private boolean orphaned;
    private int uploaderId;
    public ScrollStats(String name, String uploaderName, int uploaderId, int uploads, int downloads, boolean orphaned){
        this.name = name;
        this.uploaderName = uploaderName;
        this.uploadCount = uploads;
        this.downloadCount = downloads;
        this.orphaned = orphaned;
        this.uploaderId = uploaderId;
    }

    public String getName() {
        return name;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public int getUploadCount() {
        return uploadCount;
    }

    public int getDownloadCount() {
        return downloadCount;
    }
    // True if this scrollStat has its scroll deleted, false if it is linked to an active scroll
    public boolean isOrphaned() {
        return orphaned;
    }

    public int getUploaderId() {
        return uploaderId;
    }

    public void setOrphaned(Boolean newValue) {
        this.orphaned = newValue;
    }
}
