package dev.dankom.cc.chain;

import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.coin.Coin;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.cc.chain.wallet.transaction.Transaction;
import dev.dankom.cc.chain.wallet.transaction.TransactionInput;
import dev.dankom.cc.chain.wallet.transaction.TransactionOutput;
import dev.dankom.cc.file.FileManager;
import dev.dankom.cc.util.CoinUtil;
import dev.dankom.cc.util.HashUtil;
import dev.dankom.cc.util.KeyUtil;
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
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        Coin c = new Coin();
        while (!c.isValid()) {
            c.mineBlock(3);
        }

        wallets = new ArrayList<>();
        wallets.add(new Wallet("Dankom", "9001", 710, 15, DataStructureAdapter.arrayToList("Admin")));
        wallets.add(new Wallet("Banker", "9000", 0, 0, DataStructureAdapter.arrayToList("Banker")));
        Wallet dankom = getWallet("Dankom");
        Wallet banker = getWallet("Banker");

        addFunds(dankom, DataStructureAdapter.arrayToList(c));
        sendFunds(dankom, banker, dankom.getBalance());

        new ShutdownOperation(new ThreadMethodRunner(() -> save()), "Save", logger);
    }

    public static Wallet getWallet(String username) {
        for (Wallet w : wallets) {
            if (w.getUsername().equalsIgnoreCase(username)) return w;
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
        String prevHash = null;
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
        String prevHash = null;
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
                return false;
            }
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                return false;
            }
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                return false;
            }

            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if (!currentTransaction.verifySignature()) {
                    return false;
                }
                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
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
                    return false;
                }
                if (currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
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
            wallets.add(new JsonObjectBuilder()
                    .addKeyValuePair("username", w.getUsername())
                    .addKeyValuePair("pin", w.getPin())
                    .addKeyValuePair("homeroom", w.getHomeroom())
                    .addKeyValuePair("studentNumber", w.getStudentNumber())
                    .addKeyValuePair("jobs", w.getJobs())
                    .addKeyValuePair("public", KeyUtil.toJson(w.publicKey))
                    .addKeyValuePair("private", KeyUtil.toJson(w.privateKey))
                    .build());
        }
        builder.addArray("wallets", wallets);

        JSONArray blockchain = new JSONArray();
        for (Block b : BlockChain.blockchain) {
            Transaction t = b.transactions.get(0);
            blockchain.add(new JsonObjectBuilder()
                    .addKeyValuePair("hash", b.hash)
                    .addKeyValuePair("previousHash", b.previousHash)
                    .addKeyValuePair("merkleRoot", b.merkleRoot)
                    .addKeyValuePair("timeStamp", b.timeStamp)
                    .addKeyValuePair("nonce", b.nonce)
                    .addKeyValuePair("transaction", new JsonObjectBuilder()
                            .addKeyValuePair("transactionId", t.transactionId)
                            .addKeyValuePair("sender", KeyUtil.toJson(t.sender))
                            .addKeyValuePair("recipient", KeyUtil.toJson(t.recipient))
                            .addKeyValuePair("signature", HashUtil.hexFromBytes(t.signature))
                            .addArray("coins", t.getCoins())
                            .addArray("inputs", ((Returner<List<JSONObject>>) () -> {
                                JSONArray array = new JSONArray();
                                if (t.getInputs() != null && !t.getInputs().isEmpty()) {
                                    for (TransactionInput ti : t.getInputs()) {
                                        JsonObjectBuilder inputBuilder = new JsonObjectBuilder();
                                        JsonObjectBuilder outputBuilder = new JsonObjectBuilder();
                                        inputBuilder.addKeyValuePair("transactionOutputId", ti.transactionOutputId);
                                        outputBuilder.addKeyValuePair("id", ti.UTXO.id);
                                        outputBuilder.addKeyValuePair("recipient", KeyUtil.toJson(ti.UTXO.recipient));
                                        outputBuilder.addKeyValuePair("parentTransactionId", ti.UTXO.parentTransactionId);
                                        outputBuilder.addArray("coins", CoinUtil.toHashes(ti.UTXO.value));
                                        inputBuilder.addKeyValuePair("output", ti.UTXO);
                                        array.add(inputBuilder.build());
                                    }
                                }
                                return array;
                            }).returned())
                            .build())
                    .build());
        }
        builder.addArray("blockchain", blockchain);

        new JsonFile(new Directory("./coin"), "blockchain", builder.build());
    }

    public void load() {
        JsonFile json = new JsonFile(new Directory("./coin"), "blockchain");
        for (Object o : (JSONArray) json.get().get("wallets")) {
            JSONObject jo = (JSONObject) o;
            wallets.add(new Wallet((String) jo.get("username"), (String) jo.get("pin"), ((Long) jo.get("homeroom")).intValue(), ((Long) jo.get("studentNumber")).intValue(), (List<String>) jo.get("jobs"), KeyUtil.fromJsonPrivate((JSONObject) jo.get("private")), KeyUtil.fromJsonPublic((JSONObject) jo.get("public"))));
        }

        for (Object o : (JSONArray) json.get().get("blocks")) {
            JSONObject jo = (JSONObject) o;
            JSONObject transaction = (JSONObject) jo.get("transaction");
            blockchain.add(new Block(
                    (String) jo.get("hash"),
                    (String) jo.get("previousHash"),
                    (String) jo.get("merkleRoot"),
                    new Transaction((String) transaction.get("transactionId"), KeyUtil.fromJsonPublic((JSONObject) transaction.get("sender")), KeyUtil.fromJsonPublic((JSONObject) transaction.get("recipient")), CoinUtil.fromHashes((ArrayList<String>) transaction.get("coins")), ((Returner<ArrayList<TransactionInput>>) () -> {
                        ArrayList<TransactionInput> out = new ArrayList<>();
                        for (Object transo : (JSONArray) transaction.get("inputs")) {
                            JSONObject transj = (JSONObject) transo;
                            out.add(new TransactionInput((String) transj.get("transactionOutputId"), new TransactionOutput(KeyUtil.fromJsonPublic((JSONObject) transj.get("recipient")), CoinUtil.fromHashes((List<String>) transj.get("coins")), (String) transj.get("parentTransactionId"), (String) transj.get("id"))));
                        }
                        return out;
                    }).returned()),
                    (long) jo.get("timeStamp"),
                    ((Long) jo.get("nonce")).intValue()));
        }
    }
}
