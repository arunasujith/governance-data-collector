package org.wso2.carbon.data;

/**
 * Created by aruna on 3/24/14.
 */
public class BuildCommitsData {

    private String id;
    private String commitAuthor;
    private String comment;
    private String changesetID;
    private String date;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommitAuthor() {
        return commitAuthor;
    }

    public void setCommitAuthor(String commitAuthor) {
        this.commitAuthor = commitAuthor;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getChangesetID() {
        return changesetID;
    }

    public void setChangesetID(String changesetID) {
        this.changesetID = changesetID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String toString() {
        return "ID :: " + id +
                "commitAuthor :: " + commitAuthor +
                "comment :: " + comment +
                "changesetID :: " + changesetID +
                "date :: " + date ;

    }
}
