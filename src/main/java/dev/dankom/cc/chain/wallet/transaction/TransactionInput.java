package dev.dankom.cc.chain.wallet.transaction;

public class TransactionInput {
    public String transactionOutputId;
    public TransactionOutput UTXO;

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public TransactionInput(String transactionOutputId, TransactionOutput UTXO) {
        this.transactionOutputId = transactionOutputId;
        this.UTXO = UTXO;
    }
}
