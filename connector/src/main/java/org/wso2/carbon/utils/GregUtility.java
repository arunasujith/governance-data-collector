package org.wso2.carbon.utils;

import java.net.URL;

/**
 * Created by aruna on 3/24/14.
 */
public class GregUtility {
    /**
     * Get Git repository name by the url
     *
     * @param repoURL url of the repository
     * @return
     */
    public static String getGitRepoName(String repoURL) {
        String repoName = null;

        try {
            URL u = new URL(repoURL);
            String[] array = repoURL.split("/");
            repoName = array[array.length - 1];
        } catch (Exception e) {

        }

        return repoName;

    }

    /**
     * Get Git repository owner name by the url
     *
     * @param repoURL url of the repository
     * @return
     */
    public static String getGitRepoOwner(String repoURL) {
        String repoOwner = null;
        try {
            URL u = new URL(repoURL);
            String[] array = repoURL.split("/");
            repoOwner = array[array.length - 2];
        } catch (Exception e) {

        }

        return repoOwner;

    }

    /**
     * Get bamboo project key by the bamboo url
     *
     * @param bambooURL url to bamboo project
     * @return bamboo project key
     */
    public static String getBambooPorjectKey(String bambooURL) {
        String bambooProjectKey = null;
            try {

                if(bambooURL.substring(bambooURL.length()-1 , bambooURL.length()).equals("/")){
                    bambooURL = bambooURL.substring(0 , bambooURL.length() -1);
                    //System.out.println(bambooURL);
                }
                String[] array = bambooURL.split("/");
                bambooProjectKey = array[array.length - 1];
            } catch (Exception e) {

            }

        return bambooProjectKey;

    }

    /**
     * Get bamboo project key by the bamboo url
     *
     * @param jenkinsURL url to bamboo project
     * @return bamboo project key
     */
    public static String getJenkinsPorjectKey(String jenkinsURL) {
        String jenkinsProjectKey = null;
            try {
                URL u = new URL(jenkinsURL);
                if(jenkinsURL.substring(jenkinsURL.length()-1 , jenkinsURL.length()).equals("/")){
                    jenkinsURL = jenkinsURL.substring(0 , jenkinsURL.length() -1);
                    //System.out.println(jenkinsURL);
                }
                String[] array = jenkinsURL.split("/");
                jenkinsProjectKey = array[array.length - 1];
            } catch (Exception e) {

            }
        return jenkinsProjectKey;

    }

//    public static void main(String[] args) {
//        System.out.println(getJenkinsPorjectKey("http://ec2-54-83-33-151.compute-1.amazonaws.com:8080/job/C5-carbon-kernel-rule-validation/"));
//        System.out.println(getBambooPorjectKey("https://wso2.org/bamboo/browse/WSO2CARBON/"));
//    }
}
