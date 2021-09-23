package dev.dankom.cc.chain;

import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.coin.Coin;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.cc.file.FileManager;
import dev.dankom.cc.util.CoinUtil;
import dev.dankom.file.json.JsonFile;
import dev.dankom.file.json.JsonObjectBuilder;
import dev.dankom.file.type.Directory;
import dev.dankom.interfaces.impl.ThreadMethodRunner;
import dev.dankom.logger.LogManager;
import dev.dankom.logger.abztract.DefaultLogger;
import dev.dankom.logger.interfaces.ILogger;
import dev.dankom.operation.operations.ShutdownOperation;
import dev.dankom.type.returner.Returner;
import dev.dankom.util.general.DataStructureAdapter;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockChain {
    public static final ILogger logger = LogManager.addLogger("BlockChain", new DefaultLogger());
    public static List<Wallet> wallets = new ArrayList<>();
    public static int difficulty;
    public static float minimumTransaction;
    public static ArrayList<Block> blockchain = new ArrayList<>();
    public final FileManager fileManager = new FileManager();

    public BlockChain(int difficulty, float minimumTransaction) {
        Security.addProvider(new BouncyCastleProvider());
        BlockChain.difficulty = difficulty;
        BlockChain.minimumTransaction = minimumTransaction;

//        wallets.add(new Wallet("danylo.komisarenko", "oNAxLmav", 710, 14, DataStructureAdapter.arrayToList("Admin")));
//        wallets.add(new Wallet("banker", "dfshbfh", 0, 0, DataStructureAdapter.arrayToList("Banker")));

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

    public void addBlock(Block b) {
        blockchain.add(b);
        if (!isChainValid()) {
            blockchain.remove(b);
        }
    }

    public void sendFunds() {

    }

    public void createGenesisBlock(Wallet sender, Wallet recipient, Coin... coins) {

    }

    public boolean isChainValid() {
        return true;
    }

    public void load() {

    }

    public void save() {

    }
}