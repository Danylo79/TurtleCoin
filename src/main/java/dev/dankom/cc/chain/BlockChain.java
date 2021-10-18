package dev.dankom.cc.chain;

import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.coin.Coin;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.cc.file.FileManager;
import dev.dankom.cc.util.CoinUtil;
import dev.dankom.cc.util.EncodingUtil;
import dev.dankom.cc.util.JSONUtil;
import dev.dankom.file.json.JsonFile;
import dev.dankom.file.json.JsonObjectBuilder;
import dev.dankom.file.type.Directory;
import dev.dankom.interfaces.impl.ThreadMethodRunner;
import dev.dankom.logger.LogManager;
import dev.dankom.logger.abztract.DefaultLogger;
import dev.dankom.logger.interfaces.ILogger;
import dev.dankom.operation.operations.ShutdownOperation;
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

        wallets.clear();
        createWallet("banker", Wallet.createPin(10), 0, 0, "Banker", "Admin");
        createWallet("andrea.gayed", Wallet.createPin(10), 710, 0, "Teacher", "Admin");
        createWallet("danylo.k", Wallet.createPin(5), 710, 14, "Developer", "Admin");
        for (Object o : ((JSONArray) new JsonFile(new File("./"), "secret").get().get("students"))) {
            JSONObject jo = (JSONObject) o;
            createWallet((String) jo.get("username"), (String) jo.get("pin"), ((Long) jo.get("room")).intValue(), ((Long) jo.get("studentNumber")).intValue(), "Student");
        }
//        addFunds(getWallet("andrea.gayed"), CoinUtil.mineCoin(difficulty, 10000).toArray(new Coin[]{}));

        new ShutdownOperation(new ThreadMethodRunner(() -> save()), "Save", logger);
    }

    public static Wallet getWallet(String username) {
        for (Wallet w : wallets) {
            if (w.getUsername().equalsIgnoreCase(username)) return w;
        }
        return null;
    }

    public static Wallet getWallet(String username, int roomNumber, int studentNumber) {
        for (Wallet w : wallets) {
            if (w.getUsername().equalsIgnoreCase(username) && w.getHomeroom() == roomNumber && w.getStudentNumber() == studentNumber)
                return w;
        }
        return null;
    }

    public static Wallet getWallet(PublicKey key) {
        for (Wallet w : wallets) {
            if (EncodingUtil.hexFromBytes(w.publicKey.getEncoded()).equals(EncodingUtil.hexFromBytes(key.getEncoded())))
                return w;
        }
        return null;
    }

    public static void createWallet(String username, String pin, int homeroom, int studentNumber, String... jobs) {
        if (getWallet(username) == null) {
            wallets.add(new Wallet(username, pin, homeroom, studentNumber, DataStructureAdapter.arrayToList(jobs)));
        }
    }

    public static void sendFunds(Wallet sender, Wallet recipient, Coin... coins) {
        if (!sender.getUsername().equals("banker") && recipient.getBalance().containsAll(DataStructureAdapter.arrayToList(coins))) {
            logger.error("BlockChain", "#Insufficient Funds");
            return;
        }

        if (sender.getUsername().equals("banker")) {
            logger.info("BlockChain", "Added " + coins.length + " coin(s) to " + EncodingUtil.hexFromBytes(recipient.publicKey.getEncoded()));
        } else {
            logger.info("BlockChain", "Sent " + coins.length + " coin(s) to " + EncodingUtil.hexFromBytes(recipient.publicKey.getEncoded()));
        }

        if (hasGenesis()) {
            createBlock(blockchain.get(blockchain.size() - 1).hash, sender, recipient, coins);
        } else {
            logger.info("BlockChain", "Created genesis transaction");
            createBlock("0", sender, recipient, coins);
        }
    }

    public static void addFunds(Wallet w, Coin... coins) {
        for (Block b : blockchain) {
            if (b.isSender(getWallet("banker").publicKey)) {
                for (Coin c : coins) {
                    if (b.coins.contains(c)) return;
                }
            }
        }
        sendFunds(getWallet("banker"), w, coins);
    }

    public static void createBlock(String hash, Wallet sender, Wallet recipient, Coin... coins) {
        Block b = new Block(hash, sender.publicKey, recipient.publicKey, DataStructureAdapter.arrayToList(coins));
        addBlock(b);
    }

    public static boolean hasGenesis() {
        for (Block b : blockchain) {
            if (b.previousHash.equalsIgnoreCase("0")) return true;
        }
        return false;
    }

    public static void addBlock(Block b) {
        blockchain.add(b);
        if (!isChainValid()) {
            blockchain.remove(b);
        }
    }

    public static boolean isChainValid() {
        return true;
    }

    public void load() {
        JsonFile json = fileManager.database;
        for (Object o : (JSONArray) json.get().get("blockchain"))
            blockchain.add(JSONUtil.deserializeBlock((JSONObject) o));
        for (Object o : (JSONArray) json.get().get("wallets")) wallets.add(JSONUtil.deserializeWallet((JSONObject) o));
    }

    public void save() {
        try {
            FileUtils.forceDelete(new File(new Directory("./coin"), "database.json"));
        } catch (IOException e) {
        }

        JsonObjectBuilder builder = new JsonObjectBuilder();
        List<JSONObject> blockchain = new ArrayList<>();
        for (Block b : BlockChain.blockchain) {
            blockchain.add(JSONUtil.buildBlock(b));
        }
        builder.addArray("blockchain", blockchain);
        List<JSONObject> wallets = new ArrayList<>();
        for (Wallet w : BlockChain.wallets) {
            wallets.add(JSONUtil.buildWallet(w));
        }
        builder.addArray("wallets", wallets);

        JSONObject json = builder.build();
        new JsonFile(new Directory("./coin"), fileManager.database.getName().replace(".json", ""), json);
    }
}