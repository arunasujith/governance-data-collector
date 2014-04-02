package org.wso2.carbon.implementation;

import org.apache.log4j.Logger;
import org.wso2.carbon.connector.ContinuousIntegratioonRulesConnector;
import org.wso2.carbon.connector.HttpConnector;
import org.wso2.carbon.data.BuildRuleData;
import org.wso2.carbon.data.BuildRuleStat;
import org.wso2.carbon.utils.JenkinsBuildLogParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * JenkinsRulesConnector.java
 * <p/>
 * Extract build rules related details from jenkins build logs
 */
public class JenkinsRulesConnector implements ContinuousIntegratioonRulesConnector {

    private static Logger log = Logger.getLogger(JenkinsEmmaConnector.class);

    private String jenkinsUrl;
    private String jenkinsJobName;
    private String jenkinsoBuildNumber;
    private JenkinsBuildLogParser jenkinsBuildLogParser;
    private ArrayList<BuildRuleData> buildRuleDataArrayList;
    private BuildRuleStat buildRuleStat;

    public JenkinsRulesConnector(String jenkinsUrl, String jobName, String buildNumber) {

        this.jenkinsUrl = jenkinsUrl;
        this.jenkinsJobName = jobName;
        this.jenkinsoBuildNumber = buildNumber;

        fetchData();

    }

    private void fetchData() {
        String url = getBuildLogUrl();
        System.out.println(url);

        if (downloadBuildLogFile(url)) {
            jenkinsBuildLogParser = new JenkinsBuildLogParser();
            this.buildRuleDataArrayList = jenkinsBuildLogParser.getBuildRuleDataList();
//            for (BuildRuleData data : buildRuleDataArrayList)
//                log.info(data.toString());

            this.buildRuleStat = jenkinsBuildLogParser.getBuildRuleStats();
//            System.out.println(jenkinsBuildLogParser.getBuildRuleStats().toString());
        }
    }

    /**
     * Download the build log file
     *
     * @return success or failure
     */
    private boolean downloadBuildLogFile(String url) {
        HttpConnector httpConnector = new HttpConnector();
        InputStream inputStream = httpConnector.getHttpData(url);
        FileOutputStream fileOutputStream = null;
        boolean downloadFlag = false;

        try {
            if (inputStream != null) {
                File file = new File("build.log");
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
                log.info("Build log File not found");
                File file = new File("build.log");
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
     * Get the build log file url
     *
     * @return url value
     */
    private String getBuildLogUrl() {

        String url = this.jenkinsUrl + "job/" + this.jenkinsJobName + "/" + this.jenkinsoBuildNumber + "/consoleText";
        return url;
    }

    @Override
    public String getBuildID() {
        return this.jenkinsJobName+"-"+this.jenkinsoBuildNumber;
    }

    @Override
    public ArrayList<BuildRuleData> getBuildRuleData() {
        return this.buildRuleDataArrayList;
    }

    @Override
    public BuildRuleStat getBuildRuleStatData() {
        return this.buildRuleStat;
    }

//    public static void main(String[] args) {
//        JenkinsRulesConnector con = new JenkinsRulesConnector("http://ec2-54-83-33-151.compute-1.amazonaws.com:8080/", "C5-carbon-kernel-rule-validation", "42");
//
//    }
}
