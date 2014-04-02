package org.wso2.carbon.implementation;

import org.apache.log4j.Logger;
import org.wso2.carbon.connector.EmmaConnector;
import org.wso2.carbon.connector.HttpConnector;
import org.wso2.carbon.data.EmmaData;
import org.wso2.carbon.data.EmmaPackageData;
import org.wso2.carbon.utils.Constants;
import org.wso2.carbon.utils.EmmaXMLParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by aruna on 3/24/14.
 */
public class BambooEmmaConnector implements EmmaConnector {


    private static Logger log = Logger.getLogger(BambooEmmaConnector.class);

    private String bambooUrl;
    private String bambooBuildID;
    private String bambooBuildNumber;

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


    public BambooEmmaConnector(String bambooUrl, String bambooBuildID, String bambooBuildNumber) {
        this.bambooUrl = bambooUrl;
        this.bambooBuildID = bambooBuildID;
        this.bambooBuildNumber = bambooBuildNumber;

        fetchData();

    }

    private void fetchData() {


        if (downloadEmmaReport()) {
            EmmaData emmaData = parseReport(bambooBuildID+"-"+bambooBuildNumber);

            System.out.println(emmaData.toString());
            this.reportID = emmaData.getReportID();
            this.numberOfPackages = emmaData.getNumberOfPackages();
            this.numberOfClasses = emmaData.getNumberOfClasses();
            this.numberOfMethods = emmaData.getNumberOfMethods();
            this.numberOfBlocks = emmaData.getNumberOfBlocks();
            this.numberOfLines = emmaData.getNumberOfLines();
            this.numberOfFiles = emmaData.getNumberOfFiles();

            this.allClassCoverage = emmaData.getAllClassCoverage();
            this.allMethodCoverage = emmaData.getAllMethodCoverage();
            this.allBlockCoverage = emmaData.getAllBlockCoverage();
            this.allLineCoverage = emmaData.getAllLineCoverage();

            this.list = emmaData.getList();
        }

    }

    private String getBambooEmmaURL() {
        //http://localhost:8085/artifact/CAR001-KER/shared/build-8/artifact_emma/coverage.xml
        //return "http://localhost:8085/artifact/CAR001-KER/shared/build-8/artifact_emma/coverage.xml";

        String url = this.bambooUrl + "/artifact/" + this.bambooBuildID + "/shared/build-" + this.bambooBuildNumber + "/artifact_emma/coverage.xml";

        return url;
    }


    /**
     * Download the emma report file
     *
     * @return success or failure
     */
    private boolean downloadEmmaReport() {
        HttpConnector httpConnector = new HttpConnector();
        InputStream inputStream = httpConnector.getHttpData(getBambooEmmaURL());
        FileOutputStream fileOutputStream = null;
        boolean downloadFlag = false;

        try {
            if (inputStream != null) {
                File file = new File(Constants.EMMA_FILE_NAME);
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
                File file = new File(Constants.EMMA_FILE_NAME);
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
     * Parse the emma report xml file and generate the EmmaReport
     *
     * @return EmmaData bean
     */
    private EmmaData parseReport(String reportID) {

        EmmaXMLParser parser = new EmmaXMLParser(Constants.EMMA_FILE_NAME);
        EmmaData data = parser.parse(reportID);

        return data;
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


}
