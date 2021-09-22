package dev.dankom.cc.chain.wallet.transaction;

import dev.dankom.cc.chain.coin.Coin;
import dev.dankom.cc.util.HashUtil;
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

    public TransactionOutput(PublicKey recipient, List<Coin> value, String parentTransactionId, String id) {
        this.recipient = recipient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = id;
    }

    public boolean isMine(PublicKey publicKey) {
        return HashUtil.hexFromBytes(publicKey.getEncoded()).equals(HashUtil.hexFromBytes(recipient.getEncoded()));
    }
}
