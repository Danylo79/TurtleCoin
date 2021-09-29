package dev.dankom.cc.chain.block;

import dev.dankom.cc.chain.wallet.transaction.Transaction;
import dev.dankom.cc.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;

public class Block {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<>();
    public long timeStamp;
    public int nonce;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash();
    }

    public Block(String hash, String previousHash, String merkleRoot, Transaction transaction, long timeStamp, int nonce) {
        this.hash = hash;
        this.previousHash = previousHash;
        this.merkleRoot = merkleRoot;
        this.transactions.add(transaction);
        this.timeStamp = timeStamp;
        this.nonce = nonce;
    }

    public String calculateHash() {
        return StringUtil.applySha256(previousHash + timeStamp + nonce + merkleRoot);
    }

    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDifficultyString(difficulty);
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
    }

    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) return false;
        if (!"0".equals(previousHash) && !transaction.processTransaction()) {
            return false;
        }

        transactions.add(transaction);
        return true;
    }

    public boolean isValid() {
        return hash != null && previousHash != null && merkleRoot != null && transactions != null && timeStamp != -1 && nonce != -1;
    }
}
