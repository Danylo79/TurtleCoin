package dev.dankom.cc.util;

import dev.dankom.cc.chain.BlockChain;
import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.coin.Coin;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.file.json.JsonObjectBuilder;
import org.json.simple.JSONObject;

import java.util.List;

public class JSONUtil {
    public static JSONObject buildBlock(Block b) {
        return new JsonObjectBuilder()
                .addKeyValuePair("hash", b.hash)
                .addKeyValuePair("previousHash", b.previousHash)
                .addKeyValuePair("timeStamp", b.timeStamp)
                .addKeyValuePair("nonce", b.nonce)
                .addKeyValuePair("sender", KeyUtil.toJson(b.sender))
                .addKeyValuePair("recipient", KeyUtil.toJson(b.recipient))
                .addKeyValuePair("coins", CoinUtil.toHashes(b.coins))
                .build();
    }

    public static Block deserializeBlock(JSONObject jo) {
        return new Block(
                (String) jo.get("hash"),
                (String) jo.get("previousHash"),
                (long) jo.get("timeStamp"),
                ((Long) jo.get("nonce")).intValue(),
                KeyUtil.fromJsonPublic((JSONObject) jo.get("sender")),
                KeyUtil.fromJsonPublic((JSONObject) jo.get("recipient")),
                CoinUtil.fromHashes((List<String>) jo.get("coins"))
        );
    }

    public static JSONObject buildWallet(Wallet w) {
        return new JsonObjectBuilder()
                .addKeyValuePair("public", KeyUtil.toJson(w.publicKey))
                .addKeyValuePair("private", KeyUtil.toJson(w.privateKey))
                .addKeyValuePair("homeroom", w.getHomeroom())
                .addKeyValuePair("studentNumber", w.getStudentNumber())
                .addKeyValuePair("username", w.getUsername())
                .addKeyValuePair("pin", w.getPin())
                .addKeyValuePair("jobs", w.getJobs())
                .build();
    }

    public static Wallet deserializeWallet(JSONObject jo) {
        BlockChain.logger.info("BlockChain", "Loading " + jo.get("username"));
        return new Wallet((String) jo.get("username"),
                (String) jo.get("pin"),
                ((Long) jo.get("homeroom")).intValue(),
                ((Long) jo.get("studentNumber")).intValue(),
                (List<String>) jo.get("jobs"),
                KeyUtil.fromJsonPrivate((JSONObject) jo.get("private")),
                KeyUtil.fromJsonPublic((JSONObject) jo.get("public")));
    }
}
