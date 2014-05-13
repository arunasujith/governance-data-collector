package org.wso2.carbon.implementation;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.connector.RestConnector;
import org.wso2.carbon.connector.SourceRepoConnector;
import org.wso2.carbon.data.HttpHeaderData;
import org.wso2.carbon.data.RepoCommitersData;
import org.wso2.carbon.data.RepoCommitsData;
import org.wso2.carbon.utils.Constants;

import java.io.IOException;
import java.util.*;

/**
 * Created by aruna on 3/21/14.
 */
public class GithubConnector implements SourceRepoConnector {

    private static Logger log = Logger.getLogger(GithubConnector.class);


    private String gitRepoName;
    private String gitOwner;
    private String gitRestUrl;
    private String token;
    private long totalNUmberOfCommits;
    private String topContributor;
    private int totalPullRequestCount;
    private String lastCommitDateTime;
    private String commitLastYear;
    private List<RepoCommitsData> commitsList = new ArrayList<RepoCommitsData>();
    private List<RepoCommitersData> commitersList = new ArrayList<RepoCommitersData>();


    /**
     * Github connector Constructor
     *
     * @param gitRepoName repository Name
     * @param gitOwner    repo owner name
     * @param gitRestUrl  git rest url
     */

    public GithubConnector(String gitRepoName, String gitOwner, String gitRestUrl, String token) {
        this.gitRepoName = gitRepoName;
        this.gitOwner = gitOwner;
        this.gitRestUrl = gitRestUrl;
        this.token = token;

        fetchData();
    }

    /**
     * Call github rest api and populate data
     */
    private void fetchData() {

        Map<String, String> map = getGitBranches(getBranchesJsonString());
        String shavalue = map.get(Constants.MASTER);
        parseCommitsJsonString(getCommitsJsonString(shavalue));
        parsePullRequestsJsonString(getPullRequestsJsonString());
        parseCommitsStatsJsonString(getCommitStatsJsonString());
        parseWeeklyCommits(getWeeklyCommitsJsonString());


    }

    /**
     * Get the rest api Json String
     *
     * @param restUrl rest url
     * @return Json String
     * @throws HttpException
     * @throws IOException
     */
    private String getRestJsonString(String restUrl) throws HttpException, IOException {
        HttpHeaderData acceptData = new HttpHeaderData(Constants.HEADER_ACCEPT, Constants.HEADER_ACCEPT_JSON);
        HttpHeaderData authData = new HttpHeaderData(Constants.HEADER_AUTHORIZATION, this.token);
        ArrayList<HttpHeaderData> headerDataList = new ArrayList<HttpHeaderData>();
        headerDataList.add(acceptData);
        headerDataList.add(authData);

        RestConnector restConnector = new RestConnector();

        String json = restConnector.getRestData(restUrl, headerDataList);

        return json;
    }

    /**
     * Get the branches json string value
     *
     * @return
     */

    private String getBranchesJsonString() {
        String branchesJsonString;
        try {
            branchesJsonString = getRestJsonString(getBranchesURL());
        } catch (Exception e) {
            log.error("Exception occurred", e);
            branchesJsonString = "[]";
        }

        return branchesJsonString;

    }

    /**
     * Get the commits json string value
     *
     * @return commits json String
     */

    private String getCommitsJsonString(String sha) {
        String commitsJsonString;
        try {
            commitsJsonString = getRestJsonString(getCommitsURL(sha));
        } catch (Exception e) {
            log.error("Exception occurred", e);
            commitsJsonString = "[]";
        }

        return commitsJsonString;
    }

    /**
     * Get the commit stats json string  // LIMITATION only 100 contributors returned
     *
     * @return json string
     */
    private String getCommitStatsJsonString() {
        String json;
        try {
            json = getRestJsonString(getStatsURL());
        } catch (Exception e) {
            log.error("Exception occurred", e);
            json = "[]";
        }
        return json;
    }
    /**
     * Get the weekly commit stats json string
     *
     * @return json string
     */

    private String getWeeklyCommitsJsonString(){

        String json = "{}";
        try {
            json = getRestJsonString(getWeekStatsURL());
        } catch (Exception e) {
            log.error("Exception occurred ", e);
            json = "{}";
        }
        return json;
    }

    /**
     * Get the weekly commit report
     *
     * @param json JSON String of the weekly commits
     * @return ArrayList of weekly commits
     */
    private void parseWeeklyCommits(String json) {

        ArrayList<Long> list = new ArrayList<Long>();
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray array = (JSONArray) jsonObject.get(Constants.ALL);
            if (jsonObject != null && array != null) {
                for (int i = 0; i < array.size(); i++) {
                    long numberofCommits = (Long) array.get(i);
                    list.add(numberofCommits);
                }
            }
            this.commitLastYear = list.toString();
        } catch (ParseException e) {
            log.error("PARSE EXCEPTION", e);
        }

    }


    /**
     * Get the top contributor and the total number of commits
     *
     * @return json string
     */
    private void parseCommitsStatsJsonString(String json) {

        long commits = 0;
        long totalCommits = 0;
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONArray jsonArrayObject = (JSONArray) obj;

            Iterator<JSONObject> iterator = jsonArrayObject.iterator(); //*************** if more than 100 contributors

            while (iterator.hasNext()) {
                RepoCommitersData committer = new RepoCommitersData();
                committer.setGitRepoName(this.gitRepoName);

                JSONObject jsonObject = iterator.next();
                long total = (Long) jsonObject.get(Constants.TOTAL);
                committer.setNumberofCommits("" + total);
                totalCommits += total;
                JSONObject authorObject = (JSONObject) jsonObject.get(Constants.AUTHOR);
                committer.setCommitterName((String) authorObject.get(Constants.LOGIN));

                if (total >= commits) {
                    this.topContributor = (String) authorObject.get(Constants.LOGIN);
                }
                commitersList.add(committer);
            }
            this.totalNUmberOfCommits = totalCommits;

        } catch (ParseException e) {
            log.error("PARSE EXCEPTION", e);
        }
    }


    /**
     * Parse the commits json string populate latest commits and last commit date time
     *
     * @param json
     * @return
     */
    private void parseCommitsJsonString(String json) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONArray jsonArrayObject = (JSONArray) obj;

            //set the last commit date
            if (jsonArrayObject.size() > 0) {
                JSONObject jObject = (JSONObject) jsonArrayObject.get(0);
                JSONObject jObject1 = (JSONObject) jObject.get(Constants.COMMIT);
                JSONObject commitObject = (JSONObject) jObject1.get(Constants.COMMITTER);
                this.lastCommitDateTime = (String) commitObject.get(Constants.DATE);
            }

            Iterator<JSONObject> iterator = jsonArrayObject.iterator();

            //populate the commits list
            while (iterator.hasNext()) {
                RepoCommitsData commitsData = new RepoCommitsData();

                JSONObject jsonObject = iterator.next();
                commitsData.setId(this.gitRepoName);
                commitsData.setShaValue((String) jsonObject.get(Constants.SHA));
                JSONObject jsonCommmitObject = (JSONObject) jsonObject.get(Constants.COMMIT);
                commitsData.setCommitMessage((String) jsonCommmitObject.get(Constants.MESSAGE));
                JSONObject jsonAuthorObject = (JSONObject) jsonCommmitObject.get(Constants.AUTHOR);
                commitsData.setAuthorName((String) jsonAuthorObject.get(Constants.NAME));
                commitsData.setAuthorEMail((String) jsonAuthorObject.get(Constants.EMAIL));
                commitsData.setAuthorDate((String) jsonAuthorObject.get(Constants.DATE));

                commitsList.add(commitsData);

            }

        } catch (ParseException e) {
            log.error("PARSE EXCEPTION", e);
        }
    }

    /**
     * Get pull requests json String
     */

    private String getPullRequestsJsonString() {
        String pullRequesstsJsonString;
        try {
            pullRequesstsJsonString = getRestJsonString(getPullRequestURL());
        } catch (Exception e) {
            log.error("Exception occurred", e);
            pullRequesstsJsonString = "[]";
        }

        return pullRequesstsJsonString;
    }

    private void parsePullRequestsJsonString(String json) {
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONArray jsonObject = (JSONArray) obj;
            if (jsonObject != null)
                this.totalPullRequestCount = jsonObject.size();
        } catch (ParseException e) {
            log.error("PARSE EXCEPTION", e);
        }
    }

    /**
     * Get the branches names and sha values
     *
     * @param json string value contains branches details
     * @return map contains branches and sha values
     */
    private Map<String, String> getGitBranches(String json) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONArray jsonObject = (JSONArray) obj;

            Iterator<JSONObject> iterator = jsonObject.iterator();
            String name, sha;
            while (iterator.hasNext()) {
                JSONObject jObject = iterator.next();
                name = (String) jObject.get(Constants.NAME);
                JSONObject jObject1 = (JSONObject) jObject.get(Constants.COMMIT);
                sha = (String) jObject1.get(Constants.SHA);
                map.put(name, sha);
            }
        } catch (ParseException e) {
            log.error("PARSE EXCEPTION", e);
        }
        return map;
    }

    /**
     * Create the rest url string
     *
     * @return rest url
     */
    private String getBranchesURL() {
        String url;

        url = this.gitRestUrl + this.gitOwner + "/" + this.gitRepoName + "/branches";
        log.info("Git Rest URl :: " + url);
        return url;
    }

    /**
     * Create the rest url string
     *
     * @return rest url
     */
    private String getCommitsURL(String sha) {
        String url;
        url = this.gitRestUrl + this.gitOwner + "/" + this.gitRepoName + "/commits?sha=" + sha;
        log.info("Git Rest URl :: " + url);
        return url;
    }

    /**
     * Create the rest url string
     *
     * @return rest url
     */
    private String getStatsURL() {
        String url;
        url = this.gitRestUrl + this.gitOwner + "/" + this.gitRepoName + "/stats/contributors";
        log.info("Git Rest URl :: " + url);
        return url;
    }

    /**
     * Create the rest url string
     *
     * @return rest url
     */
    private String getPullRequestURL() {
        String url;
        url = this.gitRestUrl + this.gitOwner + "/" + this.gitRepoName + "/pulls?per_page=100";
        log.info("Git Rest URl :: " + url);
        return url;
    }

    /**
     * Create the rest url string
     *
     * @return rest url
     */
    private String getWeekStatsURL() {
        String url;
        url = this.gitRestUrl + this.gitOwner + "/" + this.gitRepoName + "/stats/participation";
        log.info("Git Rest URl :: " + url);
        return url;
    }

    @Override
    public String getRepositoryID() {
        return this.gitRepoName;
    }

    @Override
    public String getOwnerID() {
        return this.gitOwner;
    }

    @Override
    public long getTotalNumberOfCommits() {
        return this.totalNUmberOfCommits;
    }

    @Override
    public String getTopContributor() {
        return this.topContributor;
    }

    @Override
    public int getTotalPullRequestCount() {
        return this.totalPullRequestCount;
    }

    @Override
    public String getLastCommitDateTime() {
        return this.lastCommitDateTime;
    }

    @Override
    public String getCommitLastYear() {
        return this.commitLastYear;
    }

    @Override
    public List<RepoCommitsData> getLatestCommitsList() {
        return this.commitsList;
    }

    @Override
    public List<RepoCommitersData> getLatestCommitersList() {
        return this.commitersList;
    }
    // public static void main(String[] args) {

    //     for (int i = 0; i < 20; i++) {

    //     GithubConnector c = new GithubConnector("carbon-kernel" , "wso2" , "https://api.github.com/repos/" , "token 9c52f8a6a0051059051f8ab3ae9ec110efaa20a5");

    //     System.out.println(c.getRepositoryID());
    //     System.out.println(c.getOwnerID());
    //     System.out.println(c.getTotalNumberOfCommits());
    //     System.out.println(c.getTopContributor());
    //     System.out.println(c.getTotalPullRequestCount());
    //     System.out.println(c.getLastCommitDateTime());
    //     System.out.println(c.getCommitLastYear());
    //     System.out.println(c.getLatestCommitsList().toString());
    //     System.out.println(c.getLatestCommitersList().toString());
    //     }

    // }
}
