package dev.dankom.cc.http.rest;

import dev.dankom.cc.chain.BlockChain;
import dev.dankom.cc.chain.wallet.Wallet;
import dev.dankom.cc.util.KeyUtil;
import dev.dankom.file.json.JsonObjectBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlockChainRest {
    @GetMapping("/echo/{s}")
    public String echo(@PathVariable String s) {
        return s;
    }

    @GetMapping("/wallets/get/{authCode}")
    public String getWallet(@PathVariable String authCode) {
        String[] split = authCode.split("-");
        System.out.println(split[0] + "-" + split[1] + "-" + split[2] + "-" + split[3]);
        Wallet w = BlockChain.getWallet(split[0], split[1], Integer.parseInt(split[2]), Integer.parseInt(split[3]));
        try {
            return new JsonObjectBuilder()
                    .addKeyValuePair("username", w.getUsername())
                    .addKeyValuePair("pin", w.getPin())
                    .addKeyValuePair("homeroom", w.getHomeroom())
                    .addKeyValuePair("studentNumber", w.getStudentNumber())
                    .addKeyValuePair("jobs", w.getJobs())
                    .addKeyValuePair("public", KeyUtil.toJson(w.publicKey))
                    .addKeyValuePair("private", KeyUtil.toJson(w.privateKey))
                    .build().toJSONString();
        } catch (NullPointerException e) {
            return "Failed: " + e.getMessage();
        }
    }
}
