package in.geekofia.igdl.models;

import java.io.Serializable;

public class InstaPost implements Serializable {
    String postUrl, postCode, postType, postSourceCode;
    Boolean isPrivate;

    public InstaPost(String postUrl, String postCode, String postType) {
        this.postUrl = postUrl;
        this.postCode = postCode;
        this.postType = postType;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getPostType() {
        return postType;
    }

    public Boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getPostSourceCode() {
        return postSourceCode;
    }

    public void setPostSourceCode(String postSourceCode) {
        this.postSourceCode = postSourceCode;
    }
}
