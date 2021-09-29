package dev.dankom.cc.util;

import dev.dankom.cc.chain.BlockChain;
import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.file.json.JsonObjectBuilder;
import org.json.simple.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BlockChainUtil {
    public static List<JSONObject> getTransactions(Wallet w) {
        List<JSONObject> out = new ArrayList<>();
        List<JSONObject> temp = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss", Locale.ENGLISH);
        for (Block b : BlockChain.blockchain) {
            if (b.isSender(w.publicKey) || b.isRecipient(w.publicKey)) {
                JSONObject build = new JsonObjectBuilder()
                        .addKeyValuePair("entity", (b.isSender(w.publicKey) ? BlockChain.getWallet(b.recipient).getUsername() : BlockChain.getWallet(b.sender).getUsername()))
                        .addKeyValuePair("coins", b.coins.size())
                        .addKeyValuePair("timestamp", b.timeStamp)
                        .addKeyValuePair("formattedTimeStamp", df.format(new Date(b.timeStamp)))
                        .addKeyValuePair("type", (b.isSender(w.publicKey) ? "outbound" : "inbound"))
                        .build();
                temp.add(build);
            }
        }
        while (!temp.isEmpty()) {
            JSONObject latest = temp.get(0);
            for (JSONObject transaction : temp) {
                if (((Long) transaction.get("timestamp")) > ((Long) latest.get("timestamp"))) {
                    latest = transaction;
                }
            }
            temp.remove(latest);
            out.add(latest);
        }
        return out;
    }
}
