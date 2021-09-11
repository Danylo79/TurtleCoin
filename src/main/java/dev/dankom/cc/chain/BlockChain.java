package dev.dankom.cc.chain;

import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.wallet.transaction.TransactionOutput;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain {
    public static int difficulty;
    public static float minimumTransaction = 0.1f;

    public static ArrayList<Block> blockchain = new ArrayList<>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    public BlockChain(int difficulty) {
        this.difficulty = difficulty;

        Security.addProvider(new BouncyCastleProvider());
    }

    public void addBlock(Block block) {
        block.mineBlock(difficulty);
        blockchain.add(block);
    }

    public boolean isChainValid() {
        boolean flag = false;
        for (int i = 0; i < blockchain.size(); i++) {
            String previousHash = i == 0 ? "0" : blockchain.get(i - 1).getHash();
            Block block = blockchain.get(i);
            flag = block.getHash().equals(block.calculateBlockHash())
                    && previousHash.equals(block.getPreviousHash())
                    && block.getHash().substring(0, difficulty).equals(new String(new char[difficulty]).replace('\0', '0'));
            if (!flag) break;
        }
        return flag;
    }
}
