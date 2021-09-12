package dev.dankom.cc.http.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlockChainRest {
    @GetMapping("/echo/{s}")
    public String echo(@PathVariable String s) {
        return s;
    }
}
