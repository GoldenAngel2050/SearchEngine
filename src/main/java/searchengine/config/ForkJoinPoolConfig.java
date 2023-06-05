package searchengine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ForkJoinPool;

@Configuration
public class ForkJoinPoolConfig {

    @Value("${myapp.pool.parallelism}")
    private int parallelism;

    @Bean
    public ForkJoinPool forkJoinPool() {
        return new ForkJoinPool(parallelism);
    }
}
