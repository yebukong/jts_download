package pers.mine.jts_download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisNoScriptException;

import java.util.List;

/**
 * @author wangxiaoqiang
 * @description TODO
 * @create 2020-04-03 11:15
 */
public class RedisTest {
    private static final Logger LOG = LoggerFactory.getLogger(RedisTest.class);

    //使指定key的value自增并返回自增前数值,参数列表[key][timeToLiveSeconds],超时时间仅在初次添加时设置
    public static final String INCR_EX_LUA = "if redis.call('EXISTS',KEYS[1]) == 1 then return tostring(redis.call('INCR',KEYS[1])-1) else redis.call('SETEX',KEYS[1],ARGV[1],1) return '0' end";
    public static volatile String INCR_EX_SHA = "";
    //给指定key的set添加成员并返回添加后成员数量,参数列表[setKey][addMember][timeToLiveSeconds],超时时间仅在初次添加时设置
    public static final String SADD_EX_LUA = "if redis.call('EXISTS',KEYS[1]) == 1 then redis.call('SADD',KEYS[1],ARGV[1]) return tostring(redis.call('SCARD',KEYS[1])) else redis.call('SADD',KEYS[1],ARGV[1]) redis.call('EXPIRE',KEYS[1],ARGV[2]) return '1' end";
    public static volatile String SADD_EX_SHA = "";

    public static void main(String[] args) {
        test2(args);
    }

    public static void test2(String[] args) {
        String host = "127.0.0.1";
        int port = 6379;
        int db = 15;
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT, null, db);
        Jedis jedis = null;
        List<Object> pipeResults = null;
        try {
            jedis = jedisPool.getResource();
            Pipeline pipelined = jedis.pipelined();
            pipelined.evalsha("test", 1, "key10086", "x_", Integer.toString(300));

            try (Jedis jedis1 = jedisPool.getResource()) {
                INCR_EX_SHA = jedis1.scriptLoad(INCR_EX_LUA);
                SADD_EX_SHA = jedis1.scriptLoad(SADD_EX_LUA);
                LOG.info("loadLua - script {} , sha {}", INCR_EX_LUA, INCR_EX_SHA);
                LOG.info("loadLua - script {} , sha {}", SADD_EX_LUA, SADD_EX_SHA);
            }
            pipeResults = pipelined.syncAndReturnAll();
        } catch (Exception e) {
            LOG.warn("执行异常", e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        for (Object pipeResult : pipeResults) {
            System.out.println(pipeResult.getClass().getName());
            System.out.println(pipeResult);
            if(pipeResult instanceof Exception){
                LOG.warn("执行异常", (Exception)pipeResult);
            }
        }
        if (jedisPool != null) {
            jedisPool.close();
        }
    }

    public static void test1(String[] args) {
        String host = "127.0.0.1";
        int port = 6379;
        int db = 5;
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT, null, db);
        try (Jedis jedis = jedisPool.getResource()) {
            INCR_EX_SHA = jedis.scriptLoad(INCR_EX_LUA);
            SADD_EX_SHA = jedis.scriptLoad(SADD_EX_LUA);
            LOG.info("loadLua - script {} , sha {}", INCR_EX_LUA, INCR_EX_SHA);
            LOG.info("loadLua - script {} , sha {}", SADD_EX_LUA, SADD_EX_SHA);
        }

        Jedis jedis = null;
        List<Object> pipeResults = null;
        try {
            jedis = jedisPool.getResource();
            Pipeline pipelined = jedis.pipelined();
            pipelined.eval("return '1'");
            pipelined.eval("return '1'");
            for (int i = 0; i < 5; i++) {

                //pipelined.setex("123" + i, 30, i + "_");
                pipelined.evalsha(SADD_EX_SHA, 1, "key10086", "x_" + i, Integer.toString(300));
                pipelined.evalsha(INCR_EX_SHA, 1, "key10010", Integer.toString(300));
            }
            pipeResults = pipelined.syncAndReturnAll();
        } catch (JedisNoScriptException e) {
            LOG.warn("lua脚本失效,将重新加载", e);
            INCR_EX_SHA = jedis.scriptLoad(INCR_EX_LUA);
            SADD_EX_SHA = jedis.scriptLoad(SADD_EX_LUA);
            LOG.info("loadLua - script {} , sha {}", INCR_EX_LUA, INCR_EX_SHA);
            LOG.info("loadLua - script {} , sha {}", SADD_EX_LUA, SADD_EX_SHA);
            Pipeline pipelined = jedis.pipelined();
            pipeResults = pipelined.syncAndReturnAll();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        for (Object pipeResult : pipeResults) {
            System.out.println(pipeResult);
        }
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
