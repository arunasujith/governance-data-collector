package org.wso2.carbon.manager;

import org.apache.log4j.Logger;
import org.wso2.carbon.data.BuildCommitsData;
import org.wso2.carbon.data.BuildRuleData;
import org.wso2.carbon.data.BuildRuleStat;
import org.wso2.carbon.data.EmmaPackageData;
import org.wso2.carbon.implementation.JenkinsConnector;
import org.wso2.carbon.implementation.JenkinsEmmaConnector;
import org.wso2.carbon.implementation.JenkinsRulesConnector;
import org.wso2.carbon.publisher.BAMDataPublisher;
import org.wso2.carbon.utils.Configurations;

import java.util.ArrayList;

/**
 * Created by aruna on 3/24/14.
 */
public class JenkinsManager {


    private static Logger log = Logger.getLogger(JenkinsManager.class);
    public static final String JENKINS_STREAM = "Jenkins_Stream";  // store bamboo builds data
    public static final String JENKINS_EMMA_STREAM = "Jenkins_Emma_Stream"; // store emma report data
    public static final String JENKINS_COMMITS_STREAM = "Jenkins_Commits_Stream"; // store bamboo commits for a particular build
    public static final String JENKINS_BUILD_RULE_STREAM = "Jenkins_Build_Rule_Stream"; // store jenkins rules status ofr a particular build
    public static final String JENKINS_BUILD_RULE_STAT_STREAM = "Jenkins_Build_Rule_Stat_Stream"; // Store jenkins build rule statistics data
    public static final String VERSION = "1.0.0";

    BAMDataPublisher pub = new BAMDataPublisher();


    /**
     * Extract data from Bamboo api's and save data in BAM
     *
     * @param jenkinsProjectID bamboo project ID
     */
    public void execute(String jenkinsProjectID) {

        try {

            JenkinsConnector jenkinsConnector = new JenkinsConnector(Configurations.getWSO2_JENKINS_URL(), jenkinsProjectID);
            JenkinsEmmaConnector jenkinsEmmaConnector = new JenkinsEmmaConnector(Configurations.getWSO2_JENKINS_URL(), jenkinsProjectID, jenkinsConnector.getBuildNumber());
            JenkinsRulesConnector jenkinsRulesConnector = new JenkinsRulesConnector(Configurations.getWSO2_JENKINS_URL(), jenkinsProjectID, jenkinsConnector.getBuildNumber());

            ArrayList<BuildRuleData> buildRuleDataArrayList = jenkinsRulesConnector.getBuildRuleData();
            log.info("Build Rule Data List ::"+ buildRuleDataArrayList.toString() );

            ArrayList<BuildCommitsData> jenkinsCommitsList = jenkinsConnector.getCommitsData();

            log.info("Jenkins Commits List :: " + jenkinsCommitsList.toString());
            log.info("Jenkins Build Data :: " + jenkinsConnector.getID() + "  " + jenkinsConnector.getSuccessRate() + "  " +
                    jenkinsConnector.getName() + "  " + jenkinsConnector.getDuration()
                    + " " + jenkinsConnector.getBuildDate() + " " + jenkinsConnector.getBuildNumber() + " " + jenkinsConnector.getBuildState());

            pub.buildDataPublish(getJenkinsDataArray(jenkinsConnector), JENKINS_STREAM, VERSION);// publish jenkins build data

            ArrayList<String[]> emmaDataList = getJenkinsEmmaDataList(jenkinsEmmaConnector);
            if (emmaDataList != null) {
                for (String[] emmaReport : emmaDataList) {
                    pub.buildEmmaDataPublish(emmaReport, JENKINS_EMMA_STREAM, VERSION); // publish jenkins emma data
                }
            }

            if (jenkinsCommitsList != null) {
                for (BuildCommitsData data : jenkinsCommitsList) {
                    pub.buildCommitsDataPublish(getJenkinsCommitsArray(data), JENKINS_COMMITS_STREAM, VERSION); // publish jenkins emma commits data
                }
            }

            if (buildRuleDataArrayList != null) {
                for (BuildRuleData data : buildRuleDataArrayList) {
                    pub.buildRulesDataPublish(getBuildRuleDataArray(jenkinsRulesConnector.getBuildID(), data), JENKINS_BUILD_RULE_STREAM, VERSION); // publish jenkins build rule data
                }
            }

            pub.buildRulesStatDataPublish(getBuildRuleStatDataArray(jenkinsRulesConnector.getBuildID(),jenkinsRulesConnector) , JENKINS_BUILD_RULE_STAT_STREAM , VERSION);


        } catch (Exception e) {
            log.error("Exception ", e);
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
     * Prepare the array to store in BAM
     *
     * @param buildID Build ID
     * @param jenkinsRulesConnector Jenkins Rules Connector
     * @return Build Rule stat Data array
     */
    private String[] getBuildRuleStatDataArray(String buildID, JenkinsRulesConnector jenkinsRulesConnector) {

        BuildRuleStat data = jenkinsRulesConnector.getBuildRuleStatData();

        String[] jenkinsBuildRuleStatData = new String[7];
        jenkinsBuildRuleStatData[0] = buildID;
        jenkinsBuildRuleStatData[1] = String.valueOf(data.getTotalRuleCount());
        jenkinsBuildRuleStatData[2] = String.valueOf(data.getExecutedRuleCount());
        jenkinsBuildRuleStatData[3] = String.valueOf(data.getInactiveRuleCount());
        jenkinsBuildRuleStatData[4] = String.valueOf(data.getExcludedRuleCount());
        jenkinsBuildRuleStatData[5] = String.valueOf(data.getMavenVersionIncompatibleRuleCount());
        jenkinsBuildRuleStatData[6] = String.valueOf(data.getSkippedExplicitRuleCount());

        log.info("Jenkins Build Rule Stats :: "+ buildID+"  "+data.toString());

        return jenkinsBuildRuleStatData;
    }


    /**
     * Prepare the array to store in BAM
     *
     * @param data Build Rule Data
     * @return Build Rule Data array
     */
    private String[] getBuildRuleDataArray(String buildID, BuildRuleData data) {

        String[] jenkinsBuildRuleData = new String[4];
        jenkinsBuildRuleData[0] = buildID;
        jenkinsBuildRuleData[1] = data.getComponentName();
        jenkinsBuildRuleData[2] = data.getRuleName();
        jenkinsBuildRuleData[3] = data.getStatus();

        return jenkinsBuildRuleData;
    }

    /**
     * Prepare the array to store in BAM
     *
     * @param data Jenkins commits data
     * @return jenkins commits data array
     */
    private String[] getJenkinsCommitsArray(BuildCommitsData data) {

        String[] jenkinsCommitsArray = new String[5];
        jenkinsCommitsArray[0] = data.getId();
        jenkinsCommitsArray[1] = data.getCommitAuthor();
        jenkinsCommitsArray[2] = data.getComment();
        jenkinsCommitsArray[3] = data.getChangesetID();
        jenkinsCommitsArray[4] = data.getDate();

        return jenkinsCommitsArray;
    }

    /**
     * Prepare the array to store in BAM
     *
     * @param jenkins Jenkins data
     * @return jenkins data array
     */

    private String[] getJenkinsDataArray(JenkinsConnector jenkins) {

        String[] jenkinsDataArray = new String[8];
        jenkinsDataArray[0] = jenkins.getID();
        jenkinsDataArray[1] = jenkins.getName();
        jenkinsDataArray[2] = jenkins.getName();
        jenkinsDataArray[3] = jenkins.getBuildState();
        jenkinsDataArray[4] = String.valueOf(jenkins.getBuildNumber());
        jenkinsDataArray[5] = String.valueOf(jenkins.getSuccessRate());
        jenkinsDataArray[6] = jenkins.getBuildDate();
        jenkinsDataArray[7] = jenkins.getDuration();

        return jenkinsDataArray;
    }

    /**
     * Prepare the array to store in BAM
     *
     * @param jenkinsEmmaConnector emma data
     * @return emma data array of array list
     */
    private ArrayList<String[]> getJenkinsEmmaDataList(JenkinsEmmaConnector jenkinsEmmaConnector) {

        ArrayList<String[]> jenkinsEmmaDataList = null;
        if (jenkinsEmmaConnector.getReportID() != null) {
            jenkinsEmmaDataList = new ArrayList<String[]>();

            ArrayList<EmmaPackageData> emmaPackageList = jenkinsEmmaConnector.getEmmaPachageData();
            for (EmmaPackageData packageData : emmaPackageList) {
                String jenkinsEmmaDataArray[] = new String[16];
                jenkinsEmmaDataArray[0] = jenkinsEmmaConnector.getReportID();
                jenkinsEmmaDataArray[1] = String.valueOf(jenkinsEmmaConnector.getNumberOfPackages());
                jenkinsEmmaDataArray[2] = String.valueOf(jenkinsEmmaConnector.getNumberOfClasses());
                jenkinsEmmaDataArray[3] = String.valueOf(jenkinsEmmaConnector.getNumberOfMethods());
                jenkinsEmmaDataArray[4] = String.valueOf(jenkinsEmmaConnector.getNumberOfBlocks());
                jenkinsEmmaDataArray[5] = String.valueOf(jenkinsEmmaConnector.getNumberOfLines());
                jenkinsEmmaDataArray[6] = String.valueOf(jenkinsEmmaConnector.getNumberOfFiles());

                jenkinsEmmaDataArray[7] = jenkinsEmmaConnector.getAllClassCoverage();
                jenkinsEmmaDataArray[8] = jenkinsEmmaConnector.getAllMethodCoverage();
                jenkinsEmmaDataArray[9] = jenkinsEmmaConnector.getAllBlockCoverage();
                jenkinsEmmaDataArray[10] = jenkinsEmmaConnector.getAllLineCoverage();

                jenkinsEmmaDataArray[11] = packageData.getPackageName();
                jenkinsEmmaDataArray[12] = packageData.getClassCoverage();
                jenkinsEmmaDataArray[13] = packageData.getMethodCoverage();
                jenkinsEmmaDataArray[14] = packageData.getBlockCoverage();
                jenkinsEmmaDataArray[15] = packageData.getLineCoverage();

                jenkinsEmmaDataList.add(jenkinsEmmaDataArray);
            }
        }
        return jenkinsEmmaDataList;
    }

}
