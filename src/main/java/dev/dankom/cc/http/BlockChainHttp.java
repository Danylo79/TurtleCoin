package dev.dankom.cc.http;

import dev.dankom.cc.chain.BlockChain;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class BlockChainHttp {
    private static BlockChainHttp instance;
    private BlockChain bc;

    public static BlockChainHttp getInstance() {
        return instance;
    }

    public void run(BlockChain bc, String[] args) {
        this.bc = bc;
        instance = this;
        SpringApplication.run(BlockChainHttp.class, args);
    }

    public BlockChain getBlockchain() {
        return bc;
    }

    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**").allowedMethods("GET", "POST").allowedOrigins("http://local.dankom.ca:4200", "http://turtle.dankom.ca").allowCredentials(true);
        }
    }
}
