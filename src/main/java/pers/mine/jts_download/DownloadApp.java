package pers.mine.jts_download;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import pers.mine.jts_download.utils.DBUtils;
import pers.mine.jts_download.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wxq
 * @description TODO
 * @create 2020-03-03 15:44
 */
public class DownloadApp {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadApp.class);
    public static final String BASE_DIR = ".";

    static AtomicInteger fileSum = new AtomicInteger(0);
    static AtomicBoolean shouldStop = new AtomicBoolean(false);

    public static void main(String[] args) throws IOException {
        int offset = Integer.parseInt(args[0]);
        int limit = Integer.parseInt(args[1]);
        String limitType = args[2];
        if (!"all,base,ext".contains(limitType)) {
            throw new RuntimeException("无效limitType：" + limitType);
        }
        int poolSize = 3;
        if (args.length > 3) {
            poolSize = Integer.parseInt(args[3]);
        }
        LOG.info("offset={} limit={} limitType={} poolSize={} ", offset, limit, limitType, poolSize);

        //初始化并发线程池
        final ExecutorService fixedThreadPool = new ThreadPoolExecutor(
                poolSize
                , poolSize
                , 0L
                , TimeUnit.MILLISECONDS
                , new LinkedBlockingQueue<Runnable>(1)
                , Executors.defaultThreadFactory()
                , new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    throw new RejectedExecutionException("Unexpected InterruptedException", e);
                }
            }
        }
        );
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shouldStop.set(true);
                try {
                    if (fixedThreadPool != null) {
                        fixedThreadPool.shutdown();
                        boolean flag = fixedThreadPool.awaitTermination(30 * 1000L, TimeUnit.MILLISECONDS);
                        if (!flag) {
                            fixedThreadPool.shutdownNow();
                        }
                    }
                } catch (Throwable e) {
                    LOG.error(e.getMessage(), e);
                }
                LOG.info("Shutdown executor service");
            }
        });
        //查询待下载列表
        JSONArray flieList;
        try {
            flieList = DBUtils.selectFileList(offset, limit, limitType);
        } catch (Exception e) {
            flieList = new JSONArray();
            LOG.error("查询失败", e);
        }

        fileSum.set(flieList.size());
        LOG.info("下载任务总数:{}", fileSum.get());
        long start = System.currentTimeMillis();
        int size = flieList.size();
        for (int i = 0; i < size; i++) {
            if (shouldStop.get()) {
                LOG.info("任务退出!");
                break;
            }
            fixedThreadPool.execute(new RealRunner(flieList.getJSONObject(i)));
        }
        while (!shouldStop.get() && fileSum.get() > 0) {
            try {
                LOG.info("wait...剩余:{}", fileSum.get());
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long end = System.currentTimeMillis();
        LOG.info("未完成任务总数:{}", fileSum.get());
        LOG.info("运行用时:{}min", (end - start) / 1000 / 60.0f);
        System.exit(0);
    }

    private static class RealRunner implements Runnable {
        private static final Logger LOG = LoggerFactory.getLogger(RealRunner.class);

        private JSONObject jo = null;

        public RealRunner(JSONObject jo) {
            this.jo = jo;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            LOG.info("子任务开始:{}", jo.toJSONString());
            try {
                String urlEncoder = Utils.normalizeJtsURL(jo.getString("download_url"));
                String localDir = jo.getString("publish_date") + File.separator + jo.getString("source") + File.separator + jo.getString("advertiser");
                String savePath = BASE_DIR + File.separator + localDir;
                String result = Utils.downLoadFromUrl(urlEncoder, jo.getString("name"), savePath, null);
                File file = new File(result);
                if (!file.exists()) {
                    throw new RuntimeException("下载文件不存在:" + result);
                } else {
                    LOG.info("下载文件路径:{}", file.getAbsolutePath());
                }
                String localDirPath = localDir + File.separator + jo.getString("name");
                DBUtils.updateLoaclPath(jo.getInteger("id"), localDirPath);
                fileSum.decrementAndGet();
            } catch (Exception e) {
                LOG.error("文件下载失败:{}", jo, e);
            }
            long end = System.currentTimeMillis();
            LOG.info("子任务结束:{} ，耗时:{}min,剩余任务:{}", jo.toJSONString(), (end - start) / 1000 / 60.0f, fileSum.get());
        }
    }

}

