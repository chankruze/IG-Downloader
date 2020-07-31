package in.geekofia.igdl.models;

public class InstaMedia {
    String id, imageUrl, videoUrl;
    boolean isVideo, hasAudio;

    public InstaMedia(String id, String imageUrl, boolean isVideo) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.isVideo = isVideo;
    }

    public InstaMedia(String id, String imageUrl, boolean isVideo, String videoUrl, boolean hasAudio) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.isVideo = isVideo;
        this.videoUrl = videoUrl;
        this.hasAudio = hasAudio;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public boolean isHasAudio() {
        return hasAudio;
    }

    public void setHasAudio(boolean hasAudio) {
        this.hasAudio = hasAudio;
    }

    @Override
    public String toString() {
        return "InstaPost{" +
                "id='" + id + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", isVideo=" + isVideo +
                ", hasAudio=" + hasAudio +
                '}';
    }
}
