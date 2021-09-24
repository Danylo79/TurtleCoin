package dev.dankom.cc.chain.coin;

import dev.dankom.cc.chain.BlockChain;
import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.util.CoinUtil;
import dev.dankom.cc.util.StringUtil;

import java.util.Date;

public class Coin {
    private String hash;
    private Integer nonce = 0;
    private long timestamp;

    public Coin(String hash) {
        this.hash = hash;
    }

    public Coin() {
        this.timestamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {
        return StringUtil.applySha256(nonce.toString() + timestamp);
    }

    public void mine(int difficulty) {
        String target = StringUtil.getDifficultyString(difficulty);
        while (!hash.substring(0, difficulty).equals(target) || !isValid()) {
            nonce++;
            hash = calculateHash();
        }
    }

    private boolean isValid() {
        for (Block b : BlockChain.blockchain) {
            if (b.isSender(BlockChain.getWallet("banker").publicKey) && CoinUtil.toHashes(b.coins).contains(hash)) {
                return false;
            }
        }
        return true;
    }

    public boolean isMined() {
        return hash != null;
    }

    public String getHash() {
        return hash;
    }
}
