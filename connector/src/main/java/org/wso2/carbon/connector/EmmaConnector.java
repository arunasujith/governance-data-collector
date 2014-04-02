package org.wso2.carbon.connector;

import org.wso2.carbon.data.EmmaPackageData;

import java.util.ArrayList;

/**
 * Created by aruna on 3/24/14.
 */
public interface EmmaConnector {

    String getReportID();

    int getNumberOfPackages();

    int getNumberOfClasses();

    int getNumberOfMethods();

    int getNumberOfBlocks();

    int getNumberOfLines();

    int getNumberOfFiles();

      String getAllClassCoverage();
      String getAllMethodCoverage();
      String getAllBlockCoverage();
      String getAllLineCoverage();

      ArrayList<EmmaPackageData> getEmmaPachageData();

}
