package dev.dankom.cc.type;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface KeyFactory {
    static PublicKey createPublic(String algorithm, String format, byte[] encoded) {
        return new PublicKey() {
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
        };
    }

    static PrivateKey createPrivate(String algorithm, String format, byte[] encoded) {
        return new PrivateKey() {
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
        };
    }
}
