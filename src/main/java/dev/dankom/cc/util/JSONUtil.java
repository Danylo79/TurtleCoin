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
    public static JSONObject buildTransaction(Transaction transaction) {
        return new JsonObjectBuilder()
                .addKeyValuePair("transactionId", transaction.transactionId)
                .addKeyValuePair("sender", KeyUtil.toJson(transaction.sender))
                .addKeyValuePair("recipient", KeyUtil.toJson(transaction.recipient))
                .addKeyValuePair("signature", HashUtil.hexFromBytes(transaction.signature))
                .addArray("coins", transaction.getCoins())
                .addArray("inputs", ((Returner<List<JSONObject>>) () -> {
                    JSONArray array = new JSONArray();
                    if (transaction.getInputs() != null && !transaction.getInputs().isEmpty()) {
                        for (TransactionInput ti : transaction.getInputs()) {
                            array.add(buildTransactionInput(ti));
                        }
                    }
                    return array;
                }).returned())
                .addArray("outputs", ((Returner<List<JSONObject>>) () -> {
                    JSONArray array = new JSONArray();
                    if (transaction.getInputs() != null && !transaction.getInputs().isEmpty()) {
                        for (TransactionInput ti : transaction.getInputs()) {
                            array.add(buildTransactionOutput(ti.UTXO));
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

    public static Transaction deserializeTransaction(JSONObject json) {
        try {
            Transaction transaction = new Transaction((String) json.get("transactionId"),
                    KeyUtil.fromJsonPublic((JSONObject) json.get("sender")),
                    KeyUtil.fromJsonPublic((JSONObject) json.get("recipient")),
                    CoinUtil.fromHashes((ArrayList<String>) json.get("coins")),
                    HashUtil.hexToBytes((String) json.get("signature")), ((Returner<ArrayList<TransactionInput>>) () -> {
                ArrayList<TransactionInput> inputs = new ArrayList<>();
                for (Object transo : (JSONArray) json.get("inputs")) {
                    JSONObject transj = (JSONObject) transo;
                    JSONObject output = (JSONObject) ((JSONObject) transo).get("output");
                    inputs.add(deserializeTransactionInput(transj, output));
                }
                return inputs;
            }).returned());
            for (Object o : (JSONArray) json.get("outputs")) {
                JSONObject jo = (JSONObject) o;
                transaction.outputs.add(deserializeTransactionOutput(jo));
            }
            return transaction;
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
