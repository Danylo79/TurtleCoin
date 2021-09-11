package dev.dankom.cc.chain.block;

import dev.dankom.cc.chain.transaction.Transaction;
import dev.dankom.cc.chain.transaction.TransactionInput;
import dev.dankom.cc.chain.transaction.TransactionOutput;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.cc.file.FileManager;
import dev.dankom.cc.type.EasyPublicKey;
import dev.dankom.file.json.JsonObjectBuilder;
import dev.dankom.logger.LogManager;
import dev.dankom.logger.abztract.DefaultLogger;
import dev.dankom.logger.interfaces.ILogger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlockChain {
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    public static final ILogger logger = LogManager.addLogger("ClassroomCoin", new DefaultLogger());
    public static final int difficulty = 3;
    public static final float minimumTransaction = 0.1f;

    private final Wallet pool;
    private final Wallet coinbase;

    private final FileManager fileManager;

    public ArrayList<Block> blockchain = new ArrayList<>();
    public Transaction genesisTransaction;

    public static void main(String[] args) {
        new BlockChain();
    }

    public BlockChain() {
        Security.addProvider(new BouncyCastleProvider());

        this.pool = new Wallet();
        this.coinbase = new Wallet();

        this.fileManager = new FileManager();

        init();
    }

    public void init() {
        if (((JSONArray) fileManager.blockchain.get().get("blocks")).isEmpty()) {
            addGenesisTransaction(pool, coinbase);
        } else {
            for (Object o : (JSONArray) fileManager.blockchain.get().get("blocks")) {
                JSONObject json = (JSONObject) o;
                List<Transaction> transactions = new ArrayList<>();
                for (Object object : (JSONArray) json.get("transactions")) {
                    JSONObject jo = (JSONObject) object;
                    transactions.add(new Transaction(new EasyPublicKey(
                            (String) (((JSONObject) jo.get("sender"))).get("algorithm"),
                            (String) (((JSONObject) jo.get("sender"))).get("format"),
                            new byte[]{((Long) (((JSONObject) jo.get("sender")).get("encoded"))).byteValue()}
                    ), new EasyPublicKey(
                            (String) (((JSONObject) jo.get("recipient"))).get("algorithm"),
                            (String) (((JSONObject) jo.get("recipient"))).get("format"),
                            new byte[]{((Long) (((JSONObject) jo.get("recipient")).get("encoded"))).byteValue()}
                    ), ((Double) jo.get("value")).floatValue(), new ArrayList<>()));
                }
                blockchain.add(new Block((String) json.get("hash"), (String) json.get("previousHash"), (String) json.get("merkleRoot"), transactions, (Long) json.get("timeStamp"), ((Long) json.get("nonce")).intValue()));
            }
        }
    }

    public Block sendFunds(Wallet wallet, PublicKey recipient, float value) {
        return sendFunds(wallet, recipient, value, blockchain.get(blockchain.size() - 1));
    }

    public Block sendFunds(Wallet wallet, PublicKey recipient, float value, Block prevBlock) {
        Block b = new Block(prevBlock.hash);
        b.addTransaction(wallet.sendFunds(recipient, value));
        addBlock(b);
        return b;
    }

    public void addGenesisTransaction(Wallet pool, Wallet coinbase) {
        genesisTransaction = new Transaction(coinbase.publicKey, pool.publicKey, 100000000f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        logger.info("ClassroomCoin", "Creating and Mining Genesis block");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);
    }

    public Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        for (int i = 1; i < blockchain.size(); i++) {

            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                logger.error("ClassroomCoin", "#Current Hashes not equal");
                return false;
            }
            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                logger.error("ClassroomCoin", "#Previous Hashes not equal");
                return false;
            }
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                logger.error("ClassroomCoin", "#This block hasn't been mined");
                return false;
            }

            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.transactions.size(); t++) {
                Transaction currentTransaction = currentBlock.transactions.get(t);

                if (!currentTransaction.verifySignature()) {
                    logger.error("ClassroomCoin", "#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    logger.error("ClassroomCoin", "#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for (TransactionInput input : currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if (tempOutput == null) {
                        logger.error("ClassroomCoin", "#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if (input.UTXO.value != tempOutput.value) {
                        logger.error("ClassroomCoin", "#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for (TransactionOutput output : currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if (currentTransaction.outputs.get(0).reciepient != currentTransaction.recipient) {
                    logger.error("ClassroomCoin", "#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if (currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
                    logger.error("ClassroomCoin", "#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }

        }
        return true;
    }

    public void addBlock(Block block) {
        block.mineBlock(difficulty);
        blockchain.add(block);
        if (isChainValid()) {
            JsonObjectBuilder builder = new JsonObjectBuilder();

            builder.addKeyValuePair("hash", block.hash);
            builder.addKeyValuePair("previousHash", block.previousHash);
            builder.addKeyValuePair("merkleRoot", block.merkleRoot);
            builder.addKeyValuePair("timeStamp", block.timeStamp);
            builder.addKeyValuePair("nonce", block.nonce);

            List<JSONObject> transactions = new ArrayList<>();
            for (Transaction transaction : block.transactions) {
                transactions.add(new JsonObjectBuilder()
                        .addKeyValuePair("transactionId", transaction.transactionId)
                        .addKeyValuePair("sender", new JsonObjectBuilder()
                                .addKeyValuePair("format", transaction.sender.getFormat())
                                .addKeyValuePair("algorithm", transaction.sender.getAlgorithm())
                                .addKeyValuePair("encoded", transaction.sender.getEncoded()[0])
                                .build())
                        .addKeyValuePair("recipient", new JsonObjectBuilder()
                                .addKeyValuePair("format", transaction.recipient.getFormat())
                                .addKeyValuePair("algorithm", transaction.recipient.getAlgorithm())
                                .addKeyValuePair("encoded", transaction.sender.getEncoded()[0])
                                .build())
                        .addKeyValuePair("value", transaction.value)
                        .build());
            }
            builder.addArray("transactions", transactions);
            fileManager.blockchain.addToArray("blocks", builder.build());
        }
    }
}
