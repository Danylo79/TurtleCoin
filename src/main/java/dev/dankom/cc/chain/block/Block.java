package dev.dankom.cc.chain.block;

import dev.dankom.cc.chain.coin.Coin;
import dev.dankom.cc.util.StringUtil;
import dev.dankom.util.general.DataStructureAdapter;

import java.security.PublicKey;
import java.util.Date;

public class Block {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public long timeStamp;
    public int nonce;
    public PublicKey sender;
    public PublicKey recipient;
    public Coin coin;

    public Block(String previousHash, PublicKey sender, PublicKey recipient, Coin coin) {
        this.previousHash = previousHash;
        this.sender = sender;
        this.recipient = recipient;
        this.coin = coin;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash();
    }

    public Block(String hash, String previousHash, String merkleRoot, long timeStamp, int nonce, PublicKey sender, PublicKey recipient, Coin coin) {
        this.hash = hash;
        this.previousHash = previousHash;
        this.merkleRoot = merkleRoot;
        this.timeStamp = timeStamp;
        this.nonce = nonce;
        this.sender = sender;
        this.recipient = recipient;
        this.coin = coin;
    }

    public String calculateHash() {
        return StringUtil.applySha256(previousHash + timeStamp + nonce + merkleRoot);
    }

    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(DataStructureAdapter.arrayToList(""));
        String target = StringUtil.getDifficultyString(difficulty);
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
    }

    public boolean isValid() {
        return hash != null && previousHash != null && merkleRoot != null && timeStamp != -1 && nonce != -1;
    }
}
