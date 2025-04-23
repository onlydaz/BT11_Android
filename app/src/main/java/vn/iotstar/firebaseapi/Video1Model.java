package vn.iotstar.firebaseapi;

public class Video1Model {
    private String title;
    private String description;
    private String url;
    private String userId;
    private int like;

    public Video1Model() {
        // Required for Firebase
    }

    public Video1Model(String title, String description, String url, String userId, int like) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.userId = userId;
        this.like = like;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getLike() {
        return like;
    }

    public void setLike(int like) {
        this.like = like;
    }
}
