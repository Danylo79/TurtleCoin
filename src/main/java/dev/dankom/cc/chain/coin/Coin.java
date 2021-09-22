package dev.dankom.cc.chain.coin;

import dev.dankom.cc.chain.BlockChain;
import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.wallet.transaction.Transaction;
import dev.dankom.cc.util.HashUtil;
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
        for (Block bc : BlockChain.blockchain) {
            for (Transaction t : bc.transactions) {
                if (HashUtil.hexFromBytes(t.sender.getEncoded()).equals(BlockChain.getWallet("banker").publicKey)) {
                    for (Coin c : t.value) {
                        if (c.hash.equals(hash)) {
                            return false;
                        }
                    }
                }
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
