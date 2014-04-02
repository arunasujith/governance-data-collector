package org.wso2.carbon.utils;

/**
 * Created by aruna on 3/24/14.
 */
public class StringUtility {
    public static String getTableString(String[] fields, String[] types, String streamName, String version
            , String nickname, String descrption) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{  'name':'");
        stringBuilder.append(streamName);
        stringBuilder.append("',  'version':'");
        stringBuilder.append(version);
        stringBuilder.append("',  'nickName': '");
        stringBuilder.append(nickname);
        stringBuilder.append("',  'description': '");
        stringBuilder.append(descrption);
        stringBuilder.append("',  'payloadData':[");
        for (int i = 0; i < fields.length; i++) {

            stringBuilder.append("{'name':'");
            stringBuilder.append(fields[i]);
            stringBuilder.append("','type':'");
            stringBuilder.append(types[i]);
            if (i == fields.length - 1) {
                stringBuilder.append("'}");
            } else {
                stringBuilder.append("'},");
            }

        }

        stringBuilder.append(" ]}");
        return stringBuilder.toString();
    }
}
