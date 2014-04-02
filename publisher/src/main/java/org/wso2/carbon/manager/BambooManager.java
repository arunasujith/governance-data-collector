package org.wso2.carbon.manager;


import org.apache.log4j.Logger;
import org.wso2.carbon.data.BuildCommitsData;
import org.wso2.carbon.data.EmmaPackageData;
import org.wso2.carbon.implementation.BambooConnector;
import org.wso2.carbon.implementation.BambooEmmaConnector;
import org.wso2.carbon.publisher.BAMDataPublisher;
import org.wso2.carbon.utils.Configurations;

import java.util.ArrayList;

/**
 * BambooManager.java
 */
public class BambooManager {


    private static Logger log = Logger.getLogger(BambooManager.class);
    public static final String BAMBOO_STREAM = "Bamboo_Stream";  // store bamboo builds data
    public static final String BAMBOO_EMMA_STREAM = "Bamboo_Emma_Stream"; // store emma report data
    public static final String BAMBOO_COMMITS_STREAM = "Bamboo_Commits_Stream"; // store bamboo commits for a particular build
    public static final String VERSION = "1.0.0";

    BAMDataPublisher pub = new BAMDataPublisher();


    /**
     * Extract data from Bamboo api's and save data in BAM
     *
     * @param bambooProjectID bamboo project ID
     */
    public void execute(String bambooProjectID) {

        BambooConnector bamboo = new BambooConnector(Configurations.getWSO2_BAMBOO_URL(), bambooProjectID);
        BambooEmmaConnector bambooEmmaConnector = new BambooEmmaConnector(Configurations.getWSO2_BAMBOO_URL(), bamboo.getName(), bamboo.getBuildNumber());

        ArrayList<BuildCommitsData> bambooCommitsList = bamboo.getCommitsData();

        try {

            pub.buildDataPublish(getBambooDataArray(bamboo),  BAMBOO_STREAM,  VERSION);

            ArrayList<String[]> emmaDataList = getBambooEmmaDataList(bambooEmmaConnector);
            if (emmaDataList != null) {
                for (String[] emmaReport : emmaDataList) {
                    pub.buildEmmaDataPublish(emmaReport,  BAMBOO_EMMA_STREAM, VERSION);
                }
            }

            if (bambooCommitsList != null) {
                for (BuildCommitsData data : bambooCommitsList) {
                    pub.buildCommitsDataPublish(getBambooCommitsArray(data),  BAMBOO_COMMITS_STREAM,  VERSION);
                }
            }

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
     * @param data Bamboo commits data
     * @return bamboo commits data array
     */
    private String[] getBambooCommitsArray(BuildCommitsData data) {

        String[] bambooCommitsArray = new String[5];
        bambooCommitsArray[0] = data.getId();
        bambooCommitsArray[1] = data.getCommitAuthor();
        bambooCommitsArray[2] = data.getComment();
        bambooCommitsArray[3] = data.getChangesetID();
        bambooCommitsArray[4] = data.getDate();

        return bambooCommitsArray;
    }

    /**
     * Prepare the array to store in BAM
     *
     * @param bamboo Bamboo data
     * @return bamboo data array
     */

    private String[] getBambooDataArray(BambooConnector bamboo) {

        String[] bambooDataArray = new String[8];
        bambooDataArray[0] = bamboo.getID();
        bambooDataArray[1] = bamboo.getName();
        bambooDataArray[2] = bamboo.getName();
        bambooDataArray[3] = bamboo.getBuildState();
        bambooDataArray[4] = String.valueOf(bamboo.getBuildNumber());
        bambooDataArray[5] = String.valueOf(bamboo.getSuccessRate());
        bambooDataArray[6] = bamboo.getBuildDate();
        bambooDataArray[7] = bamboo.getDuration();

        return bambooDataArray;
    }

    /**
     * Prepare the array to store in BAM
     *
     * @param bambooEmmaConnector emma data
     * @return emma data array of array list
     */
    private ArrayList<String[]> getBambooEmmaDataList(BambooEmmaConnector bambooEmmaConnector) {

        ArrayList<String[]> bambooEmmaDataList = null;
        if (bambooEmmaConnector.getReportID() != null) {
            bambooEmmaDataList = new ArrayList<String[]>();

            ArrayList<EmmaPackageData> emmaPackageList = bambooEmmaConnector.getEmmaPachageData();
            for (EmmaPackageData packageData : emmaPackageList) {
                String bambooEmmaDataArray[] = new String[16];
                bambooEmmaDataArray[0] = bambooEmmaConnector.getReportID();
                bambooEmmaDataArray[1] = String.valueOf(bambooEmmaConnector.getNumberOfPackages());
                bambooEmmaDataArray[2] = String.valueOf(bambooEmmaConnector.getNumberOfClasses());
                bambooEmmaDataArray[3] = String.valueOf(bambooEmmaConnector.getNumberOfMethods());
                bambooEmmaDataArray[4] = String.valueOf(bambooEmmaConnector.getNumberOfBlocks());
                bambooEmmaDataArray[5] = String.valueOf(bambooEmmaConnector.getNumberOfLines());
                bambooEmmaDataArray[6] = String.valueOf(bambooEmmaConnector.getNumberOfFiles());

                bambooEmmaDataArray[7] = bambooEmmaConnector.getAllClassCoverage();
                bambooEmmaDataArray[8] = bambooEmmaConnector.getAllMethodCoverage();
                bambooEmmaDataArray[9] = bambooEmmaConnector.getAllBlockCoverage();
                bambooEmmaDataArray[10] = bambooEmmaConnector.getAllLineCoverage();

                bambooEmmaDataArray[11] = packageData.getPackageName();
                bambooEmmaDataArray[12] = packageData.getClassCoverage();
                bambooEmmaDataArray[13] = packageData.getMethodCoverage();
                bambooEmmaDataArray[14] = packageData.getBlockCoverage();
                bambooEmmaDataArray[15] = packageData.getLineCoverage();

                bambooEmmaDataList.add(bambooEmmaDataArray);
            }
        }
        return bambooEmmaDataList;
    }
}

