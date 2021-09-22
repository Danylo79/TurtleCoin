package dev.dankom.cc.http.rest;

import dev.dankom.cc.chain.BlockChain;
import dev.dankom.cc.chain.wallet.Wallet;
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

@RestController
public class BlockChainRest {
    @GetMapping("/echo/{s}")
    public String echo(@PathVariable String s) {
        return s;
    }

    @GetMapping("/wallets/get/{authCode}")
    public String getWallet(@PathVariable String authCode) {
        String[] split = authCode.split("-");
        Wallet w = BlockChain.getWallet(split[0], split[1], Integer.parseInt(split[2]), Integer.parseInt(split[3]));
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
                    .build().toJSONString();
        } catch (NullPointerException e) {
            return "Failed: " + e.getMessage();
        }
    }

    @PostMapping("/auth/login")
    public void login(String returnUrl, String username, String pin, String roomNumber, String studentNumber, HttpServletResponse response) {
        Wallet wallet = BlockChain.getWallet(username, pin, Integer.parseInt(roomNumber), Integer.parseInt(studentNumber));
        if (wallet != null) {
            Cookie cookie = new Cookie("turtle-cookie", username);
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
}
