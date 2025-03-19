package com.fis.epm.utils;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class MobiFoneMoneyUtils {

    public static String getStringKey(String keyName) {
        String data = "";
        ClassPathResource cpr = new ClassPathResource(keyName);
        try {
            byte[] bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
            data = new String(bdata, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static RSAPrivateKey readPrivateKey(String strPrivateKey) throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");

        try (PemReader pemReader = new PemReader(new StringReader(strPrivateKey))) {
            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
            return (RSAPrivateKey) factory.generatePrivate(privKeySpec);
        }
    }

    public static String signDataBeforeSendRequest(String strSignData) throws Exception {
        String strPrivateKey = getStringKey("MBFMoney_private_key.pem");
        PrivateKey privKey = readPrivateKey(strPrivateKey);
        byte[] signedData = signatureData(strSignData.getBytes(), "SHA512withRSA", privKey);
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(signedData);
    }

    public static byte[] signatureData(byte[] data, String algorithm, PrivateKey privateKey) throws Exception {
        Signature rsa = Signature.getInstance(algorithm);
        rsa.initSign(privateKey);
        rsa.update(data);
        return rsa.sign();
    }


    public static RSAPublicKey readPublicKey(String strPublicKey) throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");

        try (StringReader keyReader = new StringReader(strPublicKey);
             PemReader pemReader = new PemReader(keyReader)) {

            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
            return (RSAPublicKey) factory.generatePublic(pubKeySpec);
        }
    }

    public boolean verify(String strPublicKey,String data, String signature) throws Exception {
        PublicKey publicKey = readPublicKey(strPublicKey);

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] byteSignature = decoder.decode(signature);

        Signature sig = Signature.getInstance("SHA512withRSA");
        sig.initVerify(publicKey);
        sig.update(data.getBytes());
        return sig.verify(signature.getBytes());
    }

    public static String encrypt3DES(String key, String data) throws Exception {
        Cipher cipher = Cipher.getInstance("TripleDES");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(key.getBytes(), 0, key.length());
        String keymd5 = new BigInteger(1, md5.digest()).toString(16).substring(0, 24);
        SecretKeySpec keyspec = new SecretKeySpec(keymd5.getBytes(), "TripleDES");
        cipher.init(Cipher.ENCRYPT_MODE, keyspec);
        byte[] stringBytes = data.getBytes();
        byte[] raw = cipher.doFinal(stringBytes);
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(raw);
    }

    public static String decrypt3DES(String key, String data) throws Exception {
        Cipher cipher = Cipher.getInstance("TripleDES");
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(key.getBytes(), 0, key.length());
        String keymd5 = new BigInteger(1, md5.digest()).toString(16).substring(0, 24);
        SecretKeySpec keyspec = new SecretKeySpec(keymd5.getBytes(), "TripleDES");
        cipher.init(Cipher.DECRYPT_MODE, keyspec);

        Base64.Decoder decoder = Base64.getDecoder();
        byte[] raw = decoder.decode(data);
        byte[] stringBytes = cipher.doFinal(raw);
        return new String(stringBytes, StandardCharsets.UTF_8);
    }



}
