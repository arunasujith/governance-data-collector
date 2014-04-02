package org.wso2.carbon.data;

/**
 * Created by aruna on 3/21/14.
 */
public class RepoCommitersData {
    private String gitRepoName;
    private String committerName;
    private String numberofCommits;

    public String getGitRepoName() {
        return gitRepoName;
    }

    public void setGitRepoName(String gitRepoName) {
        this.gitRepoName = gitRepoName;
    }

    public String getCommitterName() {
        return committerName;
    }

    public void setCommitterName(String committerName) {
        this.committerName = committerName;
    }

    public String getNumberofCommits() {
        return numberofCommits;
    }

    public void setNumberofCommits(String numberofCommits) {
        this.numberofCommits = numberofCommits;
    }

    public String toString() {
        return gitRepoName + " " +
                committerName + " " +
                numberofCommits;
    }
}
