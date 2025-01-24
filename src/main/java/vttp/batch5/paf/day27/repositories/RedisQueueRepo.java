package vttp.batch5.paf.day27.repositories;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import vttp.batch5.paf.day27.utils.RedisUtils;

@Repository
public class RedisQueueRepo {
    
    @Autowired
    @Qualifier(RedisUtils.redisTemplate)
    RedisTemplate<String, Document> redisTemplate;

    public boolean pushEventToQueue(Document event){
        try {
            int listsLength = redisTemplate.opsForList().size(RedisUtils.eventSourceQueue).intValue();
            redisTemplate.opsForList().leftPush(RedisUtils.eventSourceQueue, event);
            int newListLength = redisTemplate.opsForList().size(RedisUtils.eventSourceQueue).intValue();
            return listsLength+1==newListLength;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
