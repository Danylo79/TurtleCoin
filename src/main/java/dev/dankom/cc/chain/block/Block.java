package dev.dankom.cc.chain.block;

import dev.dankom.cc.chain.coin.Coin;
import dev.dankom.cc.util.EncodingUtil;
import dev.dankom.cc.util.StringUtil;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

public class Block {
    public String hash;
    public String previousHash;
    public long timeStamp;
    public int nonce;
    public PublicKey sender;
    public PublicKey recipient;
    public List<Coin> coins;

    public Block(String previousHash, PublicKey sender, PublicKey recipient, List<Coin> coins) {
        this.previousHash = previousHash;
        this.sender = sender;
        this.recipient = recipient;
        this.coins = coins;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash();
    }

    public Block(String hash, String previousHash, long timeStamp, int nonce, PublicKey sender, PublicKey recipient, List<Coin> coins) {
        this.hash = hash;
        this.previousHash = previousHash;
        this.timeStamp = timeStamp;
        this.nonce = nonce;
        this.sender = sender;
        this.recipient = recipient;
        this.coins = coins;
    }

    public String calculateHash() {
        return StringUtil.applySha256(previousHash + timeStamp + nonce);
    }

    public void mineBlock(int difficulty) {
        String target = StringUtil.getDifficultyString(difficulty);
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
    }

    public boolean isValid() {
        return hash != null && previousHash != null && timeStamp != -1 && nonce != -1;
    }

    public boolean isRecipient(PublicKey publicKey) {
        return EncodingUtil.hexFromBytes(publicKey.getEncoded()).equals(EncodingUtil.hexFromBytes(recipient.getEncoded()));
    }

    public boolean isSender(PublicKey publicKey) {
        return EncodingUtil.hexFromBytes(publicKey.getEncoded()).equals(EncodingUtil.hexFromBytes(sender.getEncoded()));
    }
}
