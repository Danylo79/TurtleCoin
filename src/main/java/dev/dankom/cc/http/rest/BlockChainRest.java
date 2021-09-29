package dev.dankom.cc.http.rest;

import dev.dankom.cc.chain.BlockChain;
import dev.dankom.cc.chain.coin.Coin;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.cc.util.BlockChainUtil;
import dev.dankom.cc.util.CoinUtil;
import dev.dankom.cc.util.KeyUtil;
import dev.dankom.file.json.JsonObjectBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class BlockChainRest {
    @GetMapping("/echo/{s}")
    public String echo(@PathVariable String s) {
        return s;
    }

    @GetMapping("/wallets/get/{authCode}")
    public String getWallet(@PathVariable String authCode) {
        String[] split = authCode.split("-");
        Wallet w = BlockChain.getWallet(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        try {
            return new JsonObjectBuilder()
                    .addKeyValuePair("username", w.getUsername())
                    .addKeyValuePair("pin", w.getPin())
                    .addKeyValuePair("homeroom", w.getHomeroom())
                    .addKeyValuePair("studentNumber", w.getStudentNumber())
                    .addArray("jobs", w.getJobs())
                    .addArray("coins", CoinUtil.toHashes(w.getBalance()))
                    .addKeyValuePair("public", KeyUtil.toJson(w.publicKey))
                    .addKeyValuePair("private", KeyUtil.toJson(w.privateKey))
                    .addArray("transactions", BlockChainUtil.getTransactions(w))
                    .build().toJSONString();
        } catch (NullPointerException e) {
            return "Failed: " + e.getMessage();
        }
    }

    @PostMapping("/auth/login")
    public void login(String returnUrl, String username, String pin, String roomNumber, String studentNumber, HttpServletResponse response) {
        Wallet wallet = BlockChain.getWallet(username, Integer.parseInt(roomNumber), Integer.parseInt(studentNumber));
        System.out.println(wallet.getPin());
        if (wallet.getPin().equalsIgnoreCase(pin)) {
            Cookie cookie = new Cookie("turtle-cookie", username + "-" + roomNumber + "-" + studentNumber);
            cookie.setPath("/");
            response.addCookie(cookie);
            try {
                response.sendRedirect(returnUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                response.sendError(403, "Failed to authenticate");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @PostMapping("/transact")
    public void transact(String returnUrl, String sender, String recipient, String amount, HttpServletResponse response) {
        Wallet w = BlockChain.getWallet(sender);
        if (w.getBalance().size() >= Integer.parseInt(amount)) {
            List<Coin> send = new ArrayList<>();
            for (int i = 0; i < Integer.parseInt(amount); i++) {
                Coin c = w.getBalance().get(i);
                send.add(c);
            }
            BlockChain.sendFunds(w, BlockChain.getWallet(recipient), send.toArray(new Coin[]{}));
        }
        try {
            response.sendRedirect(returnUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
