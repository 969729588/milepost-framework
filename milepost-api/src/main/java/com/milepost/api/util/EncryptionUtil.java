package com.milepost.api.util;

import org.apache.commons.io.FileUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * https://www.cnblogs.com/duanxz/p/3195098.html
 *
 * 加密/解密
 *
 * @author Huarf
 *         2017年12月5日
 */
public class EncryptionUtil {

    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 基于口令的对称加密算法，
     * 甲乙双方使用同样的口令加密解密数据
     */
    public static final String PBE_ALGORITHM = "PBEWithMD5AndDES";
    /**
     * 基于口令的对称加密的默认口令
     */
    private static String DEFAULT_KEY_SPEC = "default_key_spec";

    /**
     * 基于密钥的对称加密算法，
     * 甲乙双方使用同一个密钥加密解密数据
     */
    public static final String AES_ALGORITHM = "AES";

    /**
     * 非对称加密算法，
     * 甲乙双方使用不同的密钥加密解密数据，甲方生成一对密钥，将公钥发布给多个乙方，自己保留私钥。
     * 用法一：
     *  乙方使用公钥加密数据，发给甲方，甲方使用私钥解密数据
     * 用法二：
     *  数字签名，甲方使用私钥对数据进行签名，发给乙方，乙方使用公钥解密数据并验证签名是否是甲方，验证数据是否被篡改过
     *
     */
    public static final String RSA_ALGORITHM = "RSA";

    /**
     * 非对称加密算法的密钥长度，DH算法的默认密钥长度是1024，
     * 密钥长度必须是64的倍数，在512到65536位之间。
     */
    private static final int RSA_KEY_SIZE = 512;

    /**
     * Map中公钥key
     */
    public static final String RSA_PUBLIC_KEY = "RSAPublicKey";
    /**
     * Map中私钥key
     */
    public static final String RSA_PRIVATE_KEY = "RSAPrivateKey";

    /**
     * 公钥文件名
     */
    public static final String RSA_PUBLIC_KEY_FILE_NAME = "public.key";
    /**
     * 私钥文件名
     */
    public static final String RSA_PRIVATE_KEY_FILE_NAME = "private.key";

    /**
     * SHA-256加密算法
     */
    public static final String SHA_256_ALGORITHM = "SHA-256";


    /**
     * 数字签名算法。
     * JDK只提供了MD2withRSA, MD5withRSA, SHA1withRSA，其他的算法需要第三方包才能支持。
     * 数字签名是基于RSA非对称加密实现的，
     */
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";


    /**
     * 生成基于密钥的对称加密的密钥，加密解密私用同一个密钥
     * @return
     * @throws Exception
     */
    public static SecretKey generateSecretKey() throws Exception {
        // 创建一个KeyGenerator
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
        // 生成秘钥
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    /**
     * 保存基于密钥的对称加密的密钥
     * @param key
     * @param secretKeyFilePath 是一个文件，一般以“.key”结尾，如“/aa/bb/secret.key”
     * @throws Exception
     */
//    public static void saveSecretKey(SecretKey key, String secretFilePath) throws Exception {
//        FileOutputStream fosKey = null;
//        ObjectOutputStream oosSecretKey = null;
//        try {
//            fosKey = new FileOutputStream(secretFilePath);
//            oosSecretKey = new ObjectOutputStream(fosKey);
//            oosSecretKey.writeObject(key);
//        }finally {
//            IOUtils.closeQuietly(oosSecretKey);
//            IOUtils.closeQuietly(fosKey);
//        }
//    }

//    public static void saveSecretKey(SecretKey key, String secretKeyFilePath) throws Exception {
//        OutputStream fosKey = null;
//        try {
//            fosKey = new FileOutputStream(secretKeyFilePath);
//            byte[] keyEncodedBytes = key.getEncoded();
//            //字节数组转换成base64字符串
//            String keyEncodedBase64 = EncryptionUtil.byteArr2Base64Str(keyEncodedBytes);
//            byte[] keyEncodedBase64Bytes = keyEncodedBase64.getBytes();
//            fosKey.write(keyEncodedBase64Bytes);
//        }finally {
//            IOUtils.closeQuietly(fosKey);
//        }
//    }

    public static void saveSecretKey(SecretKey key, String secretKeyFilePath) throws Exception {
        byte[] keyEncodedBytes = key.getEncoded();
        //字节数组转换成base64字符串
        String keyEncodedBase64 = EncryptionUtil.byteArr2Base64Str(keyEncodedBytes);
        byte[] keyEncodedBase64Bytes = keyEncodedBase64.getBytes();
        FileUtils.writeByteArrayToFile(new File(secretKeyFilePath), keyEncodedBase64Bytes);
    }

    /**
     * 加载基于密钥的对称加密的密钥
     * @param secretFilePath
     * @return
     * @throws Exception
     */
//    public static SecretKey loadSecretKey(String secretFilePath) throws Exception {
//        FileInputStream fisKey = null;
//        ObjectInputStream oisSecretKey = null;
//        SecretKey secretKey = null;
//        try {
//            fisKey = new FileInputStream(secretFilePath);
//            oisSecretKey = new ObjectInputStream(fisKey);
//            secretKey = (SecretKey)oisSecretKey.readObject();
//        }finally {
//            IOUtils.closeQuietly(oisSecretKey);
//            IOUtils.closeQuietly(fisKey);
//        }
//        return secretKey;
//    }

//    public static SecretKey loadSecretKey(String secretFilePath) throws Exception {
//        InputStream fisKey = null;
//        SecretKey secretKey = null;
//        try {
//            fisKey = new FileInputStream(secretFilePath);
//            byte[] keyEncodedBase64Bytes = new byte[fisKey.available()];
//            fisKey.read(keyEncodedBase64Bytes, 0, fisKey.available());
//            String keyEncodedBase64 = new String(keyEncodedBase64Bytes);
//            //base64字符串转换成字节数组
//            byte[] keyEncodedBytes = EncryptionUtil.base64Str2ByteArr(keyEncodedBase64);
//            secretKey = new SecretKeySpec(keyEncodedBytes, AES_ALGORITHM);
//        }finally {
//            IOUtils.closeQuietly(fisKey);
//        }
//        return secretKey;
//    }

    public static SecretKey loadSecretKey(String secretFilePath) throws Exception {
        byte[] keyEncodedBase64Bytes = FileUtils.readFileToByteArray(new File(secretFilePath));
        String keyEncodedBase64 = new String(keyEncodedBase64Bytes);
        //base64字符串转换成字节数组
        byte[] keyEncodedBytes = EncryptionUtil.base64Str2ByteArr(keyEncodedBase64);
        SecretKey secretKey = new SecretKeySpec(keyEncodedBytes, AES_ALGORITHM);
        return secretKey;
    }

    /**
     * 基于密钥的对称加密
     * @param src
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] aesEncrypt(String src, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(src.getBytes());
    }

    /**
     * 基于密钥的对称解密
     * @param encrypt
     * @param key
     * @return
     * @throws Exception
     */
    public static String aesDecrypt(byte[] encrypt, SecretKey key) throws Exception{
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(encrypt));
    }


    /**
     * 基于口令的对称加密
     * 返回字节数组，可以使用bytes2HexString方法将字节数组转化成小写的16进制字符串
     *
     * @param src
     * @param keySpec
     * @return
     * @throws Exception
     */
    public static byte[] pbeEncrypt(String src, String keySpec) throws Exception {
        //实例化工具
        Cipher cipher = Cipher.getInstance(PBE_ALGORITHM);

        //使用该工具将基于密码的形式生成Key
        SecretKey key = SecretKeyFactory.getInstance(PBE_ALGORITHM).generateSecret(new PBEKeySpec(keySpec.toCharArray()));  //pwd
        PBEParameterSpec parameterspec = new PBEParameterSpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}, 2);  //salt

        //初始化加密操作，同时传递加密的算法
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterspec);

        //将要加密的数据传递进去，返回加密后的数据
        byte[] results = cipher.doFinal(src.getBytes());
        return results;
    }

    /**
     * 基于口令（使用默认口令）的对称加密
     * 返回字节数组，可以使用bytes2HexString方法将字节数组转化成小写的16进制字符串
     *
     * @param src
     * @return
     * @throws Exception
     */
    public static byte[] pbeEncrypt(String src) throws Exception {
        return pbeEncrypt(src, DEFAULT_KEY_SPEC);
    }

    /**
     * 基于口令的对称解密
     * 接收字节数组，可以使用hexString2Bytes方法将小写的16进制字符串转化成字节数组
     *
     * @param encrypt
     * @param keySpec
     * @return
     * @throws Exception
     */
    public static String pbeDecrypt(byte[] encrypt, String keySpec) throws Exception {
        //实例化工具
        Cipher cipher = Cipher.getInstance(PBE_ALGORITHM);

        //使用该工具将基于密码的形式生成Key
        SecretKey key = SecretKeyFactory.getInstance(PBE_ALGORITHM).generateSecret(new PBEKeySpec(keySpec.toCharArray()));  //pwd
        PBEParameterSpec parameterspec = new PBEParameterSpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}, 2);  //salt

        //初始化加密操作，同时传递加密的算法
        cipher.init(Cipher.DECRYPT_MODE, key, parameterspec);

        //将要加密的数据传递进去，返回加密后的数据
        byte[] results = cipher.doFinal(encrypt);

        return new String(results);
    }

    /**
     * 使用默认密码将小写的16进制字符串解密成字符串
     * 基于口令的对称解密
     * @param encrypt
     * @return
     * @throws Exception
     */
    public static String pbeDecrypt(byte[] encrypt) throws Exception {
        return pbeDecrypt(encrypt, DEFAULT_KEY_SPEC);
    }

    /**
     * 生成非对称加密的密钥对
     * @return
     * @throws Exception
     */
    public static Map<String, RSAKey> generateSecretKeyPairMap() throws Exception {
        //实例化密钥生成器
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        //初始化密钥生成器
        keyPairGenerator.initialize(RSA_KEY_SIZE);
        //生成密钥对
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        //甲方公钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        //甲方私钥
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        //将密钥存储在map中
        Map<String, RSAKey> keyMap = new HashMap<>();
        keyMap.put(RSA_PUBLIC_KEY, publicKey);
        keyMap.put(RSA_PRIVATE_KEY, privateKey);
        return keyMap;
    }

    /**
     * 保存非对称加密的密钥对
     * @param secretKeyPairMap
     * @param secretKeyPairDirPath 是一个目录，会在该目录下生成public.key、private.key两个文件
     */
    public static void saveSecretKeyPair(Map<String, RSAKey> secretKeyPairMap, String secretKeyPairDirPath) throws Exception {
        PublicKey publicKey = (PublicKey)secretKeyPairMap.get(RSA_PUBLIC_KEY);
        PrivateKey privateKey = (PrivateKey)secretKeyPairMap.get(RSA_PRIVATE_KEY);

        byte[] publicKeyEncodedBytes = publicKey.getEncoded();
        byte[] privateKeyEncodedBytes = privateKey.getEncoded();

        //字节数组转换成base64字符串
        String publicKeyEncodedBase64 = EncryptionUtil.byteArr2Base64Str(publicKeyEncodedBytes);
        String privateKeyEncodedBase64 = EncryptionUtil.byteArr2Base64Str(privateKeyEncodedBytes);

        byte[] publicKeyEncodedBase64Bytes = publicKeyEncodedBase64.getBytes();
        byte[] privateKeyEncodedBase64Bytes = privateKeyEncodedBase64.getBytes();

        FileUtils.writeByteArrayToFile(new File(secretKeyPairDirPath + File.separator + RSA_PUBLIC_KEY_FILE_NAME), publicKeyEncodedBase64Bytes);
        FileUtils.writeByteArrayToFile(new File(secretKeyPairDirPath + File.separator + RSA_PRIVATE_KEY_FILE_NAME), privateKeyEncodedBase64Bytes);

    }

    /**
     * 加载非对称加密的公钥
     * @param publicKeyFilePath 公钥文件
     * @return
     * @throws Exception
     */
    public static PublicKey loadPublicKey(String publicKeyFilePath) throws Exception {
        byte[] keyEncodedBase64Bytes = FileUtils.readFileToByteArray(new File(publicKeyFilePath));
        String keyEncodedBase64 = new String(keyEncodedBase64Bytes);
        //base64字符串转换成字节数组
        byte[] keyEncodedBytes = EncryptionUtil.base64Str2ByteArr(keyEncodedBase64);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyEncodedBytes);
        KeyFactory factory = KeyFactory.getInstance(RSA_ALGORITHM);
        PublicKey publicKey = (PublicKey)factory.generatePublic(x509EncodedKeySpec);
        return publicKey;
    }

    /**
     * 加载非对称加密的私钥
     * @param privateKeyFilePath 私钥文件
     * @return
     * @throws Exception
     */
    public static PrivateKey loadPrivateKey(String privateKeyFilePath) throws Exception {
        byte[] keyEncodedBase64Bytes = FileUtils.readFileToByteArray(new File(privateKeyFilePath));
        String keyEncodedBase64 = new String(keyEncodedBase64Bytes);
        //base64字符串转换成字节数组
        byte[] keyEncodedBytes = EncryptionUtil.base64Str2ByteArr(keyEncodedBase64);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyEncodedBytes);
        KeyFactory factory = KeyFactory.getInstance(RSA_ALGORITHM);
        PrivateKey privateKey = (PrivateKey)factory.generatePrivate(pkcs8KeySpec);
        return privateKey;
    }

    /**
     * 非对称私钥加密，私钥加密公钥解密，常用于数字签名，使用私钥签名，使用公钥验签。
     * @param src
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static byte[] rsaEncryptByPrivateKey(String src, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        return cipher.doFinal(src.getBytes());
    }

    /**
     * 非对称私钥加密，私钥加密公钥解密，常用于数字签名，使用私钥签名，使用公钥验签。
     * @param src
     * @param privateKeyBytes 从私钥文件中读取到的字节数组
     * @return
     * @throws Exception
     */
    public static byte[] rsaEncryptByPrivateKey(String src, byte[] privateKeyBytes) throws Exception {
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        //密钥材料转换
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(EncryptionUtil.base64Str2ByteArr(new String(privateKeyBytes)));
        //生成私钥
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        return rsaEncryptByPrivateKey(src, privateKey);
    }

    /**
     * 非对称公钥解密，私钥加密公钥解密，常用于数字签名，使用私钥签名，使用公钥验签。
     * @param encrypt
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static String rsaDecryptByPublicKey(byte[] encrypt, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return new String(cipher.doFinal(encrypt));
    }

    /**
     * 非对称公钥解密，私钥加密公钥解密，常用于数字签名，使用私钥签名，使用公钥验签。
     * @param encrypt
     * @param publicKeyBytes 从公钥文件中读取到的字节数组
     * @return
     * @throws Exception
     */
    public static String rsaDecryptByPublicKey(byte[] encrypt, byte[] publicKeyBytes) throws Exception {
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(EncryptionUtil.base64Str2ByteArr(new String(publicKeyBytes)));
        //生成公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
        return rsaDecryptByPublicKey(encrypt, pubKey);
    }

    /**
     * 非对称公钥加密
     * @param src
     * @param publicKey
     * @return
     * @throws Exception
     */
    public static byte[] rsaEncryptByPublicKey(String src, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(src.getBytes());
    }

    /**
     * 非对称公钥加密，
     * @param src
     * @param publicKeyBytes 从公钥文件中读取到的字节数组
     * @return
     * @throws Exception
     */
    public static byte[] rsaEncryptByPublicKey(String src, byte[] publicKeyBytes) throws Exception {
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        //密钥材料转换
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(EncryptionUtil.base64Str2ByteArr(new String(publicKeyBytes)));
        //生成公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
        return rsaEncryptByPublicKey(src, pubKey);
    }

    /**
     * 非对称私钥解密
     * @param encrypt
     * @param privateKey
     * @return
     * @throws Exception
     */
    public static String rsaDecryptByPrivateKey(byte[] encrypt, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(encrypt));
    }

    /**
     * 非对称私钥解密
     * @param encrypt
     * @param privateKeyBytes 从私钥文件中读取到的字节数组
     * @return
     * @throws Exception
     */
    public static String rsaDecryptByPrivateKey(byte[] encrypt, byte[] privateKeyBytes) throws Exception {
        //实例化密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        //密钥材料转换
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(EncryptionUtil.base64Str2ByteArr(new String(privateKeyBytes)));
        //生成私钥
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        return rsaDecryptByPrivateKey(encrypt, privateKey);
    }

    /**
     * sha256加密
     * @param src
     * @return
     */
    public static String sha256Encrypt(String src) throws NoSuchAlgorithmException {
        MessageDigest messageDigest;
        messageDigest = MessageDigest.getInstance(SHA_256_ALGORITHM);
        messageDigest.update(src.getBytes());
        return EncryptionUtil.bytes2HexString(messageDigest.digest());
    }

    /**
     * 用私钥对数据签名
     * @param data 待签名数据
     * @param privateKey 私钥
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data);
        return EncryptionUtil.byteArr2Base64Str(signature.sign());
    }

    /**
     * 用私钥对数据签名
     * @param data 待签名数据
     * @param privateKeyBytes 从私钥文件中读取到的字节数组
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, byte[] privateKeyBytes) throws Exception {
        // 解密由base64编码的私钥
        byte[] keyBytes = EncryptionUtil.base64Str2ByteArr(new String(privateKeyBytes));
        // 构造PKCS8EncodedKeySpec对象
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        //指定加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        //获取私钥匙对象
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        return EncryptionUtil.sign(data, privateKey);
    }

    /**
     * 验证签名
     * @param data 待验签数据
     * @param publicKey 公钥
     * @param sign
     * @return
     * @throws Exception
     */
    public static boolean verify(byte[] data, PublicKey publicKey, String sign) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data);
        // 验证签名是否正常
        return signature.verify(EncryptionUtil.base64Str2ByteArr(sign));
    }

    /**
     * 验证签名
     * @param data 待验签数据
     * @param publicKeyBytes 从公钥文件中读取到的字节数组
     * @param sign
     * @return
     * @throws Exception
     */
    public static boolean verify(byte[] data, byte[] publicKeyBytes, String sign) throws Exception {
        // 解密由base64编码的公钥
        byte[] keyBytes = EncryptionUtil.base64Str2ByteArr(new String(publicKeyBytes));
        // 构造X509EncodedKeySpec对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        //指定加密算法
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        // 取公钥匙对象
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return EncryptionUtil.verify(data, publicKey, sign);
    }


    /**
     * 16进制字符串转换成字节数组
     *
     * @param str
     * @return
     */
    public static byte[] hexString2Bytes(String str) {

        int byteLength = str.length() / 2;//16进制字符串中的两个字符转换成一个字节

        byte[] bytes = new byte[byteLength];
        for (int i = 0; i < byteLength; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);//subStr为大写字母和小写字符的结果是一样的
        }

        return bytes;
    }

    /**
     * 字节数组转换成16进制字符串(小写的)
     *
     * @param bytes
     * @return
     */
    public static String bytes2HexString(byte[] bytes) {
        int charLength = bytes.length * 2;//一个字节可以转换成两个16进制字符
        char[] chars = new char[charLength];
        int index = 0;
        for (byte b : bytes) {
            // 利用位运算进行转换，
            chars[index++] = HEX_CHAR[b >>> 4 & 0xf];
            chars[index++] = HEX_CHAR[b & 0xf];
        }
        return new String(chars);
    }

    /**
     * 使用指定的口令加密输入流
     *
     * @param inputStream
     * @param pwd
     * @return
     * @throws Exception
     */
    public static InputStream encryptInputStream(InputStream inputStream, String pwd) throws Exception {
        return secretInputStream(inputStream, pwd, "1");
    }

    /**
     * 使用指定的口令解密输入流
     *
     * @param inputStream
     * @param pwd
     * @return
     * @throws Exception
     */
    public static InputStream decryptInputStream(InputStream inputStream, String pwd) throws Exception {
        return secretInputStream(inputStream, pwd, "0");
    }

    /**
     * 加密/解密输入流
     *
     * @param inputStream
     * @param pwd
     * @param type        操作类型，1加密，0解密
     * @return
     * @throws Exception
     */
    private static InputStream secretInputStream(InputStream inputStream, String pwd, String type) throws Exception {
        // 实例化工具
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");

        // 使用该工具将基于密码的形式生成Key
        Key key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(pwd.toCharArray())); // pwd
        PBEParameterSpec parameterspec = new PBEParameterSpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}, 1); // salt

        // 初始化加密操作，同时传递加密的算法
        if ("1".equals(type)) {
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterspec);
        } else if ("0".equals(type)) {
            cipher.init(Cipher.DECRYPT_MODE, key, parameterspec);
        }

        // 对输入流进行加密
        CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher);
        return cipherInputStream;
    }

    /**
     * 对字符串做Base64编码
     *
     * @param src
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encodeWithBase64(String src) throws UnsupportedEncodingException {
        if (src != null) {
            String charsetName = "UTF-8";
            byte[] srcByte = src.getBytes(charsetName);
            String encodedSrc = new String(Base64.getEncoder().encode(srcByte), charsetName);
            return encodedSrc;
        } else {
            return null;
        }
    }

    /**
     * 对字符串做Base64解码
     *
     * @param base64Str
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String decodeWithBase64(String base64Str) throws UnsupportedEncodingException {
        if (base64Str != null) {
            String charsetName = "UTF-8";
            byte[] srcByte = base64Str.getBytes(charsetName);
            String decodedBase64Str = new String(Base64.getDecoder().decode(srcByte), charsetName);
            return decodedBase64Str;
        } else {
            return null;
        }
    }

    /**
     * 字节数组加密成base64字符串
     * @param byteArr
     * @return
     */
    public static String byteArr2Base64Str(byte[] byteArr){
        return (new BASE64Encoder()).encodeBuffer(byteArr);
    }

    /**
     * base64字符串解密成字节数组
     * @param base64Str
     * @return
     */
    public static byte[] base64Str2ByteArr(String base64Str) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(base64Str);
    }

}
