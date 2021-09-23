package dev.dankom.cc.chain;

import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.coin.Coin;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.cc.file.FileManager;
import dev.dankom.interfaces.impl.ThreadMethodRunner;
import dev.dankom.logger.LogManager;
import dev.dankom.logger.abztract.DefaultLogger;
import dev.dankom.logger.interfaces.ILogger;
import dev.dankom.operation.operations.ShutdownOperation;
import dev.dankom.util.general.DataStructureAdapter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

public class BlockChain {
    public static final ILogger logger = LogManager.addLogger("BlockChain", new DefaultLogger());
    public static List<Wallet> wallets = new ArrayList<>();
    public static int difficulty;
    public static float minimumTransaction;
    public static ArrayList<Block> blockchain = new ArrayList<>();
    public final FileManager fileManager = new FileManager();

    public BlockChain(int difficulty, float minimumTransaction) {
        load();

        Security.addProvider(new BouncyCastleProvider());
        BlockChain.difficulty = difficulty;
        BlockChain.minimumTransaction = minimumTransaction;

        if (getWallet("banker") != null) {
            createWallet("banker", Wallet.createPin(10), 0, 0, "Banker");
        }

        new ShutdownOperation(new ThreadMethodRunner(() -> save()), "Save", logger);
    }

    public static Wallet getWallet(String username) {
        for (Wallet w : wallets) {
            if (w.getUsername().equalsIgnoreCase(username)) return w;
        }
        return null;
    }

    public static Wallet getWallet(String username, String pin, int roomNumber, int studentNumber) {
        for (Wallet w : wallets) {
            if (w.getUsername().equalsIgnoreCase(username) && w.getPin().equalsIgnoreCase(pin) && w.getHomeroom() == roomNumber && w.getStudentNumber() == studentNumber)
                return w;
        }
        return null;
    }

    public static Wallet getWallet(PublicKey key) {
        for (Wallet w : wallets) {
            if (w.publicKey == key) return w;
        }
        return null;
    }

    public static void createWallet(String username, String pin, int homeroom, int studentNumber, String... jobs) {
        wallets.add(new Wallet(username, pin, homeroom, studentNumber, DataStructureAdapter.arrayToList(jobs)));
    }

    public void addBlock(Block b) {
        blockchain.add(b);
        if (!isChainValid()) {
            blockchain.remove(b);
        }
    }

    public void sendFunds(Wallet sender, Wallet recipient, Coin... coins) {
        if (hasGenesis()) {
            createBlock(blockchain.get(blockchain.size() - 1).hash, sender, recipient, coins);
        } else {
            createBlock("0", sender, recipient, coins);
        }
    }

    public void createBlock(String hash, Wallet sender, Wallet recipient, Coin... coins) {
        Block b = new Block(hash, sender.publicKey, recipient.publicKey, DataStructureAdapter.arrayToList(coins));
        addBlock(b);
    }

    public boolean hasGenesis() {
        for (Block b : blockchain) {
            if (b.hash.equals("0")) return true;
        }
        return false;
    }

    public boolean isChainValid() {
        return true;
    }

    public void load() {

    }

    public void save() {

    }
}