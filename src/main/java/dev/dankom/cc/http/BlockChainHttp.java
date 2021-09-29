package dev.dankom.cc.http;

import dev.dankom.cc.chain.BlockChain;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
}
