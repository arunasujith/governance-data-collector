package org.wso2.carbon.connector;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * HttpConnector.java
 */
public class HttpConnector {

    private static Logger log = Logger.getLogger(HttpConnector.class);

    public InputStream getHttpData(String httpURL){
        InputStream inputStream = null;

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(httpURL);
            HttpResponse response = httpClient.execute(httpget);

            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (HttpStatus.SC_OK == statusCode && entity != null) {
                inputStream = entity.getContent();
            }
        } catch(ClientProtocolException e){
            log.error("Exception" , e);
        } catch(IOException e){
            log.error("Exception" , e);
        }
        return inputStream;
    }
}
