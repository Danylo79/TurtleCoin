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
}
