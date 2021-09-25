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

    public static List<Coin> mineCoin(int difficulty, int amt) {
        List<Coin> coins = new ArrayList<>();
        for (int i = 0; i < amt; i++) {
            Coin c = new Coin();
            c.mine(difficulty);
            while (toHashes(coins).contains(c.getHash())) {
                c.mine(difficulty);
            }
            coins.add(c);
        }
        return coins;
    }
}
