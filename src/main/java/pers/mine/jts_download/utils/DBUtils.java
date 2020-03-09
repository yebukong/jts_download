package pers.mine.jts_download.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * Created by Administrator on 2016/1/8.
 */
@SuppressWarnings("PMD.ClassNamingShouldBeCamelRule")
public final class DBUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DBUtils.class);
    public static final String PROXY_IP = "proxy_ip";
    public static final String PROXY_PORT = "proxy_port";
    public static final Properties DRUID_PROPS;

    public static DataSource DATASOURCE;

    static {
        DRUID_PROPS = new Properties();
        try {
            DRUID_PROPS.load(DBUtils.class.getResourceAsStream("/druid.properties"));
            DATASOURCE = DruidDataSourceFactory.createDataSource(DRUID_PROPS);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws Exception {
        try {
            return DATASOURCE.getConnection();
        } catch (Throwable t) {
            closeDataSource();
            DATASOURCE = DruidDataSourceFactory.createDataSource(DRUID_PROPS);
            return DATASOURCE.getConnection();
        }
    }

    public static void closeDataSource() {
        if (DATASOURCE != null) {
            ((DruidDataSource) DATASOURCE).close();
        }
    }

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (Throwable e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public static void close(Statement statement) {
        if (statement != null) {
            try {
                if (!statement.isClosed()) {
                    statement.close();
                }
            } catch (Throwable e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public static void close(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                if (!preparedStatement.isClosed()) {
                    preparedStatement.close();
                }
            } catch (Throwable e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public static void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                if (!resultSet.isClosed()) {
                    resultSet.close();
                }
            } catch (Throwable e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public static String escapeSql(String s) {
        if (null == s) {
            return null;
        }
        return s.replaceAll("'", "''").replaceAll("\\\\", "\\\\\\\\");
    }

    public static JSONArray selectFileList(int offset, int limit, String typeLimit) {
        String typeSql = "";
        if ("base".equals(typeLimit)) {
            typeSql = "and f.type in ('powerpoint','video')";
        } else if ("ext".equals(typeLimit)) {
            typeSql = "and f.type in ('image','pdf','sound')";
        }
        String sql = "select f.id as id,f.jts_id as jts_id,concat(f.id,'_',f.name) as name,f.download_url as download_url,ifnull(DATE_FORMAT(w.publish_date,'%Y'),'null') as publish_date,w.source as source,w.advertiser as advertiser,w.brand as brand " +
                " from jts_file f,jts_works w where f.jts_id = w.jts_id and LENGTH(ifnull(f.local_path,''))<1 " + typeSql + " order by f.jts_id limit " + offset + "," + limit;
        JSONArray result = new JSONArray();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            JSONObject one;
            while (rs.next()) {
                one = new JSONObject();
                one.put("id", rs.getInt("id"));
                one.put("jts_id", rs.getString("jts_id"));
                one.put("name", rs.getString("name"));
                one.put("download_url", rs.getString("download_url"));
                one.put("publish_date", rs.getString("publish_date"));
                one.put("source", rs.getString("source"));
                one.put("advertiser", rs.getString("advertiser"));
                one.put("brand", rs.getString("brand"));
                result.add(one);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            DBUtils.close(rs);
            DBUtils.close(ps);
            DBUtils.close(conn);
        }
        return result;
    }

    public static JSONArray selectAllFile() {
        String sql = "select f.id as id,f.jts_id as jts_id,f.name as name,concat(f.id,'_',f.name) as t_name,f.download_url as download_url,f.local_path as local_path,f.type as type,"
                + "ifnull(DATE_FORMAT(w.publish_date,'%Y'),'null') as publish_date,w.source as source,w.advertiser as advertiser "
                + " from jts_file f,jts_works w where f.jts_id = w.jts_id order by f.jts_id";
        JSONArray result = new JSONArray();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            JSONObject one;
            while (rs.next()) {
                one = new JSONObject();
                one.put("id", rs.getInt("id"));
                one.put("jts_id", rs.getString("jts_id"));
                one.put("name", rs.getString("name"));
                one.put("t_name", rs.getString("t_name"));
                one.put("download_url", rs.getString("download_url"));
                one.put("local_path", rs.getString("local_path"));
                one.put("type", rs.getString("type"));
                one.put("publish_date", rs.getString("publish_date"));
                one.put("source", rs.getString("source"));
                one.put("advertiser", rs.getString("advertiser"));
                result.add(one);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } finally {
            DBUtils.close(rs);
            DBUtils.close(ps);
            DBUtils.close(conn);
        }
        return result;
    }

    public static void updateLoaclPath(int id, String loaclPath) throws Exception {
        String sql = "UPDATE `jts_file` SET local_path=?,update_time=now() WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(2, id);
            ps.setString(1, loaclPath);
            int i = ps.executeUpdate();
            if (i != 1) {
                throw new RuntimeException("不是预期的更新行数:" + i + ",id=" + id);
            }
        } finally {
            DBUtils.close(rs);
            DBUtils.close(ps);
            DBUtils.close(conn);
        }
    }

    public static void updateFileName(int id, String name,String loaclPath) throws Exception {
        String sql = "UPDATE `jts_file` SET name=?,local_path=?,update_time=now() WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtils.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, loaclPath);
            ps.setInt(3, id);

            int i = ps.executeUpdate();
            if (i != 1) {
                throw new RuntimeException("不是预期的更新行数:" + i + ",id=" + id);
            }
        } finally {
            DBUtils.close(rs);
            DBUtils.close(ps);
            DBUtils.close(conn);
        }
    }

    public static void main(String[] args) throws Exception {
//        JSONArray result = selectFileList(0, 100);
//        LOG.info(result.toJSONString());
        updateLoaclPath(46, null);
    }
}
