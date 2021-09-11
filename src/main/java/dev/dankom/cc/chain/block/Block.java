package dev.dankom.cc.chain.block;

import dev.dankom.cc.chain.transaction.Transaction;
import dev.dankom.cc.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static dev.dankom.cc.chain.block.BlockChain.logger;

public class Block {
    public String hash;
    public String previousHash;
    public String merkleRoot;
    public List<Transaction> transactions = new ArrayList<>();
    public long timeStamp;
    public int nonce;

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();

        this.hash = calculateHash();
    }

    public Block(String hash, String previousHash, String merkleRoot, List<Transaction> transactions, long timeStamp, int nonce) {
        this.hash = hash;
        this.previousHash = previousHash;
        this.merkleRoot = merkleRoot;
        this.transactions = transactions;
        this.timeStamp = timeStamp;
        this.nonce = nonce;
    }

    public String calculateHash() {
        return StringUtil.applySha256(previousHash + timeStamp + nonce + merkleRoot);
    }

    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDificultyString(difficulty);
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        logger.info("ClassroomCoin", "Block Mined: " + hash);
    }

    public boolean addTransaction(Transaction transaction) {
        if (transaction == null) return false;
        if ((!"0".equals(previousHash))) {
            if ((transaction.processTransaction() != true)) {
                logger.info("ClassroomCoin", "Transaction failed to process. Discarded.");
                return false;
            }
        }

        transactions.add(transaction);
        logger.info("ClassroomCoin", "Transaction Successfully added to Block");
        return true;
    }
}
