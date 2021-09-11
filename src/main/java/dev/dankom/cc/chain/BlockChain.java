package dev.dankom.cc.chain;

import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.cc.chain.wallet.transaction.Transaction;
import dev.dankom.cc.chain.wallet.transaction.TransactionInput;
import dev.dankom.cc.chain.wallet.transaction.TransactionOutput;
import dev.dankom.cc.coin.Coin;
import dev.dankom.logger.LogManager;
import dev.dankom.logger.abztract.DefaultLogger;
import dev.dankom.logger.interfaces.ILogger;
import dev.dankom.util.general.DataStructureAdapter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlockChain {
    public static int difficulty;
    public static float minimumTransaction;
    public static final ILogger logger = LogManager.addLogger("BlockChain", new DefaultLogger());

    public static Wallet coinbase;
    public static ArrayList<Block> blockchain = new ArrayList<>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    public static Transaction genesisTransaction;

    public static void main(String[] arhs) {
        new BlockChain(3, 1.0f);
    }

    public BlockChain(int difficulty, float minimumTransaction) {
        Security.addProvider(new BouncyCastleProvider());
        this.difficulty = difficulty;
        this.minimumTransaction = minimumTransaction;

        this.coinbase = new Wallet("Coinbase");

        Wallet walletA = new Wallet("WalletA");
        Wallet walletB = new Wallet("WalletB");

        Coin c = new Coin(walletA).mineBlock(difficulty);
        sendFunds(walletA, walletB, DataStructureAdapter.arrayToList(c));
        sendFunds(walletB, walletA, DataStructureAdapter.arrayToList(c));
    }

    public Block makeGenesisTransaction(Wallet sender, Wallet recipient, List<Coin> value) {
        logger.info("BlockChain", "Created genesis block");
        genesisTransaction = new Transaction(sender.publicKey, recipient.publicKey, value, null);
        genesisTransaction.generateSignature(coinbase.privateKey);
        genesisTransaction.transactionId = "0";
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value, genesisTransaction.transactionId));
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);
        return genesis;
    }

    public Block sendFunds(Wallet sender, Wallet recipient, List<Coin> value) {
        String prevHash = null;
        try {
            prevHash = blockchain.get(blockchain.size() - 1).hash;
        } catch (IndexOutOfBoundsException e) {}
        return sendFunds(sender, recipient, value, prevHash);
    }

    public Block sendFunds(Wallet sender, Wallet recipient, List<Coin> value, String prevHash) {
        if (genesisTransaction == null) {
            return makeGenesisTransaction(sender, recipient, value);
        } else {
            Block block = new Block(prevHash);
            block.addTransaction(sender.sendFunds(recipient.publicKey, value));
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
        if (isChainValid()) {
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
}
