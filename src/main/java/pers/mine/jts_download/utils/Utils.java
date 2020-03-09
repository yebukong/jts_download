package pers.mine.jts_download.utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wxq
 * @description TODO
 * @create 2020-03-04 14:19
 */
public class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    public static String normalizeURL(String urlStr) {
        URL url = null;
        try {
            url = new URL(urlStr);
            String protocol = url.getProtocol();
            int port = url.getPort();
            String host = url.getHost().toLowerCase();
            String path = url.getPath();
            String queryString = url.getQuery();

            String normalizedQueryStr = null;
            StringBuilder builder = new StringBuilder();

            if (queryString != null && !"".equals(queryString = queryString.trim())/*end with ?*/) {
                List<String> queryStringParts = new ArrayList<>();
                String[] split = queryString.split("&");
                for (String param : split) {
                    String[] p = param.split("=");
                    if (p.length > 1) {
                        queryStringParts.add(p[0] + "=" + URLEncoder.encode(p[1], "UTF-8"));
                    } else {
                        queryStringParts.add(p[0] + "=");
                    }
                }

                Collections.sort(queryStringParts);
                for (String param : queryStringParts) {
                    builder.append('&').append(param);
                }
                if (builder.length() > 0) {
                    normalizedQueryStr = builder.toString().substring(1);
                }

            }
            String ref = url.getRef();
            builder.setLength(0);
            builder.append(protocol).append("://").append(host);
            if (port != -1) {
                builder.append(':').append(port);
            }
            builder.append(path);
            if (normalizedQueryStr != null) {
                builder.append('?').append(normalizedQueryStr);
            }
            if (ref != null) {
                builder.append('#');
                builder.append(ref);
            }
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Pattern pattern = Pattern.compile("(?<baseUrl>.+)\\?attname=(?<param>.+)");

    public static String normalizeJtsURL(String url) throws Exception {
        Matcher m = pattern.matcher(url);
        String baseUrl = "";
        String param = "";
        if (m.find()) {
            baseUrl = m.group("baseUrl");
            param = m.group("param");
            System.out.println(baseUrl);
            System.out.println(param);
            return baseUrl + "?attname=" + URLEncoder.encode(param, "UTF-8");
        } else {
            throw new RuntimeException("不是预期的URL：" + url);
        }
    }

    public static Pattern pattern1 = Pattern.compile("\\?attname=(?<param>.+)");

    public static String getAttname(String url) throws Exception {
        Matcher m = pattern.matcher(url);
        if (m.find()) {
            return m.group("param");
        } else {
            throw new RuntimeException("不是预期的URL：" + url);
        }
    }

    /**
     * 从网络Url中下载文件
     *
     * @param urlStr
     * @param fileName
     * @param savePath
     * @throws IOException
     */
    public static String downLoadFromUrl(String urlStr, String fileName, String savePath, Proxy proxy) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = null;
        if (proxy != null) {
            conn = (HttpURLConnection) url.openConnection(proxy);
        } else {
            conn = (HttpURLConnection) url.openConnection();
        }
        conn.setRequestMethod("GET");
        //设置超时
        conn.setConnectTimeout(5 * 1000);
        conn.setReadTimeout(5 * 1000);
        //UA
        conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        //conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.34 Safari/537.36 Edg/81.0.416.20");
        InputStream inputStream = conn.getInputStream();
        //文件保存位置
        File saveDir = new File(savePath);
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }
        String filePath = saveDir.getPath() + File.separator + fileName;
        File file = new File(filePath);
        FileUtils.copyInputStreamToFile(inputStream, file);
        LOG.info("info:" + url + " download success");
        return filePath;
    }
}
