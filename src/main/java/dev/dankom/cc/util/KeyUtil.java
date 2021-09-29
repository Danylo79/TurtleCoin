package dev.dankom.cc.util;

import dev.dankom.cc.type.KeyFactory;
import dev.dankom.file.json.JsonObjectBuilder;
import org.json.simple.JSONObject;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import static dev.dankom.cc.util.EncodingUtil.hexToBytes;
import static dev.dankom.cc.util.EncodingUtil.hexFromBytes;

public class KeyUtil {
    public static JSONObject toJson(Key key) {
        return new JsonObjectBuilder()
                .addKeyValuePair("algorithm", key.getAlgorithm())
                .addKeyValuePair("format", key.getFormat())
                .addKeyValuePair("encoded", hexFromBytes(key.getEncoded()))
                .build();
    }

    public static PrivateKey fromJsonPrivate(JSONObject json) {
        return KeyFactory.createPrivate((String) json.get("algorithm"), (String) json.get("format"), hexToBytes((String) json.get("encoded")));
    }

    public static PublicKey fromJsonPublic(JSONObject json) {
        return KeyFactory.createPublic((String) json.get("algorithm"), (String) json.get("format"), hexToBytes((String) json.get("encoded")));
    }
}
