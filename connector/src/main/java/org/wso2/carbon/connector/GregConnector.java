package org.wso2.carbon.connector;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.data.GRegData;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.Constants;
import org.wso2.carbon.utils.GregUtility;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;


public class GregConnector {

    private static Logger log = Logger.getLogger(GregConnector.class);
    private String gregHome;
    private String gregURL;
    private String gregUserName;
    private String gregPassword;


    public GregConnector(String gregHome, String gregURl, String gregUserName, String gregPassword) {
        this.gregHome = gregHome;
        this.gregURL = gregURl;
        this.gregUserName = gregUserName;
        this.gregPassword = gregPassword;
    }

    /**
     * Initialize the Greg registry
     *
     * @return
     */
    public Registry init() {

        Registry governanceRegistry = null;
        try {
            System.setProperty("carbon.repo.write.mode", "true");
            System.setProperty("javax.net.ssl.trustStore", gregHome
                    + "/repository/resources/security/client-truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");

            Registry rootRegistry = new RemoteRegistry(gregURL, gregUserName, gregPassword);
            governanceRegistry = GovernanceUtils.getGovernanceUserRegistry(rootRegistry, gregUserName);
        } catch (RegistryException e) {
            log.error("Registry Exception ", e);
        } catch (MalformedURLException e) {
            log.error("Malformed URL Exceprion ", e);
        }
        return governanceRegistry;
    }

    /**
     * Get the GregData Array List
     *
     * @param governanceRegistry Registry object
     * @return GregData Array List
     */
    public ArrayList<GRegData> getProjectsData(Registry governanceRegistry) {

        ArrayList<GRegData> gregDataList = new ArrayList<GRegData>();
        try {
            if (governanceRegistry.resourceExists(Constants.GREG_RESOURCE_PATH)) {
                Collection projectsCollection = (Collection) governanceRegistry.get(Constants.GREG_RESOURCE_PATH);
                String[] child = projectsCollection.getChildren();
                for (int i = 0; i < child.length; i++) {
                    Collection collection = (Collection) governanceRegistry.get(child[i]);
                    String[] ch = collection.getChildren();
                    for (int j = 0; j < ch.length; j++) {
                        Resource r = governanceRegistry.get(ch[j]);
                        try {
                            byte[] contentBytes = (byte[]) r.getContent();
                            String content = new String(contentBytes);
                            gregDataList.add(parseXMLString(content));
                        } catch (ClassCastException e) {
                            log.error("GREG Class cast Exception", e);
                        }

                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Registry Exception ", e);
        }
        return gregDataList;
    }

    /**
     * Parse the greg data xml string
     *
     * @param xmlString greg xml string
     * @return GReg Data Object
     */
    private GRegData parseXMLString(String xmlString) {
        GRegData gRegData = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlString));
            Document document = builder.parse(is);

            document.getDocumentElement().normalize();

            //read the project data
            NodeList otherNodeList = document.getElementsByTagName(Constants.OTHER);

            Node nNode = otherNodeList.item(0);
            String gitRepoURL = null, buildUrl = null, buildType = null;
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) nNode;
                gitRepoURL = eElement.getElementsByTagName(Constants.VERSION_CONTROL).item(0).getTextContent();
                buildUrl = eElement.getElementsByTagName(Constants.CONTINUOUS_INTEGRATION).item(0).getTextContent();
                //buildType = eElement.getElementsByTagName(Constants.CONTINUOUS_INTEGRATION_TYPE).item(0).getTextContent();
                buildType = "jenkins";
            }

            gRegData = new GRegData();
            gRegData.setGithubOwnerName(GregUtility.getGitRepoOwner(gitRepoURL));
            gRegData.setGithubRepoName(GregUtility.getGitRepoName(gitRepoURL));
            if (buildType.equalsIgnoreCase("bamboo")) {
                gRegData.setBambooName(GregUtility.getBambooPorjectKey(buildUrl));
                log.info("Bamboo URL ADDDED :: " + buildUrl);
            } else if (buildType.equalsIgnoreCase("jenkins")) {
                gRegData.setJenkinsName((GregUtility.getJenkinsPorjectKey(buildUrl)));
                log.info("Jenkins URL ADDDED :: " + buildUrl);
            }


        } catch (ParserConfigurationException e) {
            log.error("Parser Configuration Exception ", e);
        } catch (SAXException e) {
            log.error("SAX Exception ", e);
        } catch (IOException e) {
            log.error("IOException ", e);
        }
        return gRegData;
    }

}

