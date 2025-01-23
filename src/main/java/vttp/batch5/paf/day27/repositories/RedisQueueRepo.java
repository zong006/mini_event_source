package vttp.batch5.paf.day27.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.json.JsonObject;
import vttp.batch5.paf.day27.utils.RedisUtils;

@Repository
public class RedisQueueRepo {
    
    @Autowired
    @Qualifier(RedisUtils.redisTemplate)
    RedisTemplate<String, String> stringTemplate;

    public boolean pushEventToQueue(JsonObject event ){
        try {
            stringTemplate.opsForList().leftPush(RedisUtils.eventSourceQueue, event.toString());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
