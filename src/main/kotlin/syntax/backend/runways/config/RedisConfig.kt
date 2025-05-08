package syntax.backend.runways.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    /*
    // RedisConnectionFactory 직접 설정 - 현재는 자동 설정 사용
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration("localhost", 6379)
        config.setPassword("password")
        return LettuceConnectionFactory("localhost", 6379)
    }
    */

    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val redisTemplate = RedisTemplate<String, String>()
        redisTemplate.connectionFactory = redisConnectionFactory
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = StringRedisSerializer()
        return redisTemplate
    }
}