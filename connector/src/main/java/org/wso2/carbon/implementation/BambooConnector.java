package org.wso2.carbon.implementation;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.connector.ContinuousIntegrationConnector;
import org.wso2.carbon.connector.RestConnector;
import org.wso2.carbon.data.BuildCommitsData;
import org.wso2.carbon.data.HttpHeaderData;
import org.wso2.carbon.utils.Constants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by aruna on 3/21/14.
 */
public class BambooConnector implements ContinuousIntegrationConnector {

    private static Logger log = Logger.getLogger(BambooConnector.class);

    private String bambooUrl;
    private String projectID;
    private String planName;
    private String buildState;
    private String buildNumber;
    private String successRate;
    private String buildDate;
    private String duration;
    private ArrayList<BuildCommitsData> commitsList = new ArrayList<BuildCommitsData>();


    public BambooConnector(String bambooUrl, String projectID) {
        this.bambooUrl = bambooUrl;
        this.projectID = projectID;

        fetchData();
    }

    /**
     * Call github rest api and populate data
     */
    private void fetchData() {

        ArrayList<String> planList = parseBambooProjectJsonString(getBambooProjectJsonString("0"));
        if (planList.size() >= 25) {
            ArrayList<String> list = parseBambooProjectJsonString(getBambooProjectJsonString("10"));
            planList.addAll(list);
        }

        Map<String, Long> planNumberMap = getPlanNumberMap(planList);
        Map<String, String> planDateMap = getPlanDateMap(planNumberMap);
        String latestBuildPlan = getTheLatestBuildPlan(planDateMap);
        this.planName = latestBuildPlan;
        parseLatestBuildPlan(latestBuildPlan, String.valueOf(planNumberMap.get(latestBuildPlan)));

    }

    /**
     * Create the rest url string
     *
     * @return rest url
     */
    private String getProjectUrl(String startIndex) {
        String url;
        url = this.bambooUrl + "/project/" + this.projectID + "?expand=plans&start-index=" + startIndex;
        log.info("Git Rest URl :: " + url);
        return url;
    }

    /**
     * Create the rest url string
     *
     * @return rest url
     */
    private String getAllPlanUrl(String plan) {
        String url;
        url = this.bambooUrl + "/result/" + plan + "";
        log.info("Git Rest URl :: " + url);
        return url;
    }

    /**
     * Create the rest url string
     *
     * @return rest url
     */
    private String getLatestPlanUrl(String plan, String number) {
        String url;
        url = this.bambooUrl + "/result/" + plan + "-" + number + "?expand=changes.change.files";
        log.info("Git Rest URl :: " + url);
        return url;
    }

    /**
     * Get the rest result json string
     *
     * @param bambooRestUrl
     * @return Json string of the result
     */
    private String getBambooRestData(String bambooRestUrl) throws HttpException, IOException {

        HttpHeaderData acceptData = new HttpHeaderData(Constants.HEADER_ACCEPT, Constants.HEADER_ACCEPT_JSON);
        ArrayList<HttpHeaderData> headerDataList = new ArrayList<HttpHeaderData>();
        headerDataList.add(acceptData);

        RestConnector restConnector = new RestConnector();

        String json = restConnector.getRestData(bambooRestUrl, headerDataList);

        return json;
    }

    /**
     * Get the json String of a project
     *
     * @return json string
     */
    private String getBambooProjectJsonString(String startIndex) {

        String jsonPlanString;
        try {
            jsonPlanString = getBambooRestData(getProjectUrl(startIndex));
        } catch (Exception e) {
            log.error("Exception", e);
            jsonPlanString = "{}";
        }
        return jsonPlanString;
    }

    /**
     * Get the plan and their latest plan number details
     *
     * @param planList plan list for a project
     * @return plan and their numbers map
     */
    private Map<String, Long> getPlanNumberMap(ArrayList<String> planList) {
        Map<String, Long> planNUmberMap = new HashMap<String, Long>();
        String jsonPlanString;
        for (String plan : planList) {
            try {
                jsonPlanString = getBambooRestData(getAllPlanUrl(plan));
            } catch (Exception e) {
                log.error("Exception", e);
                jsonPlanString = "{}";
            }
            long number = getLatestPlanNumber(jsonPlanString);
            planNUmberMap.put(plan, number);
        }

        return planNUmberMap;
    }

    /**
     * Get the plan and the build dates map
     *
     * @param planNumberMap plan and number map
     * @return plan and buiild date map
     */
    private Map<String, String> getPlanDateMap(Map<String, Long> planNumberMap) {
        Map<String, String> planDateMap = new HashMap<String, String>();

        Set<String> plansKeySet = planNumberMap.keySet();
        for (String plan : plansKeySet) {
            long number = planNumberMap.get(plan);

            String jsonPlanNumberString;
            try {
                jsonPlanNumberString = getBambooRestData(getLatestPlanUrl(plan, String.valueOf(number)));
            } catch (Exception e) {
                log.error("Exception getting plan ::" + plan, e);
                jsonPlanNumberString = "{}";
            }

            try {
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(jsonPlanNumberString);
                JSONObject jsonObject = (JSONObject) obj;
                if (jsonObject != null) {
                    String buildCompletedTime = (String) jsonObject.get(Constants.BUILD_COMPLETED_TIME);
                    planDateMap.put(plan, buildCompletedTime);
                }
            } catch (ParseException e) {
                log.error("PARSE EXCEPTION", e);
            }
        }

        return planDateMap;
    }

    /**
     * Get the latest build plan
     *
     * @param planDateMap Plan Date Map
     * @return Latest build plan
     */

    private String getTheLatestBuildPlan(Map<String, String> planDateMap) {
        Set<String> plansKeySet = planDateMap.keySet();
        SimpleDateFormat simpleDateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String latestBuildPlan = "";
        try {
            Date latestBuildDateTime = simpleDateFormatter.parse("1900-01-01T00:00:00"); // older date to start comparison

            for (String plan : plansKeySet) {
                String date = planDateMap.get(plan);
                if (date != null) {
                    Date dateObject = simpleDateFormatter.parse(date);

                    if (dateObject.compareTo(latestBuildDateTime) > 0) {
                        latestBuildDateTime = dateObject;
                        latestBuildPlan = plan;
                    }
                }

            }
        } catch (java.text.ParseException e) {
            log.error("PARSE EXCEPTION", e);
        }
        return latestBuildPlan;
    }

    /**
     * Parse the latest build plan
     *
     * @param latestPlan
     * @param latestBuildNumber
     */

    private void parseLatestBuildPlan(String latestPlan, String latestBuildNumber) {

        try {
            String json = getBambooRestData(getLatestPlanUrl(latestPlan, latestBuildNumber));

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONObject jsonObject = (JSONObject) obj;

            if (jsonObject != null) {
                JSONObject jsonPlanObject = (JSONObject) jsonObject.get(Constants.PLAN);
                if (jsonPlanObject != null) {
                    String key = (String) jsonPlanObject.get(Constants.KEY);
                    this.buildNumber = String.valueOf(jsonObject.get(Constants.NUMBER));
                    this.buildState = ((String) jsonObject.get(Constants.STATE));
                    this.buildDate = (((String) jsonObject.get(Constants.BUILD_COMPLETED_TIME)).substring(0, 19));
                    this.duration = ((String) jsonObject.get(Constants.BUILD_RELATIVE_TIME));
                    this.successRate = String.valueOf(calculateSuccessRate(latestPlan));

                    JSONObject commitsJonObject = (JSONObject) jsonObject.get(Constants.CHANGES);
                    JSONArray commitsJsonArray = (JSONArray) commitsJonObject.get(Constants.CHANGE);

                    Iterator<JSONObject> iterator = commitsJsonArray.iterator();

                    while (iterator.hasNext()) {
                        BuildCommitsData commitsData = new BuildCommitsData();
                        JSONObject jObject = iterator.next();

                        commitsData.setId(key);
                        commitsData.setCommitAuthor((String) jObject.get(Constants.AUTHOR));
                        commitsData.setComment((String) jObject.get(Constants.COMMENT));
                        commitsData.setChangesetID((String) jObject.get(Constants.CHANGE_SET_ID));
                        commitsData.setDate((String) jObject.get(Constants.DATE));
                        commitsList.add(commitsData);
                    }
                }
            }

        } catch (HttpException e) {
            log.error("HTTP Exception ", e);
        } catch (IOException e) {
            log.error("IO Exception ", e);
        } catch (ParseException e) {
            log.error("IO Exception ", e);
        }
    }

    /**
     * Return the success rate for a particular build plan
     *
     * @param plan build plan id
     * @return success rate
     */
    public float calculateSuccessRate(String plan) {

        float successRate = 0;
        String jsonString;
        try {
            jsonString = getBambooRestData(getAllPlanUrl(plan));
        } catch (Exception e) {
            log.error("Exception ", e);
            jsonString = "{}";
        }

        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(jsonString);
            JSONObject jsonObject = (JSONObject) obj;
            if (jsonObject != null) {
                JSONObject jsonResultsObject = (JSONObject) jsonObject.get(Constants.RESULTS);
                if (jsonResultsObject != null) {
                    long size = (Long) jsonResultsObject.get(Constants.SIZE);
                    long success = 0;
                    if (size != 0) {
                        JSONArray jsonPlanResultsObjectArray = (JSONArray) jsonResultsObject.get(Constants.RESULT);

                        Iterator<JSONObject> iterator = jsonPlanResultsObjectArray.iterator();

                        while (iterator.hasNext()) {
                            JSONObject jObject = iterator.next();
                            String state = (String) jObject.get(Constants.STATE);
                            if (state.equals(Constants.SUCCESS_STRING)) {
                                ++success;
                            }
                        }
                        float successFloatVal = (float) success;
                        successRate = (successFloatVal / size) * 100;
                    }
                }
            }
        } catch (ParseException e) {
            log.error("PARSE EXCEPTION", e);
        }

        return successRate;
    }

    /**
     * Get the latest plan build number
     *
     * @param json rest json String
     * @return latest build number
     */
    private long getLatestPlanNumber(String json) {
        long number = 0;
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONObject jsonObject = (JSONObject) obj;

            if (jsonObject != null) {
                JSONObject jsonPlansObject = (JSONObject) jsonObject.get(Constants.RESULTS);
                if (jsonPlansObject != null) {
                    JSONArray jsonPlanObjectArray = (JSONArray) jsonPlansObject.get(Constants.RESULT);

                    if (jsonPlanObjectArray.size() > 0) {
                        JSONObject jObject = (JSONObject) jsonPlanObjectArray.get(0);
                        number = (Long) jObject.get(Constants.NUMBER);
                    }
                }
            }
        } catch (ParseException e) {
            log.error("PARSE EXCEPTION", e);
        }
        return number;
    }

    /**
     * Get the plan list of a project
     *
     * @return plans string list
     */
    private ArrayList<String> parseBambooProjectJsonString(String json) {

        ArrayList<String> planList = new ArrayList<String>();
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONObject jsonObject = (JSONObject) obj;
            if (jsonObject != null) {
                JSONObject jsonPlansObject = (JSONObject) jsonObject.get(Constants.PLANS);

                if (jsonPlansObject != null) {
                    JSONArray jsonPlanObjectArray = (JSONArray) jsonPlansObject.get(Constants.PLAN);
                    log.info("Number of Plans :: " + jsonPlanObjectArray.size());
                    Iterator<JSONObject> iterator = jsonPlanObjectArray.iterator();

                    while (iterator.hasNext()) {
                        JSONObject jObject = iterator.next();
                        planList.add((String) jObject.get(Constants.KEY));
                    }
                }
            }
        } catch (ParseException e) {
            log.error("PARSE EXCEPTION", e);
        }
        return planList;
    }

    @Override
    public String getName() {
        return this.planName;
    }

    @Override
    public String getID() {
        return this.projectID;
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


//    public static void main(String[] args) {
//        ContinuousIntegrationConnector c = new BambooConnector("https://wso2.org/bamboo/rest/api/latest", "WSO2CARBON");
//        System.out.println(c.getBuildDate());
//        System.out.println(c.getBuildNumber());
//        System.out.println(c.getBuildState());
//        System.out.println(c.getCommitsData().toString());
//        System.out.println(c.getDuration());
//        System.out.println(c.getName());
//        System.out.println(c.getID());
//        System.out.println(c.getSuccessRate());
//
//    }
}
