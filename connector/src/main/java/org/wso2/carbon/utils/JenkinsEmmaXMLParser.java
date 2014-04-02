package org.wso2.carbon.utils;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.data.EmmaData;
import org.wso2.carbon.data.EmmaPackageData;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by aruna on 3/26/14.
 */
public class JenkinsEmmaXMLParser {


    private File xmlFile;
    private EmmaData emmaData = new EmmaData();
    private static Logger log = Logger.getLogger(EmmaXMLParser.class);

    public JenkinsEmmaXMLParser(String xmlFileName) {
        xmlFile = new File(xmlFileName);
    }

    /**
     * Parse the xml file and return EmmaData
     *
     * @return EmmaData bean
     */

    public EmmaData parse() {

        if (this.xmlFile.exists()) {

            try {
                Document document = getDocument();
                parseXmlFile(document);
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                log.error("Exception ", e);
            } catch (SAXException e) {
                e.printStackTrace();
                log.error("Exception ", e);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("Exception ", e);
            }
        }
        // emmaData.setReportID(reportID);
        return emmaData;
    }

    private Document getDocument() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        dbf.setFeature("http://xml.org/sax/features/namespaces", false);
        dbf.setFeature("http://xml.org/sax/features/validation", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.parse(xmlFile);
    }

    /**
     * Read XML Document
     *
     * @param document document to be parsed
     */
    private void parseXmlFile(Document document) {

        document.getDocumentElement().normalize();
        NodeList statsNodeList = document.getElementsByTagName("report");
        Node  e = statsNodeList.item(0);
        if (e.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) e;
            System.out.println(eElement.getAttribute("name"));
        }

        NodeList rootChildNodeList = e.getChildNodes();
        for (int j = 0; j < rootChildNodeList.getLength(); j++) {
            Node coverage = rootChildNodeList.item(j);
            if (coverage.getNodeName().equals("counter")) {
                if (coverage.getNodeName().equals("counter")) {
                    if (coverage.getNodeType() == Node.ELEMENT_NODE) {
                        if (((Element) coverage).getAttribute(Constants.TYPE).equals("CLASS")) {
                            emmaData.setAllClassCoverage(getCoverPercentage(((Element) coverage).getAttribute("missed"),
                                    ((Element) coverage).getAttribute("covered")));
                            emmaData.setNumberOfClasses(getTotal(((Element) coverage).getAttribute("missed"),
                                    ((Element) coverage).getAttribute("covered")));
                        } else if (((Element) coverage).getAttribute(Constants.TYPE).equals("METHOD")) {
                            emmaData.setAllMethodCoverage(getCoverPercentage(((Element) coverage).getAttribute("missed"),
                                    ((Element) coverage).getAttribute("covered")));
                            emmaData.setNumberOfMethods(getTotal(((Element) coverage).getAttribute("missed"),
                                    ((Element) coverage).getAttribute("covered")));
                        } else if (((Element) coverage).getAttribute(Constants.TYPE).equals("BRANCH")) {
                            emmaData.setAllBlockCoverage(getCoverPercentage(((Element) coverage).getAttribute("missed"),
                                    ((Element) coverage).getAttribute("covered")));
                            emmaData.setNumberOfBlocks(getTotal(((Element) coverage).getAttribute("missed"),
                                    ((Element) coverage).getAttribute("covered")));
                        } else if (((Element) coverage).getAttribute(Constants.TYPE).equals("LINE")) {
                            emmaData.setAllLineCoverage(getCoverPercentage(((Element) coverage).getAttribute("missed"),
                                    ((Element) coverage).getAttribute("covered")));
                            emmaData.setNumberOfLines(getTotal(((Element) coverage).getAttribute("missed"),
                                    ((Element) coverage).getAttribute("covered")));
                        }

                    }
                }
            }
        }
        //read all packages
        NodeList packageNodeList = document.getElementsByTagName(Constants.PACKAGE);
        emmaData.setNumberOfPackages(packageNodeList.getLength());

        for (int i = 0; i < packageNodeList.getLength(); i++) {
            Node packageNode = packageNodeList.item(i);
            if (packageNode.getNodeType() == Node.ELEMENT_NODE) {
                EmmaPackageData emmaPackageData = new EmmaPackageData();

                Element eElement = (Element) packageNode;
                emmaPackageData.setPackageName(eElement.getAttribute(Constants.NAME));

                NodeList packageChildList = eElement.getChildNodes();
                for (int j = 0; j < packageChildList.getLength(); j++) {
                    Node coverage = packageChildList.item(j);
                    //  String parent = coverage.getParentNode().getNodeName();
                    if (coverage.getNodeName().equals("counter")) {
                        if (coverage.getNodeType() == Node.ELEMENT_NODE) {
                            if (((Element) coverage).getAttribute(Constants.TYPE).equals("CLASS")) {
                                emmaPackageData.setClassCoverage(getCoverPercentage(((Element) coverage).getAttribute("missed"),
                                        ((Element) coverage).getAttribute("covered")));
                            } else if (((Element) coverage).getAttribute(Constants.TYPE).equals("METHOD")) {
                                emmaPackageData.setMethodCoverage(getCoverPercentage(((Element) coverage).getAttribute("missed"),
                                        ((Element) coverage).getAttribute("covered")));
                            } else if (((Element) coverage).getAttribute(Constants.TYPE).equals("BRANCH")) {
                                emmaPackageData.setBlockCoverage(getCoverPercentage(((Element) coverage).getAttribute("missed"),
                                        ((Element) coverage).getAttribute("covered")));
                            } else if (((Element) coverage).getAttribute(Constants.TYPE).equals("LINE")) {
                                emmaPackageData.setLineCoverage(getCoverPercentage(((Element) coverage).getAttribute("missed"),
                                        ((Element) coverage).getAttribute("covered")));
                            }

                        }
                    }
                }
                emmaData.addToEmmaPackageList(emmaPackageData);
            }
        }
    }

    private static String getCoverPercentage(String missed, String covered) {
        double missedValue = Double.parseDouble(missed);
        double coveredvalue = Double.parseDouble(covered);
        double coverrate = (coveredvalue / (coveredvalue + missedValue)) * 100;
        DecimalFormat df2 = new DecimalFormat("###.##");
        coverrate = Double.valueOf(df2.format(coverrate));
        return String.valueOf(coverrate);

    }

    private static int getTotal(String missed, String covered) {
        int missedValue = Integer.parseInt(missed);
        int coveredvalue = Integer.parseInt(covered);

        return missedValue + coveredvalue;

    }

//    public static void main(String[] args) {
//        JenkinsEmmaXMLParser p = new JenkinsEmmaXMLParser("jacoco.xml");
//        EmmaData d = p.parse();
//        System.out.println(d.toString());
//    }
}
