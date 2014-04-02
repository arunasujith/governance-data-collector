package org.wso2.carbon.data;

/**
 * Created by aruna on 3/24/14.
 */
public class EmmaPackageData {
    private String packageName;
    private String classCoverage;
    private String methodCoverage;
    private String blockCoverage;
    private String lineCoverage;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassCoverage() {
        return classCoverage;
    }

    public void setClassCoverage(String classCoverage) {
        this.classCoverage = classCoverage;
    }

    public String getMethodCoverage() {
        return methodCoverage;
    }

    public void setMethodCoverage(String methodCoverage) {
        this.methodCoverage = methodCoverage;
    }

    public String getLineCoverage() {
        return lineCoverage;
    }

    public void setLineCoverage(String lineCoverage) {
        this.lineCoverage = lineCoverage;
    }

    public String getBlockCoverage() {
        return blockCoverage;
    }

    public void setBlockCoverage(String blockCoverage) {
        this.blockCoverage = blockCoverage;
    }


    public String toString() {

        return packageName + " \n" +
                classCoverage + " \n" +
                methodCoverage + " \n" +
                blockCoverage + " \n" +
                lineCoverage + " \n";
    }
}
