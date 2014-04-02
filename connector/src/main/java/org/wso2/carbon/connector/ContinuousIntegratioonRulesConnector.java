package org.wso2.carbon.connector;

import org.wso2.carbon.data.BuildRuleData;
import org.wso2.carbon.data.BuildRuleStat;

import java.util.ArrayList;

/**
 * Created by aruna on 3/27/14.
 */
public interface ContinuousIntegratioonRulesConnector {

    String getBuildID();
    ArrayList<BuildRuleData> getBuildRuleData();
    BuildRuleStat getBuildRuleStatData();
}
