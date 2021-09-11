package dev.dankom.cc.type;

import java.security.PublicKey;

public class EasyPublicKey implements PublicKey {
    private final String algorithm;
    private final String format;
    private final byte[] encoded;

    public EasyPublicKey(String algorithm, String format, byte[] encoded) {
        this.algorithm = algorithm;
        this.format = format;
        this.encoded = encoded;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public byte[] getEncoded() {
        return encoded;
    }
}
