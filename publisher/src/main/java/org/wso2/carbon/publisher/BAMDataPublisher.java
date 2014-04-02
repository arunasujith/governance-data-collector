package org.wso2.carbon.publisher;

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;
import org.wso2.carbon.utils.Configurations;
import org.wso2.carbon.utils.StringUtility;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketException;


/**
 * BAMDataPublisher.java
 */
public class BAMDataPublisher {
    private Logger log = Logger.getLogger(BAMDataPublisher.class);

    private DataPublisher dataPublisher = null;

    public BAMDataPublisher(){
        initDataPublisher();
    }

    /**
     * Initialize the BAM data publisher
     */

    private void initDataPublisher() {

        try {
            log.info("Starting BAM  client\n");
            AgentConfiguration agentConfiguration = new AgentConfiguration();

            System.setProperty("javax.net.ssl.trustStore", Configurations.getBAM_HOME()
                    + "/repository/resources/security/client-truststore.jks");

            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            Agent agent = new Agent(agentConfiguration);
            String host;

            host = Configurations.getBAM_HOST_NAME();

            String port = Configurations.getBAM_PORT();
            String username = Configurations.getBAM_USERNAME();
            String password = Configurations.getBAM_PASSWORD();

            this.dataPublisher = new DataPublisher("tcp://" + host  + ":" + port,
                    username, password, agent);

        } catch (AgentException e) {
            log.error("Exception", e);

        } catch (MalformedURLException e) {
            log.error("Exception", e);

        } catch (org.wso2.carbon.databridge.commons.exception.AuthenticationException e) {
            log.error("Exception", e);

        } catch (TransportException e) {
            log.error("Exception", e);

        }

    }

    /**
     * @param dataArray
     * @param streamName
     * @param version
     * @throws org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException
     * @throws java.net.SocketException
     * @throws java.io.UnsupportedEncodingException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     */
    public void repositoryDataPublish(String[] dataArray, String streamName, String version) throws
            NoStreamDefinitionExistException,
            SocketException,
            UnsupportedEncodingException,
            AgentException,
            MalformedStreamDefinitionException,
            StreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException {

        String streamId = null;

        try {

            streamId = this.dataPublisher.findStream(streamName, version);

        } catch (NoStreamDefinitionExistException e) {

            String[] fields = {"repositoryID", "ownerID", "totalNumberOfCommits", "topContributor",
                    "totalPullRequests", "lastCommitDateTime", "commitsThisMonth", "commitLastYear"};

            String[] types = {"STRING", "STRING", "STRING", "STRING",
                    "STRING", "STRING", "STRING", "STRING"};

            streamId = dataPublisher.defineStream(StringUtility.getTableString(fields, types, streamName,
                    version, "Repository Data", "Repository Statistics Data"));

        } catch (Exception ex) {
            log.error("***Exception****" + ex);
        }

        // Publish event for a valid stream
        if (!streamId.isEmpty()) {
            log.info("Stream ID: " + streamId + "\n");
            publishEvents(dataPublisher, streamId, dataArray);

        }

    }


    /**
     * @param dataArray
     * @param streamName
     * @param version
     * @throws org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException
     * @throws java.net.SocketException
     * @throws java.io.UnsupportedEncodingException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     */
    public void repositoryCommittersDataPublich(String[] dataArray, String streamName, String version) throws
            NoStreamDefinitionExistException,
            SocketException,
            UnsupportedEncodingException,
            AgentException,
            MalformedStreamDefinitionException,
            StreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException {

        String streamId = null;

        try {

            streamId = this.dataPublisher.findStream(streamName, version);

        } catch (NoStreamDefinitionExistException e) {
            String[] fields = {"repositoryID", "committerName", "numberofCommits"};

            String[] types = {"STRING", "STRING", "STRING"};

            streamId = dataPublisher.defineStream(StringUtility.getTableString(fields, types, streamName,
                    version, "Repository Committers Data", "Repository Committers Data"));

        } catch (Exception ex) {
            log.error("***Exception****" + ex);
        }

        // Publish event for a valid stream
        if (!streamId.isEmpty()) {
            log.info("Stream ID: " + streamId + "\n");
            publishEvents(dataPublisher, streamId, dataArray);

        }

    }

    /**
     * @param dataArray
     * @param streamName
     * @param version
     * @throws org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException
     * @throws java.net.SocketException
     * @throws java.io.UnsupportedEncodingException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     */
    public void repositoryCommitsDataPublish(String[] dataArray, String streamName, String version) throws
            NoStreamDefinitionExistException,
            SocketException,
            UnsupportedEncodingException,
            AgentException,
            MalformedStreamDefinitionException,
            StreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException {

        String streamId = null;

        try {

            streamId = this.dataPublisher.findStream(streamName, version);

        } catch (NoStreamDefinitionExistException e) {
            String[] fields = {"repositoryID", "shaValue", "authorName", "authorEMail",
                    "authorDate", "commitMessage"};

            String[] types = {"STRING", "STRING", "STRING", "STRING",
                    "STRING", "STRING"};

            streamId = dataPublisher.defineStream(StringUtility.getTableString(fields, types, streamName,
                    version, "Repository Commits Data", "Repository Latest Commits Data"));


        } catch (Exception ex) {
            log.error("***Exception****" + ex);
        }

        // Publish event for a valid stream
        if (!streamId.isEmpty()) {
            log.info("Stream ID: " + streamId + "\n");
            publishEvents(dataPublisher, streamId, dataArray);

        }

    }

    /**
     * @param dataArray
     * @param streamName
     * @param version
     * @throws org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException
     * @throws java.net.SocketException
     * @throws java.io.UnsupportedEncodingException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     */
    public void buildCommitsDataPublish(String[] dataArray, String streamName, String version) throws
            NoStreamDefinitionExistException,
            SocketException,
            UnsupportedEncodingException,
            AgentException,
            MalformedStreamDefinitionException,
            StreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException {

        String streamId = null;

        try {
            streamId = this.dataPublisher.findStream(streamName, version);

        } catch (NoStreamDefinitionExistException e) {
            String[] fields = {"planID", "commitAuthor", "comment", "changesetID",
                    "date"};

            String[] types = {"STRING", "STRING", "STRING", "STRING",
                    "STRING"};

            streamId = dataPublisher.defineStream(StringUtility.getTableString(fields, types, streamName,
                    version, "Repository Commits Data", "Repository Commits Data for the build Data"));

        } catch (Exception ex) {
            log.error("***Exception****" + ex);
        }

        // Publish event for a valid stream
        if (!streamId.isEmpty()) {
            log.info("Stream ID: " + streamId + "\n");
            publishEvents(dataPublisher, streamId, dataArray);

        }

    }


    /**
     * @param dataArray
     * @param streamName
     * @param version
     * @throws org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException
     * @throws java.net.SocketException
     * @throws java.io.UnsupportedEncodingException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     */
    public void buildRulesDataPublish(String[] dataArray, String streamName, String version) throws
            NoStreamDefinitionExistException,
            SocketException,
            UnsupportedEncodingException,
            AgentException,
            MalformedStreamDefinitionException,
            StreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException {

        String streamId = null;

        try {
            streamId = this.dataPublisher.findStream(streamName, version);

        } catch (NoStreamDefinitionExistException e) {
            String[] fields = {"buildID", "componentName", "ruleName", "status"};

            String[] types = {"STRING", "STRING", "STRING", "STRING"};

            streamId = dataPublisher.defineStream(StringUtility.getTableString(fields, types, streamName,
                    version, "Build Rule Data", "Build rule data for a build"));

        } catch (Exception ex) {
            log.error("***Exception****" + ex);
        }

        // Publish event for a valid stream
        if (!streamId.isEmpty()) {
            log.info("Stream ID: " + streamId + "\n");
            publishEvents(dataPublisher, streamId, dataArray);

        }

    }

    /**
     * @param dataArray
     * @param streamName
     * @param version
     * @throws org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException
     * @throws java.net.SocketException
     * @throws java.io.UnsupportedEncodingException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     */
    public void buildRulesStatDataPublish(String[] dataArray, String streamName, String version) throws
            NoStreamDefinitionExistException,
            SocketException,
            UnsupportedEncodingException,
            AgentException,
            MalformedStreamDefinitionException,
            StreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException {

        String streamId = null;

        try {
            streamId = this.dataPublisher.findStream(streamName, version);

        } catch (NoStreamDefinitionExistException e) {
            String[] fields = {"buildID", "totalRuleCount", "executedRuleCount", "inactiveRuleCount",
            "excludedRuleCount" ,"mavenVersionIncompatibleRuleCount" ,"skippedExplicitRuleCount"};

            String[] types = {"STRING", "STRING", "STRING", "STRING" , "STRING", "STRING", "STRING"};

            streamId = dataPublisher.defineStream(StringUtility.getTableString(fields, types, streamName,
                    version, "Build Rule Data", "Build rule data for a build"));

        } catch (Exception ex) {
            log.error("***Exception****" + ex);
        }

        // Publish event for a valid stream
        if (!streamId.isEmpty()) {
            log.info("Stream ID: " + streamId + "\n");
            publishEvents(dataPublisher, streamId, dataArray);

        }

    }


    /**
     * @param dataArray
     * @param streamName
     * @param version
     * @throws org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException
     * @throws java.net.SocketException
     * @throws java.io.UnsupportedEncodingException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     */

    public void buildEmmaDataPublish(String[] dataArray, String streamName, String version) throws
            NoStreamDefinitionExistException,
            SocketException,
            UnsupportedEncodingException,
            AgentException,
            MalformedStreamDefinitionException,
            StreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException {

        String streamId = null;

        try {
            streamId = this.dataPublisher.findStream(streamName, version);

        } catch (NoStreamDefinitionExistException e) {

            String[] fields = {"reportID", "numberOfPackages", "numberOfClasses", "numberOfMethods",
                    "numberOfBlocks", "numberOfLines", "numberOfFiles",
                    "allClassCoverage", "allMethodCoverage", "allBlockCoverage", "allLineCoverage",
                    "packageName", "classCoverage", "methodCoverage", "blockCoverage", "lineCoverage"};

            String[] types = {"STRING", "STRING", "STRING", "STRING",
                    "STRING", "STRING", "STRING", "STRING", "STRING", "STRING",
                    "STRING", "STRING", "STRING", "STRING", "STRING", "STRING"};

            streamId = dataPublisher.defineStream(StringUtility.getTableString(fields, types, streamName,
                    version, "Build Emma Data", "Build Emma Report Data"));

        } catch (Exception ex) {
            log.info("***Exception****" + ex);
        }

        // Publish event for a valid stream
        if (!streamId.isEmpty()) {
            log.info("Stream ID: " + streamId + "\n");
            publishEvents(dataPublisher, streamId, dataArray);
        }

    }

    /**
     * @param dataArray
     * @param streamName
     * @param version
     * @throws org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException
     * @throws java.net.SocketException
     * @throws java.io.UnsupportedEncodingException
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     * @throws org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.StreamDefinitionException
     * @throws org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException
     */

    public void buildDataPublish(String[] dataArray, String streamName, String version) throws
            NoStreamDefinitionExistException,
            SocketException,
            UnsupportedEncodingException,
            AgentException,
            MalformedStreamDefinitionException,
            StreamDefinitionException,
            DifferentStreamDefinitionAlreadyDefinedException {

        String streamId = null;

        try {
            streamId = this.dataPublisher.findStream(streamName, version);

        } catch (NoStreamDefinitionExistException e) {
            String[] fields = {"projectID", "planID", "planName", "buildState",
                    "buildNumber", "successRate", "buildDate", "relativeBuildDate"};

            String[] types = {"STRING", "STRING", "STRING", "STRING",
                    "STRING", "STRING", "STRING", "STRING"};

            streamId = dataPublisher.defineStream(StringUtility.getTableString(fields, types, streamName,
                    version, "Build Data", "Build Statistics Data"));

        } catch (Exception ex) {
            log.info("***Exception****" + ex);
        }

        // Publish event for a valid stream
        if (!streamId.isEmpty()) {
            log.info("Stream ID: " + streamId + "\n");
            publishEvents(dataPublisher, streamId, dataArray);

        }

    }

    /**
     * @param dataPublisher
     * @param streamId
     * @param dataArray
     * @throws org.wso2.carbon.databridge.agent.thrift.exception.AgentException
     */
    private void publishEvents(DataPublisher dataPublisher, String streamId,
                               String dataArray[]) throws AgentException {

        Object[] objectArray = new Object[dataArray.length];
        for (int i = 0; i < dataArray.length; i++) {
            objectArray[i] = dataArray[i];
        }

        Event event = new Event(streamId, System.currentTimeMillis(), null,
                null, objectArray);

        this.dataPublisher.publish(event);

    }

    /**
     * Stop the publisher
     */
    public void stopPublisher(){
        log.info("Stopping Data Publisher");
        this.dataPublisher.stop();
    }

}
