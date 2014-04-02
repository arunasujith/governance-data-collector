package org.wso2.carbon.data;

/**
 * Created by aruna on 3/21/14.
 */
public class GRegData {

    private String githubRepoName;
    private String githubOwnerName;
    private String bambooName;
    private String jenkinsName;

    public String getJenkinsName() {
        return jenkinsName;
    }

    public void setJenkinsName(String jenkinsName) {
        this.jenkinsName = jenkinsName;
    }

    public String getGithubRepoName() {
        return githubRepoName;
    }

    public void setGithubRepoName(String githubRepoName) {
        this.githubRepoName = githubRepoName;
    }

    public String getBambooName() {
        return bambooName;
    }

    public void setBambooName(String bambooName) {
        this.bambooName = bambooName;
    }

    public String getGithubOwnerName() {
        return githubOwnerName;
    }

    public void setGithubOwnerName(String githubOwnerName) {
        this.githubOwnerName = githubOwnerName;
    }

    public String toString() {
        return githubRepoName + " " +
                githubOwnerName + " " +
                bambooName;
    }
}

