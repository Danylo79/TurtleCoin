package dev.dankom.cc.chain.wallet.transaction;

import dev.dankom.cc.chain.BlockChain;
import dev.dankom.cc.chain.coin.Coin;
import dev.dankom.cc.util.StringUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private static int sequence = 0;
    public String transactionId;
    public PublicKey sender;
    public PublicKey recipient;
    public List<Coin> value;
    public byte[] signature;
    public ArrayList<TransactionInput> inputs;
    public ArrayList<TransactionOutput> outputs = new ArrayList<>();

    public Transaction(PublicKey from, PublicKey to, List<Coin> value, ArrayList<TransactionInput> inputs) {
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    public Transaction(String transactionId, PublicKey from, PublicKey to, List<Coin> value, ArrayList<TransactionInput> inputs) {
        this.transactionId = transactionId;
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;
    }

    public boolean processTransaction() {
        if (verifySignature() == false) {
            BlockChain.logger.error("BlockChain", "Signature is not valid");
            return false;
        }

        for (TransactionInput i : inputs) {
            i.UTXO = BlockChain.UTXOs.get(i.transactionOutputId);
        }

        List<Coin> leftOver = new ArrayList<>();
        for (int i = 0; i < (getInputsValue().size() - value.size()) - 1; i++) {
            leftOver.add(getInputsValue().get(i));
        }
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.recipient, value, transactionId));
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));

        for (TransactionOutput o : outputs) {
            BlockChain.UTXOs.put(o.id, o);
        }

        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue;
            BlockChain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value;
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value;
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    public List<Coin> getInputsValue() {
        List<Coin> total = new ArrayList<>();
        for (TransactionInput i : inputs) {
            if (i.UTXO == null) continue;
            for (Coin c : i.UTXO.value) {
                total.add(c);
            }
        }
        return total;
    }

    public List<Coin> getOutputsValue() {
        List<Coin> total = new ArrayList<>();
        for (TransactionOutput o : outputs) {
            for (Coin c : o.value) {
                total.add(c);
            }
        }
        return total;
    }

    public List<String> getCoins() {
        List<String> coins = new ArrayList<>();
        for (Coin c : value) {
            coins.add(c.getHash());
        }
        return coins;
    }

    private String calculateHash() {
        sequence++;
        return StringUtil.applySha256(StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + value + sequence);
    }
}
