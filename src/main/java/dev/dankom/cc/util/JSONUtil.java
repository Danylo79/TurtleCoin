package dev.dankom.cc.util;

import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.coin.Coin;
import dev.dankom.file.json.JsonObjectBuilder;
import org.json.simple.JSONObject;

public class JSONUtil {
    public static JSONObject buildBlock(Block b) {
        return new JsonObjectBuilder()
                .addKeyValuePair("hash", b.hash)
                .addKeyValuePair("previousHash", b.previousHash)
                .addKeyValuePair("merkleRoot", b.merkleRoot)
                .addKeyValuePair("timeStamp", b.timeStamp)
                .addKeyValuePair("nonce", b.nonce)
                .addKeyValuePair("sender", KeyUtil.toJson(b.sender))
                .addKeyValuePair("recipient", KeyUtil.toJson(b.recipient))
                .addKeyValuePair("coin", b.coin.getHash())
                .build();
    }

    public static Block deserializeBlock(JSONObject jo) {
        return new Block(
                (String) jo.get("hash"),
                (String) jo.get("previousHash"),
                (String) jo.get("merkleRoot"),
                (long) jo.get("timeStamp"),
                ((Long) jo.get("nonce")).intValue(),
                KeyUtil.fromJsonPublic((JSONObject) jo.get("sender")),
                KeyUtil.fromJsonPublic((JSONObject) jo.get("recipient")),
                new Coin((String) jo.get("coin"))
        );
    }
}
