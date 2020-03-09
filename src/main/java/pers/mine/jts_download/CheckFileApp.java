package pers.mine.jts_download;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import pers.mine.jts_download.utils.DBUtils;
import pers.mine.jts_download.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wxq
 * @description TODO
 * @create 2020-03-06 11:34
 */
public class CheckFileApp {
    private static final Logger LOG = LoggerFactory.getLogger(CheckFileApp.class);
    static AtomicInteger fileSum = new AtomicInteger(0);
    public static final String BASE_DIR = "./data";

    public static void main(String[] args) {
        JSONArray flieList;
        try {
            flieList = DBUtils.selectAllFile();
        } catch (Exception e) {
            flieList = new JSONArray();
            LOG.error("查询失败", e);
        }
        fileSum.set(flieList.size());
        LOG.info("文件总数:{}", fileSum.get());
        long start = System.currentTimeMillis();

        JSONArray nuDownList = new JSONArray();
        JSONArray nuSuffixList = new JSONArray();
        JSONArray nuDirList = new JSONArray();
        int size = flieList.size();
        JSONObject jo;
        File file;
        String filePath;
        //检测缺失文件
        for (int i = 0; i < size; i++) {
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
                            nuSuffixList.add(jo);
                        }
                    } else {
                        nuDirList.add(jo);
                        LOG.warn("目录错误:id={},localDir = {} local_path = {},", jo.getInteger("id"), filePath, jo.getString("local_path"));
                    }
                } else {
                    LOG.warn("文件缺失:{}", filePath);
                    nuDownList.add(jo);
                }
            } catch (Exception e) {
                LOG.error("处理失败:{}", jo, e);
            }
        }
        long end = System.currentTimeMillis();
        LOG.info("运行用时:{}min", (end - start) / 1000 / 60.0f);
        LOG.info("缺失文件数:{}", nuDownList.size());
        LOG.info("缺失文件列表:{}", nuDownList);

        LOG.info("目录不一致:{}", nuDirList.size());
        LOG.info("缺失后缀文件:");
        for (int i = 0; i < nuDirList.size(); i++) {
            jo = nuDirList.getJSONObject(i);
            try {
                String localDir = jo.getString("publish_date") + File.separator + jo.getString("source") + File.separator + jo.getString("advertiser");
                LOG.info("id={},localDir={},local_path={}", jo.getInteger("id"), localDir, jo.getString("local_path"));
            } catch (Exception e) {
                LOG.error("{}", jo, e);
            }
        }

        LOG.info("缺失后缀文件数:{}", nuSuffixList.size());
        LOG.info("缺失后缀文件:");
        for (int i = 0; i < nuSuffixList.size(); i++) {
            jo = nuSuffixList.getJSONObject(i);
            try {
                LOG.info("id={},attname={},local_path={}", jo.getInteger("id"), Utils.getAttname(jo.getString("download_url")), jo.getString("local_path"));
            } catch (Exception e) {
                LOG.error("{}", jo, e);
            }
        }
    }
}
