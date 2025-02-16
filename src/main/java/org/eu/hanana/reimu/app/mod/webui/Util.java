package org.eu.hanana.reimu.app.mod.webui;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Util extends org.eu.hanana.reimu.webui.core.Util {
    // 获取指定的查询参数
    public static String getQueryParam(String uri, String key) {
        // 解析查询字符串
        Map<String, String> queryParams = getQueryParams(uri);

        // 返回指定的参数值，如果不存在则返回 null
        return queryParams.get(key);
    }
    // 获取指定的查询参数
    public static Map<String,String> getQueryParams(String uri) {
        // 获取查询字符串部分（去掉 ? 后的部分）
        String queryString = uri.contains("?") ? uri.substring(uri.indexOf("?") + 1) : "";

        // 解析查询字符串
        Map<String, String> queryParams = parseQueryParams(queryString);

        // 返回指定的参数值，如果不存在则返回 null
        return queryParams;
    }
    // 解析查询字符串，将参数解析为 Map
    public static Map<String, String> parseQueryParams(String queryString) {
        Map<String, String> queryParams = new HashMap<>();
        try {
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queryParams;
    }
}
