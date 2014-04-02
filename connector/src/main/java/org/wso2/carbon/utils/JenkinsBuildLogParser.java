package org.wso2.carbon.utils;

import org.apache.log4j.Logger;
import org.wso2.carbon.data.BuildRuleData;
import org.wso2.carbon.data.BuildRuleStat;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by aruna on 3/27/14.
 */
public class JenkinsBuildLogParser {

    private static Logger log = Logger.getLogger(JenkinsBuildLogParser.class);

    private ArrayList<BuildRuleData> buildRuleDataArrayList = new ArrayList<BuildRuleData>();
    private BuildRuleStat buildRuleStat = new BuildRuleStat();

    public JenkinsBuildLogParser(){
        parseLogFile();
    }

    public ArrayList<BuildRuleData> getBuildRuleDataList(){
        return  this.buildRuleDataArrayList;
    }

    public BuildRuleStat getBuildRuleStats(){
        return buildRuleStat;
    }

    private void parseLogFile() {

        File buildLogFile = new File("build.log");
        if (buildLogFile.exists()) {
            try {
                FileReader fileReader = new FileReader(buildLogFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line = bufferedReader.readLine();
                boolean buildRuleFlag = false;
                boolean ruleStatFlag = false;
                String componentName = "";
                BuildRuleData buildRuleData;

                while (line != null) {
                    if (line.contains(Constants.RULE_START)) {
                        buildRuleFlag = true;
                        componentName = getComponentName(line);
                    } else if (line.contains(Constants.RULE_END)) {
                        buildRuleFlag = false;
                    }else if(line.contains("RULE EXECUTION STATS")){
                        ruleStatFlag = true;
                    }
                    if (buildRuleFlag) {
                        String array[] = extractRuleExecutionData(line);
                        if (array[0] != null && array[1] != null) {
                            buildRuleData = new BuildRuleData();

                            buildRuleData.setComponentName(componentName);
                            buildRuleData.setRuleName(array[0]);
                            buildRuleData.setStatus(array[1]);

                            buildRuleDataArrayList.add(buildRuleData);
                        }
                    }
                    if(ruleStatFlag){
                        extractRuleStatData(line);
                    }
                    line = bufferedReader.readLine();
                }
            } catch (FileNotFoundException e) {
                log.error("Log File Not Found ", e);
            } catch (IOException e) {
                log.error("Log File Read Exception ", e);
            }
        }
    }

    /**
     * Get the component name
     *
     * @param line String contains the component name
     * @return component name
     */
    private String getComponentName(String line) {
        String[] array = line.split(":");
        return array[1];
    }

    private String[] extractRuleExecutionData(String line) {
        String[] nameStatus = new String[2];
        if (line.contains("Rule Name")) {
            line = line.substring(7);
            String[] nameStatusArray = line.split(",");
            String nameArray[] = nameStatusArray[0].split(":");
            String statusArray[] = nameStatusArray[1].split(":");
            nameStatus[0] = nameArray[1].trim();
            nameStatus[1] = statusArray[1].trim();

        }
        return nameStatus;
    }

    /**
     *
     * @param line
     */

    private void extractRuleStatData(String line){
        if(line.contains("Total Rule Count")){
            String [] statData = line.split(":");
            buildRuleStat.setTotalRuleCount(Integer.parseInt(statData[1].trim()));
        }else if(line.contains("Executed Rule Count")){
            String [] statData = line.split(":");
            buildRuleStat.setExecutedRuleCount(Integer.parseInt(statData[1].trim()));
        }else if(line.contains("Inactive Rule Count")){
            String [] statData = line.split(":");
            buildRuleStat.setInactiveRuleCount(Integer.parseInt(statData[1].trim()));
        }else if(line.contains("Excluded Rule Count")){
            String [] statData = line.split(":");
            buildRuleStat.setExcludedRuleCount(Integer.parseInt(statData[1].trim()));
        }else if(line.contains("Maven Version Incompatible Count")){
            String [] statData = line.split(":");
            buildRuleStat.setMavenVersionIncompatibleRuleCount(Integer.parseInt(statData[1].trim()));
        }else if(line.contains("Skipped Explicit Rule Count")){
            String [] statData = line.split(":");
            buildRuleStat.setSkippedExplicitRuleCount(Integer.parseInt(statData[1].trim()));
        }

    }

//    public static void main(String[] args) {
//        JenkinsBuildLogParser p = new JenkinsBuildLogParser();
//        ArrayList<BuildRuleData> list = p.getBuildRuleDataList();
//        for (BuildRuleData data : list)
//            System.out.println(data.toString());
//
//        System.out.println(p.getBuildRuleStats().toString());
//    }
}
