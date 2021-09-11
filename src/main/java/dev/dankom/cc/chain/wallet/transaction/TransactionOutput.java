package dev.dankom.cc.chain.wallet.transaction;

import dev.dankom.cc.coin.Coin;
import dev.dankom.cc.util.StringUtil;

import java.security.PublicKey;
import java.util.List;

public class TransactionOutput {
    public String id;
    public PublicKey recipient;
    public List<Coin> value;
    public String parentTransactionId;

    //Constructor
    public TransactionOutput(PublicKey recipient, List<Coin> value, String parentTransactionId) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(recipient) + value + parentTransactionId);
    }

    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }
}
