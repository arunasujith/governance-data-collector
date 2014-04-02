package org.wso2.carbon.data;

import java.util.ArrayList;

/**
 * Created by aruna on 3/24/14.
 */
public class EmmaData {
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

    public void addToEmmaPackageList(EmmaPackageData data) {
        list.add(data);
    }

    public void setList(ArrayList<EmmaPackageData> list) {
        this.list = list;
    }

    public String getReportID() {
        return reportID;
    }

    public void setReportID(String reportID) {
        this.reportID = reportID;
    }

    public int getNumberOfPackages() {
        return numberOfPackages;
    }

    public void setNumberOfPackages(int numberOfPackages) {
        this.numberOfPackages = numberOfPackages;
    }

    public int getNumberOfClasses() {
        return numberOfClasses;
    }

    public void setNumberOfClasses(int numberOfClasses) {
        this.numberOfClasses = numberOfClasses;
    }

    public int getNumberOfMethods() {
        return numberOfMethods;
    }

    public void setNumberOfMethods(int numberOfMethods) {
        this.numberOfMethods = numberOfMethods;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public void setNumberOfFiles(int numberOfFiles) {
        this.numberOfFiles = numberOfFiles;
    }

    public int getNumberOfBlocks() {
        return numberOfBlocks;
    }

    public void setNumberOfBlocks(int numberOfBlocks) {
        this.numberOfBlocks = numberOfBlocks;
    }

    public int getNumberOfLines() {
        return numberOfLines;
    }

    public void setNumberOfLines(int numberOfLines) {
        this.numberOfLines = numberOfLines;
    }

    public String getAllClassCoverage() {
        return allClassCoverage;
    }

    public void setAllClassCoverage(String allClassCoverage) {
        this.allClassCoverage = allClassCoverage;
    }

    public String getAllMethodCoverage() {
        return allMethodCoverage;
    }

    public void setAllMethodCoverage(String allMethodCoverage) {
        this.allMethodCoverage = allMethodCoverage;
    }

    public String getAllBlockCoverage() {
        return allBlockCoverage;
    }

    public void setAllBlockCoverage(String allBlockCoverage) {
        this.allBlockCoverage = allBlockCoverage;
    }

    public String getAllLineCoverage() {
        return allLineCoverage;
    }

    public void setAllLineCoverage(String allLineCoverage) {
        this.allLineCoverage = allLineCoverage;
    }

    public ArrayList<EmmaPackageData> getList() {
        return list;
    }

    public String toString() {
        return reportID + numberOfPackages + " " +
                numberOfClasses + " " +
                numberOfMethods + " " +
                numberOfBlocks + " " +
                numberOfLines + " " +
                numberOfFiles + " " +

                allClassCoverage + " " +
                allMethodCoverage + " " +
                allBlockCoverage + " " +
                allLineCoverage + " "+
                list.toString();
    }
}
