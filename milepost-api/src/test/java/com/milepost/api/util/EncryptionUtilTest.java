package com.milepost.api.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.util.Map;

/**
 * Created by Ruifu Hua on 2020/2/2.
 */
public class EncryptionUtilTest {

    @Test
    public void test1() throws UnsupportedEncodingException {
        System.out.println(EncryptionUtil.encodeWithBase64("base64"));
    }

    @Test
    public void test2(){
        String str = "test1";
        byte[] strArray = str.getBytes();
        //System.out.println(EncryptionUtil.byteArrayToString(strArray));
    }


    /**
     * 基于密钥的对称加密
     * @throws Exception
     */
    @Test
    public void test3() throws Exception {
        String src = "哈哈哈哈";
        SecretKey key = EncryptionUtil.generateSecretKey();

        String filePath = "F:\\testFile\\secret.key";
        EncryptionUtil.saveSecretKey(key, filePath);


        SecretKey key1 = EncryptionUtil.loadSecretKey(filePath);
        byte[] encrypt = EncryptionUtil.aesEncrypt(src, key1);
        System.out.println(EncryptionUtil.aesDecrypt(encrypt, key1));
    }

    /**
     * 基于口令的对称加密
     * @throws Exception
     */
    @Test
    public void test4() throws Exception {
        String src = "哈哈哈哈";
        byte[] bytes = EncryptionUtil.pbeEncrypt(src, "aaa");
        System.out.println(EncryptionUtil.pbeDecrypt(bytes, "aaa"));
    }

    /**
     * 非对称加密，公钥加密私钥解密
     * @throws Exception
     */
    @Test
    public void test5() throws Exception {
        String src = "哈哈哈哈";
        Map<String, RSAKey> secretKeyPairMap = EncryptionUtil.generateSecretKeyPairMap();
        PublicKey publicKey = (PublicKey)secretKeyPairMap.get(EncryptionUtil.RSA_PUBLIC_KEY);
        PrivateKey privateKey = (PrivateKey)secretKeyPairMap.get(EncryptionUtil.RSA_PRIVATE_KEY);

        String secretKeyPairDirPath = "F:\\testFile";
        EncryptionUtil.saveSecretKeyPair(secretKeyPairMap, secretKeyPairDirPath);

        PublicKey publicKey1 = EncryptionUtil.loadPublicKey(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PUBLIC_KEY_FILE_NAME);
        PrivateKey privateKey1 = EncryptionUtil.loadPrivateKey(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PRIVATE_KEY_FILE_NAME);

        byte[] encrypt = EncryptionUtil.rsaEncryptByPrivateKey(src, privateKey1);
        System.out.println(EncryptionUtil.rsaDecryptByPublicKey(encrypt, publicKey1));

    }

    /**
     * 非对称加密，公钥加密私钥解密
     * @throws Exception
     */
    @Test
    public void test55() throws Exception {
        String src = "哈哈哈哈";
        Map<String, RSAKey> secretKeyPairMap = EncryptionUtil.generateSecretKeyPairMap();
        PublicKey publicKey = (PublicKey)secretKeyPairMap.get(EncryptionUtil.RSA_PUBLIC_KEY);
        PrivateKey privateKey = (PrivateKey)secretKeyPairMap.get(EncryptionUtil.RSA_PRIVATE_KEY);

        String secretKeyPairDirPath = "F:\\testFile";
        EncryptionUtil.saveSecretKeyPair(secretKeyPairMap, secretKeyPairDirPath);

        PublicKey publicKey1 = EncryptionUtil.loadPublicKey(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PUBLIC_KEY_FILE_NAME);
        PrivateKey privateKey1 = EncryptionUtil.loadPrivateKey(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PRIVATE_KEY_FILE_NAME);

        byte[] encrypt = EncryptionUtil.rsaEncryptByPublicKey(src, publicKey1);
        System.out.println(EncryptionUtil.rsaDecryptByPrivateKey(encrypt, privateKey1));

    }

    /**
     * 非对称加密，私钥加密公钥解密，常用于数字签名，使用私钥签名，使用公钥验签。
     * @throws Exception
     */
    @Test
    public void test6() throws Exception {
        String src = "哈哈哈哈";
//        Map<String, RSAKey> secretKeyPairMap = EncryptionUtil.generateSecretKeyPairMap();
//
        String secretKeyPairDirPath = "F:\\testFile";
//        EncryptionUtil.saveSecretKeyPair(secretKeyPairMap, secretKeyPairDirPath);

        byte[] privateKeyBytes = FileUtils.readFileToByteArray(new File(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PRIVATE_KEY_FILE_NAME));
        byte[] publicKeyBytes = FileUtils.readFileToByteArray(new File(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PUBLIC_KEY_FILE_NAME));

        byte[] encrypt = EncryptionUtil.rsaEncryptByPrivateKey(src, privateKeyBytes);
        System.out.println(EncryptionUtil.rsaDecryptByPublicKey(encrypt, publicKeyBytes));
    }

    /**
     * 非对称加密，私钥加密公钥解密，常用于数字签名，使用私钥签名，使用公钥验签。
     * @throws Exception
     */
    @Test
    public void test66() throws Exception {
        String src = "哈哈哈哈";
//        Map<String, RSAKey> secretKeyPairMap = EncryptionUtil.generateSecretKeyPairMap();

        String secretKeyPairDirPath = "F:\\testFile";
//        EncryptionUtil.saveSecretKeyPair(secretKeyPairMap, secretKeyPairDirPath);

        byte[] privateKeyBytes = FileUtils.readFileToByteArray(new File(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PRIVATE_KEY_FILE_NAME));
        byte[] publicKeyBytes = FileUtils.readFileToByteArray(new File(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PUBLIC_KEY_FILE_NAME));


        byte[] encrypt = EncryptionUtil.rsaEncryptByPublicKey(src, publicKeyBytes);
        System.out.println(EncryptionUtil.rsaDecryptByPrivateKey(encrypt, privateKeyBytes));
    }

    /**
     * sha256Encrypt
     * @throws NoSuchAlgorithmException
     */
    @Test
    public void test7() throws NoSuchAlgorithmException {
        System.out.println(EncryptionUtil.sha256Encrypt("sha256"));
        //5d5b09f6dcb2d53a5fffc60c4ac0d55fabdf556069d6631545f42aa6e3500f2e
    }

    /**
     * 使用java代码生成的密钥对签名和验签
     * @throws Exception
     */
    @Test
    public void test8() throws Exception {
        String src = "哈哈哈哈";
        Map<String, RSAKey> secretKeyPairMap = EncryptionUtil.generateSecretKeyPairMap();
        PublicKey publicKey = (PublicKey)secretKeyPairMap.get(EncryptionUtil.RSA_PUBLIC_KEY);
        PrivateKey privateKey = (PrivateKey)secretKeyPairMap.get(EncryptionUtil.RSA_PRIVATE_KEY);

        String secretKeyPairDirPath = "F:\\testFile";
        EncryptionUtil.saveSecretKeyPair(secretKeyPairMap, secretKeyPairDirPath);

        PublicKey publicKey1 = EncryptionUtil.loadPublicKey(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PUBLIC_KEY_FILE_NAME);
        PrivateKey privateKey1 = EncryptionUtil.loadPrivateKey(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PRIVATE_KEY_FILE_NAME);

        String sign = EncryptionUtil.sign(src.getBytes(), privateKey1);
        System.out.println("sign=" + sign);
        System.out.println(EncryptionUtil.verify((src+1).getBytes(), publicKey1, sign));
        System.out.println(EncryptionUtil.verify((src).getBytes(), publicKey1, sign));
    }

    /**
     * 使用java代码生成的密钥对签名和验签
     * @throws Exception
     */
    @Test
    public void test88() throws Exception {
        String src = "哈哈哈哈";
        Map<String, RSAKey> secretKeyPairMap = EncryptionUtil.generateSecretKeyPairMap();
//
        String secretKeyPairDirPath = "F:\\testFile";
        EncryptionUtil.saveSecretKeyPair(secretKeyPairMap, secretKeyPairDirPath);

        byte[] privateKeyBytes = FileUtils.readFileToByteArray(new File(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PRIVATE_KEY_FILE_NAME));
        byte[] publicKeyBytes = FileUtils.readFileToByteArray(new File(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PUBLIC_KEY_FILE_NAME));

        String sign = EncryptionUtil.sign(src.getBytes(), privateKeyBytes);
        System.out.println("sign=" + sign);
        System.out.println(EncryptionUtil.verify((src+1).getBytes(), publicKeyBytes, sign));
        System.out.println(EncryptionUtil.verify((src).getBytes(), publicKeyBytes, sign));
    }

    /**
     * 使用jdk生成的密钥对签名和验签
     * @throws Exception
     */
    @Test
    public void test9() throws Exception {
        String src = "哈哈哈哈";
        String secretKeyPairDirPath = "F:\\testFile";

        PublicKey publicKey1 = EncryptionUtil.loadPublicKey(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PUBLIC_KEY_FILE_NAME);
        PrivateKey privateKey1 = EncryptionUtil.loadPrivateKey(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PRIVATE_KEY_FILE_NAME);

        String sign = EncryptionUtil.sign(src.getBytes(), privateKey1);
        System.out.println("sign=" + sign);
        System.out.println(EncryptionUtil.verify((src+1).getBytes(), publicKey1, sign));
        System.out.println(EncryptionUtil.verify((src).getBytes(), publicKey1, sign));
    }

    /**
     * 使用jdk生成的密钥对签名和验签
     * @throws Exception
     */
    @Test
    public void test99() throws Exception {
        String src = "哈哈哈哈";
        String secretKeyPairDirPath = "F:\\testFile";

        byte[] privateKeyBytes = FileUtils.readFileToByteArray(new File(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PRIVATE_KEY_FILE_NAME));
        byte[] publicKeyBytes = FileUtils.readFileToByteArray(new File(secretKeyPairDirPath + File.separator + EncryptionUtil.RSA_PUBLIC_KEY_FILE_NAME));

        String sign = EncryptionUtil.sign(src.getBytes(), privateKeyBytes);
        System.out.println("sign=" + sign);
        System.out.println(EncryptionUtil.verify((src+1).getBytes(), publicKeyBytes, sign));
        System.out.println(EncryptionUtil.verify((src).getBytes(), publicKeyBytes, sign));
    }

    /**
     * IOUtils，FileUtils
     * @throws IOException
     */
    @Test
    public void test10() throws IOException {
        //输入流读取到字节数组中
        InputStream inputStream = new FileInputStream("");
        byte[] buffer = new byte[inputStream.available()];
        IOUtils.read(inputStream, buffer);

        //字节数字写入到输出流中
        OutputStream outputStream = new FileOutputStream("");
        IOUtils.write(buffer, outputStream);

        //字节数字写入到文件中
        File file = new File("");
        byte[] data = new byte[1];
        FileUtils.writeByteArrayToFile(file, data);

        //从文件中读取字节数组
        data = FileUtils.readFileToByteArray(file);
    }
}
