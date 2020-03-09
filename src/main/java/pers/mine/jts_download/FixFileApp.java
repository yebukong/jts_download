package pers.mine.jts_download;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import pers.mine.jts_download.utils.DBUtils;
import pers.mine.jts_download.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * @author wxq
 * @description TODO
 * @create 2020-03-06 16:30
 */
public class FixFileApp {
    private static final Logger LOG = LoggerFactory.getLogger(FixFileApp.class);
    public static final String BASE_DIR = "./data";

    public static void main(String[] args) {
        JSONArray flieList;
        try {
            flieList = DBUtils.selectAllFile();
        } catch (Exception e) {
            flieList = new JSONArray();
            LOG.error("查询失败", e);
        }
        LOG.info("文件总数:{}", flieList.size());
        long start = System.currentTimeMillis();
        JSONObject jo;
        File file;
        String filePath;
        for (int i = 0; i < flieList.size(); i++) {
            jo = flieList.getJSONObject(i);
            try {
                filePath = BASE_DIR + File.separator + jo.getString("local_path");
                file = new File(filePath);
                if (file.exists() && file.isFile()) {
                    //修复文件名后缀缺失问题
                    String fileName = file.getName();
                    String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                    String attname = Utils.getAttname(jo.getString("download_url"));
                    String attname_suffix = attname.substring(attname.lastIndexOf(".") + 1);

                    String localDir = jo.getString("publish_date") + File.separator + jo.getString("source") + File.separator + jo.getString("advertiser");
                    if (jo.getString("local_path").contains(localDir)) {
                        String targetFileName = localDir + File.separator + jo.getInteger("id") + "_" + attname;
                        if (!targetFileName.equals(jo.getString("local_path"))) {
                            if (file.renameTo(new File(BASE_DIR + File.separator + targetFileName))) {
                                DBUtils.updateFileName(jo.getInteger("id"), attname, targetFileName);
                                LOG.info("文件修改成功 id={}, {} -> {}", jo.getInteger("id"), jo.getString("local_path"), targetFileName);
                            } else {
                                throw new RuntimeException("文件名修改失败" + jo.getString("local_path") + " -> " + targetFileName);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("处理失败:{}", jo, e);
                return;
            }
        }
    }
}
