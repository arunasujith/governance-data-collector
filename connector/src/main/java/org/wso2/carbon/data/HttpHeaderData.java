package org.wso2.carbon.data;

/**
 * Created by aruna on 3/21/14.
 */
public class HttpHeaderData {

    private String headerName;
    private String headerValue;

    public HttpHeaderData(String name , String value){
        this.headerName = name;
        this.headerValue = value;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }
}
