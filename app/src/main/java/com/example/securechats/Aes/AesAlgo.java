package com.example.securechats.Aes;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static com.google.android.gms.common.util.Base64Utils.decode;
import static com.google.android.gms.common.util.Base64Utils.encode;

public class AesAlgo {

    public static final String ALGORITHM = "AES";
    public byte[] keyValue;

    public AesAlgo(String key) {
        keyValue = key.getBytes();
    }

    public String encrypt(String plainText) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(plainText.getBytes());
        String encryptedValue = encode(encVal);
        return encryptedValue;
    }

    public String decrypt(String cipherText) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decodedValue = decode(cipherText);
        byte[] decValue = c.doFinal(decodedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    private Key generateKey() throws Exception{
        Key key = new SecretKeySpec(keyValue, ALGORITHM);
        return key;
    }



}
