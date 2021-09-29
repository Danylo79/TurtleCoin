package dev.dankom.cc;

import dev.dankom.cc.chain.BlockChain;
import dev.dankom.cc.http.BlockChainHttp;

public class Start {
    public static void main(String[] args) {
        BlockChain bc = new BlockChain(3, 1.0f);
//        BlockChainHttp bch = new BlockChainHttp();
//        bch.run(bc, args);
    }
}
