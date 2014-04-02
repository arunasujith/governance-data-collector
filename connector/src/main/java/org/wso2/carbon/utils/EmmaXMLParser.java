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

/**
 * EmmaXMLParser.java
 */
public class EmmaXMLParser {

    private File xmlFile;
    private EmmaData emmaData = new EmmaData();
    private static Logger log = Logger.getLogger(EmmaXMLParser.class);

    public EmmaXMLParser(String xmlFileName) {
        xmlFile = new File(xmlFileName);
    }

    /**
     * Parse the xml file and return EmmaData
     * @return EmmaData bean
     */

    public EmmaData parse(String reportID) {

        if (this.xmlFile.exists()) {

            try {
                Document document = getDocument();
                parseXmlFile(document);
            } catch (ParserConfigurationException e) {
                log.error("Exception ", e);
            } catch (SAXException e) {
                log.error("Exception ", e);
            } catch (IOException e) {
                log.error("Exception ", e);
            }
        }
        emmaData.setReportID(reportID);
        return emmaData;
    }

    private Document getDocument() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setValidating(false);
        domFactory.setNamespaceAware(false);
        DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
        return domBuilder.parse(xmlFile);
    }

    /**
     * Read XML Document
     * @param document document to be parsed
     */
    private void parseXmlFile(Document document) {

        document.getDocumentElement().normalize();

        //read the stats
        NodeList statsNodeList = document.getElementsByTagName(Constants.STATS);
        Node statNode = statsNodeList.item(0);
        if (statNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) statNode;

            Node packages = eElement.getElementsByTagName(Constants.PACKAGES).item(0);
            if (packages.getNodeType() == Node.ELEMENT_NODE) {
                ((Element) packages).getAttribute(Constants.VALUE);
                int numberOfPackages = Integer.parseInt(((Element) packages).getAttribute(Constants.VALUE));
                emmaData.setNumberOfPackages(numberOfPackages);
            }
            Node classes = eElement.getElementsByTagName(Constants.CLASSES).item(0);
            if (classes.getNodeType() == Node.ELEMENT_NODE) {
                int numberOfClasses = Integer.parseInt(((Element) classes).getAttribute(Constants.VALUE));
                emmaData.setNumberOfClasses(numberOfClasses);
            }
            Node methods = eElement.getElementsByTagName(Constants.METHODS).item(0);
            if (methods.getNodeType() == Node.ELEMENT_NODE) {
                int numberOfMethods = Integer.parseInt(((Element) methods).getAttribute(Constants.VALUE));
                emmaData.setNumberOfMethods(numberOfMethods);
            }
            Node srcfiles = eElement.getElementsByTagName(Constants.SRC_FILES).item(0);
            if (srcfiles.getNodeType() == Node.ELEMENT_NODE) {
                int numberOfSrcFiles = Integer.parseInt(((Element) srcfiles).getAttribute(Constants.VALUE));
                emmaData.setNumberOfFiles(numberOfSrcFiles);
            }
            Node srclines = eElement.getElementsByTagName(Constants.SRC_LINES).item(0);
            if (srclines.getNodeType() == Node.ELEMENT_NODE) {
                int numberOfSrcLines = Integer.parseInt(((Element) srclines).getAttribute(Constants.VALUE));
                emmaData.setNumberOfLines(numberOfSrcLines);
            }


        }

        //read the all classes
        NodeList dataNodeList = document.getElementsByTagName(Constants.ALL);
        Node dataNode = dataNodeList.item(0);
        if (dataNode.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) dataNode;
            for (int i = 0; i < 4; i++) {
                Node coverage = eElement.getElementsByTagName(Constants.COVERAGE).item(i);
                if (coverage.getNodeType() == Node.ELEMENT_NODE) {
                    if (((Element) coverage).getAttribute(Constants.TYPE).equals(Constants.CLASS_COVERAGE)) {
                        emmaData.setAllClassCoverage(((Element) coverage).getAttribute(Constants.VALUE));
                    } else if (((Element) coverage).getAttribute(Constants.TYPE).equals(Constants.METHOD_COVERAGE)) {
                        emmaData.setAllMethodCoverage(((Element) coverage).getAttribute(Constants.VALUE));
                    } else if (((Element) coverage).getAttribute(Constants.TYPE).equals(Constants.BLOCK_COVERAGE)) {
                        emmaData.setAllBlockCoverage(((Element) coverage).getAttribute(Constants.VALUE));
                    } else if (((Element) coverage).getAttribute(Constants.TYPE).equals(Constants.LINE_COVERAGE)) {
                        emmaData.setAllLineCoverage(((Element) coverage).getAttribute(Constants.VALUE));
                    }
                }
            }
        }

        //read all packages
        NodeList packageNodeList = document.getElementsByTagName(Constants.PACKAGE);
        for (int i = 0; i < packageNodeList.getLength(); i++) {
            Node packageNode = packageNodeList.item(i);
            if (packageNode.getNodeType() == Node.ELEMENT_NODE) {
                EmmaPackageData emmaPackageData = new EmmaPackageData();

                Element eElement = (Element) packageNode;
                emmaPackageData.setPackageName(eElement.getAttribute(Constants.NAME));

                for (int j = 0; j < 4; j++) {
                    Node coverage = eElement.getElementsByTagName(Constants.COVERAGE).item(j);
                    if (coverage.getNodeType() == Node.ELEMENT_NODE) {
                        if (((Element) coverage).getAttribute(Constants.TYPE).equals(Constants.CLASS_COVERAGE)) {
                            emmaPackageData.setClassCoverage(((Element) coverage).getAttribute(Constants.VALUE));
                        } else if (((Element) coverage).getAttribute(Constants.TYPE).equals(Constants.METHOD_COVERAGE)) {
                            emmaPackageData.setMethodCoverage(((Element) coverage).getAttribute(Constants.VALUE));
                        } else if (((Element) coverage).getAttribute(Constants.TYPE).equals(Constants.BLOCK_COVERAGE)) {
                            emmaPackageData.setBlockCoverage(((Element) coverage).getAttribute(Constants.VALUE));
                        } else if (((Element) coverage).getAttribute(Constants.TYPE).equals(Constants.LINE_COVERAGE)) {
                            emmaPackageData.setLineCoverage(((Element) coverage).getAttribute(Constants.VALUE));
                        }

                    }
                }
                emmaData.addToEmmaPackageList(emmaPackageData);
            }
        }
    }

}
