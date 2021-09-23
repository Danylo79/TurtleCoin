package dev.dankom.cc.chain.wallet;

import dev.dankom.cc.chain.BlockChain;
import dev.dankom.cc.chain.block.Block;
import dev.dankom.cc.chain.coin.Coin;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.List;

public class Wallet {
    private final String username;
    private final String pin;
    private final int homeroom;
    private final int studentNumber;
    private final List<String> jobs;
    public PrivateKey privateKey;
    public PublicKey publicKey;

    public Wallet(String username, String pin, int homeroom, int studentNumber, List<String> jobs) {
        this.username = username;
        this.pin = pin;
        this.homeroom = homeroom;
        this.studentNumber = studentNumber;
        this.jobs = jobs;
        generateKeyPair();
    }

    public Wallet(String username, String pin, int homeroom, int studentNumber, List<String> jobs, PrivateKey privateKey, PublicKey publicKey) {
        this.username = username;
        this.pin = pin;
        this.jobs = jobs;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.homeroom = homeroom;
        this.studentNumber = studentNumber;
    }

    public void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Coin> getBalance() {
        List<Coin> coins = new ArrayList<>();
        for (Block b : BlockChain.blockchain) {
            for (Coin c : b.coins) {
                if (b.isRecipient(publicKey)) {
                    coins.add(c);
                } else if (b.isSender(publicKey)) {
                    coins.remove(c);
                }
            }
        }
        return coins;
    }

    public String getUsername() {
        return username;
    }

    public String getPin() {
        return pin;
    }

    public int getHomeroom() {
        return homeroom;
    }

    public int getStudentNumber() {
        return studentNumber;
    }

    public List<String> getJobs() {
        return jobs;
    }
}