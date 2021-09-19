package dev.dankom.cc.util;

import dev.dankom.cc.chain.coin.Coin;

import java.util.ArrayList;
import java.util.List;

public class CoinUtil {
    public static List<Coin> fromHashes(List<String> coins) {
        List<Coin> out = new ArrayList<>();
        for (String c : coins) {
            out.add(new Coin(c));
        }
        return out;
    }

    public static List<String> toHashes(List<Coin> coins) {
        List<String> out = new ArrayList<>();
        for (Coin c : coins) {
            out.add(c.getHash());
        }
        return out;
    }

    public static Coin mineBlock(int difficulty) {
        Coin c = new Coin();
        while (!c.isValid()) {
            c.mineBlock(difficulty);
        }
        return c;
    }
}
