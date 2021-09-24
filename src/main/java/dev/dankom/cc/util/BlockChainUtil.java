package dev.dankom.cc.util;

import dev.dankom.cc.chain.BlockChain;
import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.file.json.JsonObjectBuilder;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BlockChainUtil {
    public static List<JSONObject> getInbounds(Wallet w) {
        List<JSONObject> out = new ArrayList<>();
        for (Block b : BlockChain.blockchain) {
            if (b.isRecipient(w.publicKey)) {
                out.add(new JsonObjectBuilder()
                        .addKeyValuePair("sender", BlockChain.getWallet(b.sender).getUsername())
                        .addKeyValuePair("coins", b.coins.size())
                        .build());
            }
        }
        return out;
    }

    public static List<JSONObject> getOutbounds(Wallet w) {
        List<JSONObject> out = new ArrayList<>();
        for (Block b : BlockChain.blockchain) {
            if (b.isSender(w.publicKey)) {
                out.add(new JsonObjectBuilder()
                        .addKeyValuePair("recipient", BlockChain.getWallet(b.recipient).getUsername())
                        .addKeyValuePair("coins", b.coins.size())
                        .build());
            }
        }
        return out;
    }
}
