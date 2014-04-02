package org.wso2.carbon.connector;

import org.wso2.carbon.data.BuildCommitsData;

import java.util.ArrayList;

/**
 * Created by aruna on 3/21/14.
 */
public interface ContinuousIntegrationConnector {

    String getName();

    String getID();

    String getBuildState();

    String getBuildNumber();

    String getSuccessRate();

    String getBuildDate();

    String getDuration();

    ArrayList<BuildCommitsData> getCommitsData();

}
