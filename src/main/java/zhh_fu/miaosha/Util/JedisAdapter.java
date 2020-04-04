package zhh_fu.miaosha.Util;

import com.alibaba.fastjson.JSON;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Set;

@Service
public class JedisAdapter implements InitializingBean {
    private JedisPool pool;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(JedisAdapter.class);

    //get对象
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + key;
            String str = jedis.get(realKey);
            T t = stringToBean(str, clazz);
            return t;
        }catch (Exception ex){
            logger.error("发生异常" + ex.getMessage());
            return null;
        }finally {
            if (jedis != null){
                jedis.close();
            }
        }
    }

    //set对象
    public <T> boolean set(KeyPrefix prefix, String key, T value){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();
            String str = beanToString(value);
            if (str == null || str.length() == 0){
                return false;
            }
            String realKey = prefix.getPrefix() + key;
            int seconds = prefix.expireSeconds();
            if (seconds <= 0){
                jedis.set(realKey, str);
            } else {
                jedis.setex(realKey, seconds, str);
            }
            return true;
        }catch (Exception ex){
            logger.error("发生异常" + ex.getMessage());
            return false;
        }finally {
            if (jedis != null){
                jedis.close();
            }
        }
    }

    //判断键是否存在
    public <T> boolean exists(KeyPrefix prefix, String key){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();
            String realKey = prefix.getPrefix() + key;
            jedis.exists(realKey);
            return true;
        }catch (Exception ex){
            logger.error("发生异常" + ex.getMessage());
            return false;
        }finally {
            if (jedis != null){
                jedis.close();
            }
        }
    }

    //值增加
    public <T> Long incr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.incr(realKey);
        } catch (Exception ex) {
            logger.error("发生异常" + ex.getMessage());
            return (long) 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    //值减少
    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.decr(realKey);
        } catch (Exception ex) {
            logger.error("发生异常" + ex.getMessage());
            return (long) 0;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    private <T> String beanToString(T value) {
        if (value == null) return null;
        Class<?> clazz = value.getClass();
        if(clazz == int.class || clazz == Integer.class) {
            return ""+value;
        }else if(clazz == String.class) {
            return (String)value;
        }else if(clazz == long.class || clazz == Long.class) {
            return ""+value;
        }else {
            return JSON.toJSONString(value);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.equals("") || clazz == null){
            return null;
        }
        if(clazz == int.class || clazz == Integer.class) {
            return (T)Integer.valueOf(str);
        }else if(clazz == String.class) {
            return (T)str;
        }else if(clazz == long.class || clazz == Long.class) {
            return  (T)Long.valueOf(str);
        }else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(500);
        poolConfig.setMaxTotal(1000);
        poolConfig.setMaxWaitMillis(10000);
        pool = new JedisPool("redis://192.168.132.129:6379");
    }

    public long sadd(String key,String value){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();
            return jedis.sadd(key,value);
        }
        catch (Exception ex){
            logger.error("发生异常" + ex.getMessage());
        }
        finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return 0;
    }

    public long srem(String key,String value){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();
            return jedis.srem(key,value);
        }
        catch (Exception ex){
            logger.error("发生异常" + ex.getMessage());
        }
        finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return 0;
    }

    public long scard(String key){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();
            return jedis.scard(key);
        }
        catch (Exception ex){
            logger.error("发生异常" + ex.getMessage());
        }
        finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return 0;
    }

    public boolean sismember(String key,String member){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();
            return jedis.sismember(key, member);
        }
        catch (Exception ex){
            logger.error("发生异常" + ex.getMessage());
        }
        finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return false;
    }

    public long lpush(String key,String value){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();
            return jedis.lpush(key,value);
        }
        catch (Exception ex){
            logger.error("发生异常" + ex.getMessage());
        }
        finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return 0;
    }

    public List<String> lrange(String key, int start, int end){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();
            return jedis.lrange(key,start,end);
        }
        catch (Exception ex){
            logger.error("发生异常" + ex.getMessage());
        }
        finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return null;
    }

    public List<String> brpop(int timeout, String key) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.brpop(timeout, key);
        }
        catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        }
        finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public Jedis getJedis(){
        return pool.getResource();
    }

    public Transaction multi(Jedis jedis){
        try{
            return jedis.multi();
        }
        catch (Exception ex){
            logger.error("发生异常" + ex.getMessage());
        }
        return null;
    }

    public List<Object> exec(Transaction tx, Jedis jedis){
        try{
            return tx.exec();
        }
        catch (Exception ex){
            logger.error("发生异常" + ex.getMessage());
            //事务回滚很重要
            tx.discard();
        }
        finally {
            if (tx != null){
                try{
                    tx.close();
                }
                catch(Exception ex){
                    logger.error("发生异常" + ex.getMessage());
                }
            }

            if (jedis != null){
                jedis.close();
            }
        }
        return null;
    }

    public long zadd(String key, double score, String value){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();
            return jedis.zadd(key,score,value);
        }
        catch (Exception ex){
            logger.error("发生异常" + ex.getMessage());
        }
        finally {
            if (jedis != null){
                jedis.close();
            }
        }
        return 0;
    }

    public Set<String> zrevrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zrevrange(key, start, end);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public long zcard(String key){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zcard(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public Double zscore(String key ,String member){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            return jedis.zscore(key, member);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }
}
