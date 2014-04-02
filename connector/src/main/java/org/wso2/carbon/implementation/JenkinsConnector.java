package org.wso2.carbon.implementation;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.connector.ContinuousIntegrationConnector;
import org.wso2.carbon.connector.RestConnector;
import org.wso2.carbon.data.BuildCommitsData;
import org.wso2.carbon.data.HttpHeaderData;
import org.wso2.carbon.utils.Constants;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by aruna on 3/21/14.
 */
public class JenkinsConnector implements ContinuousIntegrationConnector {

    private static Logger log = Logger.getLogger(JenkinsConnector.class);

    private String jobName;
    private String jenkinsUrl;
    private String planName;
    private String buildState;
    private String buildNumber;
    private String successRate;
    private String buildDate;
    private String duration;
    private ArrayList<BuildCommitsData> commitsList = new ArrayList<BuildCommitsData>();

    private ArrayList<String> buildNumberList = new ArrayList<String>();

    public JenkinsConnector(String url, String jobName) throws URISyntaxException {
        this.jobName = jobName;
        this.jenkinsUrl = url;

        fetchData();
    }

    /**
     * Fetch data from the jenkins rest api
     */
    private void fetchData() {
        getJenkinsData();
        getBuildCommitsData();
    }


    /**
     * Get the commits list for a particular build
     *
     * @return commits list
     */
    private void getBuildCommitsData() {

        String json = getRestJobJsonString(getRestURLForBuildNumber(this.jobName, this.buildNumber));
        if (json != null) {
            try {
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(json);
                JSONObject jsonObject = (JSONObject) obj;
                if (jsonObject != null) {
                    JSONObject jsonChangetSetObject = (JSONObject) jsonObject.get(Constants.CHANGE_SET);

                    if (jsonChangetSetObject != null) {
                        JSONArray jsonChangeSetObjectArray = (JSONArray) jsonChangetSetObject.get(Constants.ITEMS);
                        log.info("Number of commits :: " + jsonChangeSetObjectArray.size());
                        Iterator<JSONObject> iterator = jsonChangeSetObjectArray.iterator();

                        while (iterator.hasNext()) {
                            JSONObject jObject = iterator.next();

                            BuildCommitsData data = new BuildCommitsData();
                            data.setId(this.jobName+"-"+this.buildNumber);
                            data.setComment(jObject.get(Constants.COMMENT).toString().trim());
                            data.setChangesetID(jObject.get(Constants.ID).toString().trim());
                            data.setDate(jObject.get(Constants.DATE).toString().trim());

                            JSONObject userObject = (JSONObject) jObject.get(Constants.AUTHOR);
                            data.setCommitAuthor(userObject.get(Constants.FULL_NAME).toString().trim());

                            commitsList.add(data);

                        }

                    }
                }
            } catch (org.json.simple.parser.ParseException e) {
                log.error("Error getting jenkins commits list", e);
            }
        }
    }

    /**
     * Get the json string for a particular job
     *
     * @return
     */
    private String getRestJobJsonString(String url) {
        String json = null;
        try {
            json = getJenkinsRestData(url);
        } catch (IOException e) {
            log.error("Error getting Jenkins Commits data ::", e);
        } catch (HttpException e) {
            log.error("Error getting Jenkins Commits data ::", e);
        }
        return json;
    }

    /**
     * Populate  data from the jenkins rest api
     */
    private void getJenkinsData() {

        String json = getRestJobJsonString(getRestURL(this.jobName));
        getLatestBuildNumber(json);
        String latestBuildJson = getRestJobJsonString(getRestURLForBuildNumber(this.jobName, this.buildNumber));
        //System.out.println(latestBuildJson);
        parseLatestBuildString(latestBuildJson);
        calculateSuccessRate();

    }

    /**
     * Get all the build details
     * @param json Json String of the build
     */

    private void parseLatestBuildString(String json) {
        try{
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONObject jsonObject = (JSONObject) obj;

            this.duration = String.valueOf( jsonObject.get("duration"));
            this.buildState = (String) jsonObject.get("result");
            this.buildDate = timestampToDate((Long) jsonObject.get("timestamp"));
            this.planName = (String)jsonObject.get("id");

        }catch (org.json.simple.parser.ParseException e) {
            log.error("Error getting jenkins commits list", e);
        }

    }

    /**
     * Parse the json string
     */

    private long getLatestBuildNumber(String json) {

        long latestBuildNumber = 0;

        if (json != null) {
            try {
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(json);
                JSONObject jsonObject = (JSONObject) obj;
                latestBuildNumber = (Long) jsonObject.get("nextBuildNumber") - 1;
                this.buildNumber = String.valueOf(latestBuildNumber);
                if (jsonObject != null) {
                    JSONArray buildsArray = (JSONArray) jsonObject.get("builds");

                    Iterator<JSONObject> iterator = buildsArray.iterator();
                    int counter = 0;
                    while (iterator.hasNext()) {
                        counter++;
                        if (counter == 25) {
                            break;
                        }
                        JSONObject jObject = iterator.next();
                        buildNumberList.add(String.valueOf(jObject.get("number")));
                        //System.out.println("adding url " + jObject.get("number"));
                    }
                }
            } catch (org.json.simple.parser.ParseException e) {
                log.error("Error getting jenkins commits list", e);
            }
        }
        return latestBuildNumber;
    }

    /**
     * Convert unix timestamp to a data format
     *
     * @param timestamp unix timesstamp value
     * @return date string
     */

    private String timestampToDate(long timestamp) {
        String formatDateString;

        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd  HH:mm:ss");
        formatDateString = sdf.format(date);
        return formatDateString;
    }


    /**
     * Calculate the success rate of the job
     *
     * @return success rate of the job
     */

    private void calculateSuccessRate() {

        float numberOfSuccessBuilds = 0;
        int totalBuilds = buildNumberList.size();
        for(String number : buildNumberList){//http://ec2-54-83-33-151.compute-1.amazonaws.com:8080/job/C5-carbon-kernel/44/api/json
            String json =  getRestJobJsonString(this.jenkinsUrl+"job/"+this.jobName+"/"+number+"/api/json");
           // System.out.println("success rate :: "+json);
            if(json != null){
                try{
                    JSONParser parser = new JSONParser();
                    Object obj = parser.parse(json);
                    JSONObject jsonObject = (JSONObject) obj;

                    if(((String)jsonObject.get("result")).equals("SUCCESS")){
                        numberOfSuccessBuilds++;
                    }

                }catch (org.json.simple.parser.ParseException e) {
                    log.error("Error getting jenkins commits list", e);
                }
            }

        }

        this.successRate = String.valueOf(( numberOfSuccessBuilds / totalBuilds) * 100);
    }

    /**
     * Get the rest result json string
     *
     * @param url parameter of the jenkins url
     * @return Json string of the result
     */
    private String getJenkinsRestData(String url) throws HttpException, IOException {

        HttpHeaderData acceptData = new HttpHeaderData(Constants.HEADER_ACCEPT, Constants.HEADER_ACCEPT_JSON);
        ArrayList<HttpHeaderData> headerDataList = new ArrayList<HttpHeaderData>();
        headerDataList.add(acceptData);

        RestConnector restConnector = new RestConnector();

        String json = restConnector.getRestData(url, headerDataList);

        return json;
    }

    /**
     * Get the rest url
     *
     * @param job    job name
     * @param number build number
     * @return rest url
     */
    private String getRestURLForBuildNumber(String job, String number) {
        String url = "";
        url = this.jenkinsUrl + "job/" + job + "/" + number + "/api/json";
        log.info("Jenkins Rest URl :: " + url);
        return url;
    }

    /**
     * Get the rest url
     *
     * @param job job name
     * @return rest url
     */
    private String getRestURL(String job) {

        String url = this.jenkinsUrl + "job/" + job + "/api/json";
        log.info("Jenkins Rest URl :: " + url);
        return url;
    }

    @Override
    public String getName() {
        return this.jobName;
    }

    @Override
    public String getID() {
        return this.planName;
    }

    @Override
    public String getBuildState() {
        return this.buildState;
    }

    @Override
    public String getBuildNumber() {
        return this.buildNumber;
    }

    @Override
    public String getSuccessRate() {
        return this.successRate;
    }

    @Override
    public String getBuildDate() {
        return this.buildDate;
    }

    @Override
    public String getDuration() {
        return this.duration;
    }

    @Override
    public ArrayList<BuildCommitsData> getCommitsData() {
        return this.commitsList;
    }

//    public static void main(String[] args) throws Exception {
//        JenkinsConnector p = new JenkinsConnector("http://ec2-54-83-33-151.compute-1.amazonaws.com:8080/", "C5-carbon-kernel");
////        // p.setJobName("test-jenkins");
//        System.out.println(p.getName());
//        System.out.println(p.getID());
//        System.out.println(p.getBuildState());
//        System.out.println(p.getBuildNumber());
//        System.out.println(p.getSuccessRate());
//        System.out.println(p.getBuildDate());
//        System.out.println(p.getDuration());
//        System.out.println(p.getCommitsData().toString());
//
//
//    }
}
