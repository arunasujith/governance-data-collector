package org.wso2.carbon.manager;

import org.apache.log4j.Logger;
import org.wso2.carbon.data.RepoCommitersData;
import org.wso2.carbon.data.RepoCommitsData;
import org.wso2.carbon.implementation.GithubConnector;
import org.wso2.carbon.publisher.BAMDataPublisher;
import org.wso2.carbon.utils.Configurations;

import java.util.List;

/**
 * GitHubManager.java
 */
public class GithubManager {


    BAMDataPublisher pub = new BAMDataPublisher();
    private static Logger log = Logger.getLogger(GithubConnector.class);
    public static final String GIT_HUB_STREAM = "GitHub_Stream"; // Store git hub statistics data
    public static final String GIT_COMMITS_STREAM = "GitHub_Commits_Stream"; // Store git hub commits data
    public static final String GIT_COMMITTERS_STREAM = "GitHub_Committers_Stream";
    public static final String VERSION = "1.0.0";


    /**
     * Extract data from github api's and save data in BAM
     */
    public void execute(String repoName, String repoOwnerName) {
        GithubConnector con = new GithubConnector(repoName , repoOwnerName , Configurations.getWSO2_GIT_URL() , Configurations.getGTIHUB_TOKEN());
        //con.get
        //GitHubData bean = con.getGithubData(repoName, repoOwnerName);
        List<RepoCommitsData> list = con.getLatestCommitsList();
        List<RepoCommitersData> committersList = con.getLatestCommitersList();

        try {

            pub.repositoryDataPublish(getGitArray(con),  GIT_HUB_STREAM,  VERSION);

            for (RepoCommitsData data : list) {
                pub.repositoryCommitsDataPublish(getGitCommitsArray(data),  GIT_COMMITS_STREAM,  VERSION);
            }

            for (RepoCommitersData data : committersList) {
                pub.repositoryCommittersDataPublich(getCommitterGitArray(data),  GIT_COMMITTERS_STREAM,  VERSION);
            }

        } catch (Exception e) {
            log.error("Exception", e);
        } finally {

        }
    }

    /**
     * stop the publisher
     */
    public void finalize() {
       pub.stopPublisher();
    }

    /**
     * Convert GitHubData to String array
     * @param con Connection contains the github data
     * @return github data string array
     */
    private String[] getGitArray(GithubConnector con) {
        String[] gitDataArray = {
                con.getRepositoryID(), con.getOwnerID(), String.valueOf(con.getTotalNumberOfCommits())
                , con.getTopContributor(), String.valueOf(con.getTotalPullRequestCount()), con.getLastCommitDateTime()
                , "", con.getCommitLastYear()};

        return gitDataArray;

    }

    /**
     * Convert GitHubCommitterData to String array
     * @param bean GithubCommittersData
     * @return git committers string array
     */
    private String[] getCommitterGitArray(RepoCommitersData bean) {
        String[] gitDataArray = {bean.getGitRepoName(), bean.getCommitterName(), bean.getNumberofCommits()};

        return gitDataArray;

    }

    /**
     * Convert GitHubCommitsData to String array
     * @param data
     * @return
     */
    private String[] getGitCommitsArray(RepoCommitsData data) {
        String[] gitCommitsDataArray = {
                data.getId(), data.getShaValue(), data.getAuthorName(), data.getAuthorEMail(),
                data.getAuthorDate(), data.getCommitMessage()
        };
        return gitCommitsDataArray;
    }
}