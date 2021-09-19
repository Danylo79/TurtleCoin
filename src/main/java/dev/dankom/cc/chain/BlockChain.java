package dev.dankom.cc.chain;

import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.coin.Coin;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.cc.chain.wallet.transaction.Transaction;
import dev.dankom.cc.chain.wallet.transaction.TransactionInput;
import dev.dankom.cc.chain.wallet.transaction.TransactionOutput;
import dev.dankom.cc.file.FileManager;
import dev.dankom.cc.util.CoinUtil;
import dev.dankom.cc.util.JSONUtil;
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
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    public static Transaction genesisTransaction;
    public final FileManager fileManager = new FileManager();

    public BlockChain(int difficulty, float minimumTransaction) {
        load();

        Security.addProvider(new BouncyCastleProvider());
        BlockChain.difficulty = difficulty;
        BlockChain.minimumTransaction = minimumTransaction;

//        wallets.add(new Wallet("danylo.komisarenko", "oNAxLmav", 710, 14, DataStructureAdapter.arrayToList("Admin")));
//        wallets.add(new Wallet("banker", "dfshbfh", 0, 0, DataStructureAdapter.arrayToList("Banker")));
//        addFunds(getWallet("danylo.komisarenko"), DataStructureAdapter.arrayToList(CoinUtil.mineBlock(difficulty)));
        sendFunds(getWallet("danylo.komisarenko"), getWallet("banker"), DataStructureAdapter.arrayToList(CoinUtil.mineBlock(difficulty)));

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

    public Block makeGenesisTransaction(Wallet sender, Wallet recipient, List<Coin> value) {
        logger.info("BlockChain", "Created genesis block");
        genesisTransaction = new Transaction(sender.publicKey, recipient.publicKey, value, null);
        genesisTransaction.generateSignature(getWallet("Banker").privateKey);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);
        return genesis;
    }

    public Block addFunds(Wallet recipient, List<Coin> value) {
        String prevHash = "0";
        try {
            prevHash = blockchain.get(blockchain.size() - 1).hash;
        } catch (IndexOutOfBoundsException e) {
        }
        return addFunds(recipient, value, prevHash);
    }

    public Block addFunds(Wallet recipient, List<Coin> value, String prevHash) {
        if (genesisTransaction == null) {
            return makeGenesisTransaction(getWallet("Banker"), recipient, value);
        } else {
            Block block = new Block(prevHash);
            if (!block.addTransaction(recipient.addFunds(value))) {
                throw new Error("Failed to add transaction");
            }
            addBlock(block);
            if (!isChainValid()) {
                throw new Error("Failed to add block");
            }
            return block;
        }
    }

    public Block sendFunds(Wallet sender, Wallet recipient, List<Coin> value) {
        String prevHash = "0";
        try {
            prevHash = blockchain.get(blockchain.size() - 1).hash;
        } catch (IndexOutOfBoundsException e) {
        }
        return sendFunds(sender, recipient, value, prevHash);
    }

    public Block sendFunds(Wallet sender, Wallet recipient, List<Coin> value, String prevHash) {
        if (genesisTransaction == null) {
            return makeGenesisTransaction(sender, recipient, value);
        } else {
            Block block = new Block(prevHash);
            if (!block.addTransaction(sender.sendFunds(recipient.publicKey, value))) {
                throw new Error("Failed to add transaction");
            }
            addBlock(block);
            if (!isChainValid()) {
                throw new Error("Failed to add block");
            }
            return block;
        }
    }

    public void addBlock(Block block) {
        block.mineBlock(difficulty);
        blockchain.add(block);
        if (!isChainValid()) {
            blockchain.remove(block);
        }
    }

    public Coin mineCoin(Coin c) {
        return c.mineBlock(difficulty);
    }

    public Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
        for (int i = 1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                logger.error("BlockChain", "#Current Hashes not equal");
                return false;
            }
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                logger.error("BlockChain", "#Previous Hashes not equal");
                return false;
            }
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                logger.error("BlockChain", "#This block hasn't been mined");
                return false;
            }

            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if (!currentTransaction.verifySignature()) {
                    logger.error("BlockChain", "#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if (currentTransaction.getInputsValue().size() != currentTransaction.getOutputsValue().size()) {
                    logger.error("BlockChain", "#Referenced input Transaction(" + t + ") value is Invalid");
                    return false;
                }

                for (TransactionInput input : currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if (tempOutput == null) {
                        return false;
                    }

                    if (input.UTXO.value != tempOutput.value) {
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for (TransactionOutput output : currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if (currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
                    logger.error("BlockChain", "#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if (currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
                    logger.error("BlockChain", "#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }
            }
        }
        return true;
    }

    public void save() {
        try {
            FileUtils.forceDelete(new File(new Directory("./coin"), "blockchain.json"));
        } catch (IOException e) {
        }

        JsonObjectBuilder builder = new JsonObjectBuilder();
        JSONArray wallets = new JSONArray();
        for (Wallet w : BlockChain.wallets) {
            wallets.add(JSONUtil.buildWallet(w));
        }
        builder.addArray("wallets", wallets);

        JSONArray blockchain = new JSONArray();
        for (Block b : BlockChain.blockchain) {
            Transaction t = b.transactions.get(0);
            blockchain.add(JSONUtil.buildBlock(b, t));
        }
        builder.addArray("blockchain", blockchain);

        builder.addArray("UTXOs", ((Returner<List<JSONObject>>) () -> {
            List<JSONObject> out = new ArrayList<>();
            for (Map.Entry<String, TransactionOutput> me : UTXOs.entrySet()) {
                out.add(JSONUtil.buildUTXO(me));
            }
            return out;
        }).returned());

        builder.addKeyValuePair("genesisTransaction", JSONUtil.buildTransaction(genesisTransaction));

        new JsonFile(new Directory("./coin"), "blockchain", builder.build());
    }

    public void load() {
        JsonFile json = new JsonFile(new Directory("./coin"), "blockchain");
        for (Object o : (JSONArray) json.get().get("wallets")) {
            JSONObject jo = (JSONObject) o;
            wallets.add(JSONUtil.deserializeWallet(jo)
            );
        }

        for (Object o : (JSONArray) json.get().get("blockchain")) {
            JSONObject jo = (JSONObject) o;
            JSONObject transaction = (JSONObject) jo.get("transaction");
            blockchain.add(JSONUtil.deserializeBlock(jo, transaction));
        }

        for (Object o : (JSONArray) json.get().get("UTXOs")) {
            JSONObject jo = (JSONObject) o;
            UTXOs.put((String) jo.get("id"), JSONUtil.deserializeTransactionOutput((JSONObject) jo.get("output")));
        }


        genesisTransaction = JSONUtil.deserializeTransaction((JSONObject) json.get().get("genesisTransaction"));
    }
}