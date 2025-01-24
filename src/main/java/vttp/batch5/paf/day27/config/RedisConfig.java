package vttp.batch5.paf.day27.config;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import vttp.batch5.paf.day27.utils.RedisUtils;


@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.username}")
    private String redisUsername;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(){
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);

        if (redisUsername.trim().length() > 0){
            config.setUsername(redisUsername);
            config.setPassword(redisPassword);
        }

        JedisClientConfiguration jcc =  JedisClientConfiguration.builder().build();
        JedisConnectionFactory jcf = new JedisConnectionFactory(config, jcc);

        jcf.afterPropertiesSet();
        return jcf;
    }

    @Bean(name = RedisUtils.redisTemplate)
    public RedisTemplate<String, Document> redisStringTemplate(Jackson2JsonRedisSerializer<Document> serializer){
        RedisTemplate<String, Document> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setDefaultSerializer(serializer);
        template.setKeySerializer(new StringRedisSerializer());

        return template;
    }

    @Bean
    public Jackson2JsonRedisSerializer<Document> jackson2JsonRedisSerializer(){
        return new Jackson2JsonRedisSerializer<>(Document.class);
    }
}
