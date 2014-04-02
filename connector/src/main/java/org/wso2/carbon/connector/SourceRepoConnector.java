package org.wso2.carbon.connector;

import org.wso2.carbon.data.RepoCommitersData;
import org.wso2.carbon.data.RepoCommitsData;

import java.util.List;

/**
 * Created by aruna on 3/21/14.
 */
public interface  SourceRepoConnector {


    String getRepositoryID();

    String getOwnerID();

    long getTotalNumberOfCommits();

    String getTopContributor();

    int getTotalPullRequestCount();

    String getLastCommitDateTime();

    String getCommitLastYear();

    List<RepoCommitsData>   getLatestCommitsList();

    List<RepoCommitersData>   getLatestCommitersList();

}
