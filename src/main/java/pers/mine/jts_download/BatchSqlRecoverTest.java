package pers.mine.jts_download;

import pers.mine.jts_download.utils.DBUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class BatchSqlRecoverTest {
    static String sql = "insert into jts_works (aaa,bbb) values (?,?)";

    public static void main(String[] args) throws Exception {
        Connection conn = null;
        PreparedStatement ps = null;
        if (conn == null) {
            conn = DBUtils.DATASOURCE.getConnection();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);

        }
        ps.setString(1, "a");
        ps.setString(2, "b");
        ps.addBatch();
        ps.setString(1, "aa");
        ps.setString(2, "bb");
        ps.addBatch();
        List<Object> preparedStatementBatchedArgs = null;
        try {
            //需要提前执行
            preparedStatementBatchedArgs = DBUtils.getPreparedStatementBatchedArgs(ps);
            ps.executeBatch();
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(DBUtils.getInsertSql(ps));//insert into jts_works (aaa,bbb) values (** NOT SPECIFIED **,** NOT SPECIFIED **)
            System.out.println("-------------------------");
            System.out.println(DBUtils.getInsertSql(ps,preparedStatementBatchedArgs));//insert into jts_works (aaa,bbb) values ('a','b'),('aa','bb')
        }
    }

}
