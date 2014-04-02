package org.wso2.carbon.implementation;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.connector.EmmaConnector;
import org.wso2.carbon.connector.HttpConnector;
import org.wso2.carbon.connector.RestConnector;
import org.wso2.carbon.data.EmmaData;
import org.wso2.carbon.data.EmmaPackageData;
import org.wso2.carbon.data.HttpHeaderData;
import org.wso2.carbon.utils.Constants;
import org.wso2.carbon.utils.JenkinsEmmaXMLParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by aruna on 3/25/14.
 */
public class JenkinsEmmaConnector implements EmmaConnector {

    private static Logger log = Logger.getLogger(JenkinsEmmaConnector.class);

    private String jenkinsUrl;
    private String jenkinsJobName;
    private String jenkinsoBuildNumber;

    private String reportID;
    private int numberOfPackages;
    private int numberOfClasses;
    private int numberOfMethods;
    private int numberOfBlocks;
    private int numberOfLines;
    private int numberOfFiles;

    private String allClassCoverage;
    private String allMethodCoverage;
    private String allBlockCoverage;
    private String allLineCoverage;

    private ArrayList<EmmaPackageData> list = new ArrayList<EmmaPackageData>();

    public JenkinsEmmaConnector(String jenkinsUrl, String jobName, String buildNumber) {

        this.jenkinsUrl = jenkinsUrl;
        this.jenkinsJobName = jobName;
        this.jenkinsoBuildNumber = buildNumber;

        fetchData();

    }

    /**
     * Fetch xml reports and populate
     */
    private void fetchData() {

        ArrayList<String> artifactUrlList = getAllArtifactsUrlList();
        ArrayList<EmmaData> emmaDataList = new ArrayList<EmmaData>();
        for (String url : artifactUrlList) {
            emmaDataList.add(getEmmaReportData(url));
        }

        EmmaData data = aggregateEmmaData(emmaDataList);
        this.reportID = this.jenkinsJobName + "-" + this.jenkinsoBuildNumber;
        this.numberOfPackages = data.getNumberOfPackages();
        this.numberOfClasses = data.getNumberOfClasses();
        this.numberOfMethods = data.getNumberOfMethods();
        this.numberOfBlocks = data.getNumberOfBlocks();
        this.numberOfLines = data.getNumberOfLines();
        this.numberOfFiles = data.getNumberOfFiles();

        this.allClassCoverage = data.getAllClassCoverage();
        this.allMethodCoverage = data.getAllMethodCoverage();
        this.allBlockCoverage = data.getAllBlockCoverage();
        this.allLineCoverage = data.getAllLineCoverage();
        this.list = data.getList();
        //System.out.println(data.toString());

    }

    /**
     * Get emma data of jacoco.xml file
     *
     * @param url
     */
    private EmmaData getEmmaReportData(String url) {
        EmmaData data = null;
        if (downloadEmmaReport(url)) {
            JenkinsEmmaXMLParser parser = new JenkinsEmmaXMLParser("jacoco.xml");
            data = parser.parse();
        }
        return data;
    }

    /**
     * @param emmaDataList
     * @return
     */
    private EmmaData aggregateEmmaData(ArrayList<EmmaData> emmaDataList) {
        EmmaData data = new EmmaData();
        String reportID;
        int numberOfPackages = 0;
        int numberOfClasses = 0;
        int numberOfMethods = 0;
        int numberOfBlocks = 0;
        int numberOfLines = 0;
        int numberOfFiles = 0;

        double allClassCoverage = 0;
        double allMethodCoverage = 0;
        double allBlockCoverage = 0;
        double allLineCoverage = 0;
        int count = 0;
        ArrayList<EmmaPackageData> list = new ArrayList<EmmaPackageData>();
        for (EmmaData emmaData : emmaDataList) {
            if (emmaData != null) {
                count++;
                numberOfPackages += emmaData.getNumberOfPackages();
                numberOfBlocks += emmaData.getNumberOfBlocks();
                numberOfClasses += emmaData.getNumberOfClasses();
                numberOfMethods += emmaData.getNumberOfMethods();
                numberOfFiles += emmaData.getNumberOfFiles();
                numberOfLines += emmaData.getNumberOfLines();

                allClassCoverage += Double.parseDouble(emmaData.getAllClassCoverage());
                allMethodCoverage += Double.parseDouble(emmaData.getAllMethodCoverage());
                allBlockCoverage += Double.parseDouble(emmaData.getAllBlockCoverage());
                allLineCoverage += Double.parseDouble(emmaData.getAllLineCoverage());


                list.addAll(emmaData.getList());
            }
        }

        if (count != 0) {
            data.setNumberOfClasses(numberOfClasses);
            data.setNumberOfLines(numberOfLines);
            data.setNumberOfBlocks(numberOfBlocks);
            data.setNumberOfPackages(numberOfPackages);
            data.setNumberOfMethods(numberOfMethods);
            System.out.println(allClassCoverage);
            System.out.println(count);

            data.setAllClassCoverage(String.valueOf(formatDoublevalue(allClassCoverage / count)));
            data.setAllMethodCoverage(String.valueOf(formatDoublevalue(allMethodCoverage / count)));
            data.setAllBlockCoverage(String.valueOf(formatDoublevalue(allBlockCoverage / count)));
            data.setAllLineCoverage(String.valueOf(formatDoublevalue(allLineCoverage / count)));

            data.setList(list);
        }

        return data;
    }

    private static double formatDoublevalue(double value) {
//        if()
        DecimalFormat df2 = new DecimalFormat("###.##");
        return Double.valueOf(df2.format(value));
    }

    /**
     * Download the emma report file
     *
     * @return success or failure
     */
    private boolean downloadEmmaReport(String url) {
        HttpConnector httpConnector = new HttpConnector();
        InputStream inputStream = httpConnector.getHttpData(url);
        FileOutputStream fileOutputStream = null;
        boolean downloadFlag = false;

        try {
            if (inputStream != null) {
                File file = new File("jacoco.xml");
                if (file.exists()) {
                    file.delete();
                }
                fileOutputStream = new FileOutputStream(file);

                int inByte;
                while ((inByte = inputStream.read()) != -1) {
                    fileOutputStream.write(inByte);
                }
                downloadFlag = true;
            } else {
                log.info("404 Emma xml File not found");
                File file = new File("jacoco.xml");
                if (file.exists()) {
                    file.delete();
                }
            }
        } catch (IOException e) {
            log.error("Exception", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error Occurred", e);
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    log.error("Error Occurred", e);
                }
            }

        }
        return downloadFlag;
    }


    /**
     * Get the artifact url list for emma reports
     *
     * @return
     */
    private ArrayList<String> getAllArtifactsUrlList() {
        ArrayList<String> artifactUrlList = new ArrayList<String>();
        try {
            String json = getJenkinsRestData(getRestUrl());
            if (json != null) {
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(json);
                JSONObject jsonObject = (JSONObject) obj;
                if (jsonObject != null) {

                    JSONArray jsonArtifactArray = (JSONArray) jsonObject.get("artifacts");
                    Iterator<JSONObject> iterator = jsonArtifactArray.iterator();

                    while (iterator.hasNext()) {
                        JSONObject jObject = iterator.next();
                        System.out.println(jObject.get("relativePath"));
                        String relativePath = (String) jObject.get("relativePath");
//http://ec2-54-83-33-151.compute-1.amazonaws.com:8080/job/test-c5-dev-branch-for-emma/lastSuccessfulBuild/artifact/modules/clustering/target/site/jacoco/jacoco.xml
                        if (relativePath != null) {
                            String url = this.jenkinsUrl + "job/" + this.jenkinsJobName + "/lastSuccessfulBuild/artifact/" + relativePath;
                            System.out.println(url);
                            artifactUrlList.add(url);
                        }
                    }
                }
            }

        } catch (IOException e) {
            log.error("Error getting artifacts data ::", e);
        } catch (HttpException e) {
            log.error("Error getting artifacts data ::", e);
        } catch (ParseException e) {
            log.error("Error getting artifacts data ::", e);
        }

        return artifactUrlList;
    }

    /**
     * Get the rest url
     *
     * @return url
     */
    private String getRestUrl() {    //http://ec2-54-83-33-151.compute-1.amazonaws.com:8080/job/C5-carbon-kernel/api/json

        String url = this.jenkinsUrl + "job/" + this.jenkinsJobName + "/" + this.jenkinsoBuildNumber + "/api/json";

        return url;
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

    @Override
    public String getReportID() {
        return this.reportID;
    }

    @Override
    public int getNumberOfPackages() {
        return this.numberOfPackages;
    }

    @Override
    public int getNumberOfClasses() {
        return this.numberOfClasses;
    }

    @Override
    public int getNumberOfMethods() {
        return this.numberOfMethods;
    }

    @Override
    public int getNumberOfBlocks() {
        return this.numberOfBlocks;
    }

    @Override
    public int getNumberOfLines() {
        return this.numberOfLines;
    }

    @Override
    public int getNumberOfFiles() {
        return this.numberOfFiles;
    }

    @Override
    public String getAllClassCoverage() {
        return this.allClassCoverage;
    }

    @Override
    public String getAllMethodCoverage() {
        return this.allMethodCoverage;
    }

    @Override
    public String getAllBlockCoverage() {
        return this.allBlockCoverage;
    }

    @Override
    public String getAllLineCoverage() {
        return this.allLineCoverage;
    }

    @Override
    public ArrayList<EmmaPackageData> getEmmaPachageData() {
        return this.list;
    }


//    public static void main(String[] args) {//http://ec2-54-83-33-151.compute-1.amazonaws.com:8080/job/test-c5-dev-branch-for-emma/4/api/json
//        JenkinsEmmaConnector con = new JenkinsEmmaConnector("http://ec2-54-83-33-151.compute-1.amazonaws.com:8080/", "test-c5-dev-branch-for-emma", "4");
//    }
}
