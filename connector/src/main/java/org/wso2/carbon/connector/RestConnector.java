package org.wso2.carbon.connector;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.wso2.carbon.data.HttpHeaderData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * RestConnector.java
 */
public class RestConnector {

    private static Logger log = Logger.getLogger(RestConnector.class);
    private static int numberOfRestCallFailures =0;

    public String getRestData(String restURL , ArrayList<HttpHeaderData> headerDataList) throws HttpException, IOException{

        InputStreamReader reader = null;
        BufferedReader buffReader = null;
        String json = "";
        try {
            DefaultHttpClient client;
            client = new DefaultHttpClient();
            HttpGet request = new HttpGet(restURL);

            for(HttpHeaderData data : headerDataList){
                request.setHeader(data.getHeaderName() , data.getHeaderValue());
            }

            log.info("Connecting to server");
            HttpResponse response = client.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            log.info("Response returned status code " + statusCode);

            if (HttpStatus.SC_OK != statusCode) {
                numberOfRestCallFailures++;

                if(numberOfRestCallFailures < 4 && statusCode != HttpStatus.SC_NOT_FOUND   && statusCode != HttpStatus.SC_ACCEPTED ){
                    getRestData(restURL , headerDataList);
                }else{
                    throw new HttpException();
                }
            }

            reader = new InputStreamReader(response.getEntity().getContent());
            buffReader = new BufferedReader(reader);
            String line;
            while ((line = buffReader.readLine()) != null) {
                json = line;
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Error Occurred", e);
                }
            }
            if (buffReader != null) {
                try {
                    buffReader.close();
                } catch (IOException e) {
                    log.error("Error Occurred", e);
                }
            }
        }
        numberOfRestCallFailures = 0;
        return json;

    }
}
