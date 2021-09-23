package dev.dankom.cc.chain.coin;

import dev.dankom.cc.util.StringUtil;

public class Coin {
    private String hash;
    private Integer nonce = 0;

    public Coin(String hash) {
        this.hash = hash;
    }

    public Coin() {
        this.hash = calculateHash();
    }

    public String calculateHash() {
        return StringUtil.applySha256(nonce.toString());
    }

    public Coin mineBlock(int difficulty) {
        String target = StringUtil.getDifficultyString(difficulty);
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        return this;
    }

    public boolean isValid() {
        return true;
    }

    public boolean isMined() {
        return hash != null;
    }

    public String getHash() {
        return hash;
    }
}
