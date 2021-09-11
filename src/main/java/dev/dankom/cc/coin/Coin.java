package dev.dankom.cc.coin;

import dev.dankom.cc.chain.BlockChain;
import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.cc.chain.wallet.transaction.Transaction;
import dev.dankom.cc.util.StringUtil;
import dev.dankom.util.general.DataStructureAdapter;

public class Coin {
    private final Wallet wallet;
    private String hash;
    private int nonce = 0;

    public Coin(Wallet wallet) {
        this.wallet = wallet;
        this.hash = calculateHash();
    }

    public String calculateHash() {
        return StringUtil.applySha256(wallet.UTXOs.size() + wallet.publicKey.getFormat() + nonce);
    }

    public Coin mineBlock(int difficulty) {
        String target = StringUtil.getDifficultyString(difficulty);
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }

        if (isValid()) {
            wallet.addFunds(DataStructureAdapter.arrayToList(this));
        }
        return this;
    }

    public boolean isValid() {
        if (!isMined()) {
            return false;
        }
        for (Block bc : BlockChain.blockchain) {
            for (Transaction t : bc.transactions) {
                for (Coin c : t.value) {
                    if (c.hash.equals(hash)) {
                        return false;
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
