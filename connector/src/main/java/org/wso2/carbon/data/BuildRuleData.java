package org.wso2.carbon.data;

/**
 * Created by aruna on 3/27/14.
 */
public class BuildRuleData {

    String componentName;
    String ruleName;
    String status;


    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toString() {
        return componentName + "  " +
                ruleName + "  " +
                status;
    }
}
