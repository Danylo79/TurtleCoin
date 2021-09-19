package dev.dankom.cc.util;

import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.cc.chain.wallet.transaction.Transaction;
import dev.dankom.cc.chain.wallet.transaction.TransactionInput;
import dev.dankom.cc.chain.wallet.transaction.TransactionOutput;
import dev.dankom.file.json.JsonObjectBuilder;
import dev.dankom.type.returner.Returner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JSONUtil {
    public static JSONObject buildTransaction(Transaction genesisTransaction) {
        return new JsonObjectBuilder()
                .addKeyValuePair("transactionId", genesisTransaction.transactionId)
                .addKeyValuePair("sender", KeyUtil.toJson(genesisTransaction.sender))
                .addKeyValuePair("recipient", KeyUtil.toJson(genesisTransaction.recipient))
                .addKeyValuePair("signature", HashUtil.hexFromBytes(genesisTransaction.signature))
                .addArray("coins", genesisTransaction.getCoins())
                .addArray("inputs", ((Returner<List<JSONObject>>) () -> {
                    JSONArray array = new JSONArray();
                    if (genesisTransaction.getInputs() != null && !genesisTransaction.getInputs().isEmpty()) {
                        for (TransactionInput ti : genesisTransaction.getInputs()) {
                            array.add(buildTransactionInput(ti));
                        }
                    }
                    return array;
                }).returned())
                .build();
    }

    public static JSONObject buildTransactionInput(TransactionInput ti) {
        JsonObjectBuilder inputBuilder = new JsonObjectBuilder();
        inputBuilder.addKeyValuePair("transactionOutputId", ti.transactionOutputId);
        inputBuilder.addKeyValuePair("output", buildTransactionOutput(ti.UTXO));
        return inputBuilder.build();
    }

    public static JSONObject buildTransactionOutput(TransactionOutput ti) {
        return new JsonObjectBuilder()
                .addKeyValuePair("id", ti.id)
                .addKeyValuePair("parentTransactionId", ti.parentTransactionId)
                .addKeyValuePair("recipient", KeyUtil.toJson(ti.recipient))
                .addKeyValuePair("coins", CoinUtil.toHashes(ti.value))
                .build();
    }

    public static JSONObject buildWallet(Wallet w) {
        return new JsonObjectBuilder()
                .addKeyValuePair("username", w.getUsername())
                .addKeyValuePair("pin", w.getPin())
                .addKeyValuePair("homeroom", w.getHomeroom())
                .addKeyValuePair("studentNumber", w.getStudentNumber())
                .addKeyValuePair("jobs", w.getJobs())
                .addKeyValuePair("public", KeyUtil.toJson(w.publicKey))
                .addKeyValuePair("private", KeyUtil.toJson(w.privateKey))
                .build();
    }

    public static JSONObject buildBlock(Block b, Transaction t) {
        return new JsonObjectBuilder()
                .addKeyValuePair("hash", b.hash)
                .addKeyValuePair("previousHash", b.previousHash)
                .addKeyValuePair("merkleRoot", b.merkleRoot)
                .addKeyValuePair("timeStamp", b.timeStamp)
                .addKeyValuePair("nonce", b.nonce)
                .addKeyValuePair("transaction", buildTransaction(t))
                .build();
    }

    public static Block deserializeBlock(JSONObject jo, JSONObject transaction) {
        return new Block(
                (String) jo.get("hash"),
                (String) jo.get("previousHash"),
                (String) jo.get("merkleRoot"),
                deserializeTransaction(transaction),
                (long) jo.get("timeStamp"),
                ((Long) jo.get("nonce")).intValue());
    }

    public static Transaction deserializeTransaction(JSONObject transaction) {
        try {
            return new Transaction((String) transaction.get("transactionId"),
                    KeyUtil.fromJsonPublic((JSONObject) transaction.get("sender")),
                    KeyUtil.fromJsonPublic((JSONObject) transaction.get("recipient")),
                    CoinUtil.fromHashes((ArrayList<String>) transaction.get("coins")),
                    HashUtil.hexToBytes((String) transaction.get("signature")), ((Returner<ArrayList<TransactionInput>>) () -> {
                ArrayList<TransactionInput> out = new ArrayList<>();
                for (Object transo : (JSONArray) transaction.get("inputs")) {
                    JSONObject transj = (JSONObject) transo;
                    JSONObject output = (JSONObject) ((JSONObject) transo).get("output");
                    out.add(deserializeTransactionInput(transj, output));
                }
                return out;
            }).returned());
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static TransactionInput deserializeTransactionInput(JSONObject transj, JSONObject output) {
        return new TransactionInput((String) transj.get("transactionOutputId"),
                deserializeTransactionOutput(output));
    }

    public static TransactionOutput deserializeTransactionOutput(JSONObject output) {
        return new TransactionOutput(
                KeyUtil.fromJsonPublic((JSONObject) output.get("recipient")),
                CoinUtil.fromHashes((List<String>) output.get("coins")),
                (String) output.get("parentTransactionId"),
                (String) output.get("id"));
    }

    public static Wallet deserializeWallet(JSONObject jo) {
        return new Wallet((String) jo.get("username"),
                (String) jo.get("pin"),
                ((Long) jo.get("homeroom")).intValue(),
                ((Long) jo.get("studentNumber")).intValue(),
                (List<String>) jo.get("jobs"),
                KeyUtil.fromJsonPrivate((JSONObject) jo.get("private")),
                KeyUtil.fromJsonPublic((JSONObject) jo.get("public")));
    }

    public static JSONObject buildUTXO(Map.Entry<String, TransactionOutput> me) {
        return new JsonObjectBuilder()
                .addKeyValuePair("id", me.getKey())
                .addKeyValuePair("output", buildTransactionOutput(me.getValue()))
                .build();
    }
}
