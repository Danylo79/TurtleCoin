package dev.dankom.cc.chain.wallet;

import dev.dankom.cc.chain.BlockChain;
import dev.dankom.cc.chain.coin.Coin;
import dev.dankom.cc.chain.wallet.transaction.Transaction;
import dev.dankom.cc.chain.wallet.transaction.TransactionInput;
import dev.dankom.cc.chain.wallet.transaction.TransactionOutput;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet {
    private final String username;
    private final String pin;
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    public Wallet(String username, String pin) {
        this.username = username;
        this.pin = pin;
        generateKeyPair();
    }

    public Wallet(String username, String pin, PrivateKey privateKey, PublicKey publicKey) {
        this.username = username;
        this.pin = pin;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Coin> getBalance() {
        List<Coin> total = new ArrayList<>();
        for (Map.Entry<String, TransactionOutput> item : BlockChain.UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            if (UTXO.isMine(publicKey)) {
                UTXOs.put(UTXO.id, UTXO);
                for (Coin c : UTXO.value) {
                    total.add(c);
                }
            }
        }
        return total;
    }

    public Transaction sendFunds(PublicKey recipient, List<Coin> value) {
        if (getBalance().size() < value.size() || value.isEmpty()) {
            BlockChain.logger.error("BlockChain", "#Insufficient Funds!");
            return null;
        }

        ArrayList<TransactionInput> inputs = new ArrayList<>();

        List<Coin> total = new ArrayList<>();
        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            for (Coin c : UTXO.value) {
                total.remove(c);
            }
            inputs.add(new TransactionInput(UTXO.id));
            if (total.size() > value.size()) break;
        }

        Transaction newTransaction = new Transaction(publicKey, recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input : inputs) {
            UTXOs.remove(input.transactionOutputId);
        }

        BlockChain.logger.info("BlockChain", "Sent " + value.size() + " coin(s) to " + recipient);

        return newTransaction;
    }

    public Transaction addFunds(List<Coin> value) {
        if (getBalance().size() < value.size() || value.isEmpty()) {
            BlockChain.logger.error("BlockChain", "#Insufficient Funds!");
            return null;
        }

        ArrayList<TransactionInput> inputs = new ArrayList<>();

        List<Coin> total = new ArrayList<>();
        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            for (Coin c : UTXO.value) {
                total.remove(c);
            }
            inputs.add(new TransactionInput(UTXO.id));
            if (total.size() > value.size()) break;
        }

        Transaction newTransaction = new Transaction(BlockChain.getWallet("Banker").publicKey, publicKey, value, inputs);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input : inputs) {
            UTXOs.remove(input.transactionOutputId);
        }

        BlockChain.logger.info("BlockChain", "Added " + value.size() + " coin(s) to " + publicKey);

        return newTransaction;
    }

    public String getUsername() {
        return username;
    }

    public String getPin() {
        return pin;
    }
}