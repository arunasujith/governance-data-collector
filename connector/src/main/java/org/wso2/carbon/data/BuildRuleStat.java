package org.wso2.carbon.data;

/**
 * Created by aruna on 3/28/14.
 */
public class BuildRuleStat {

    int totalRuleCount;
    int executedRuleCount;
    int inactiveRuleCount;
    int excludedRuleCount;
    int mavenVersionIncompatibleRuleCount;
    int skippedExplicitRuleCount;


    public int getTotalRuleCount() {
        return totalRuleCount;
    }

    public void setTotalRuleCount(int totalRuleCount) {
        this.totalRuleCount = totalRuleCount;
    }

    public int getExecutedRuleCount() {
        return executedRuleCount;
    }

    public void setExecutedRuleCount(int executedRuleCount) {
        this.executedRuleCount = executedRuleCount;
    }

    public int getInactiveRuleCount() {
        return inactiveRuleCount;
    }

    public void setInactiveRuleCount(int inactiveRuleCount) {
        this.inactiveRuleCount = inactiveRuleCount;
    }

    public int getExcludedRuleCount() {
        return excludedRuleCount;
    }

    public void setExcludedRuleCount(int excludedRuleCount) {
        this.excludedRuleCount = excludedRuleCount;
    }

    public int getMavenVersionIncompatibleRuleCount() {
        return mavenVersionIncompatibleRuleCount;
    }

    public void setMavenVersionIncompatibleRuleCount(int mavenVersionIncompatibleRuleCount) {
        this.mavenVersionIncompatibleRuleCount = mavenVersionIncompatibleRuleCount;
    }

    public int getSkippedExplicitRuleCount() {
        return skippedExplicitRuleCount;
    }

    public void setSkippedExplicitRuleCount(int skippedExplicitRuleCount) {
        this.skippedExplicitRuleCount = skippedExplicitRuleCount;
    }

    public String toString() {
        return totalRuleCount + "  " +
                executedRuleCount + "  " +
                inactiveRuleCount + "  " +
                excludedRuleCount + "  " +
                mavenVersionIncompatibleRuleCount + "  " +
                skippedExplicitRuleCount;

    }
}
