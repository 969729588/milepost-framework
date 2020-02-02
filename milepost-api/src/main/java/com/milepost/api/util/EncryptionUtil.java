package com.milepost.api.util;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Base64;

/**
 * 加密/解密
 *
 * @author Huarf
 *         2017年12月5日
 */
public class EncryptionUtil {

    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 使用密码将字符串加密成小写的16进制字符串
     * 基于口令的对称加密
     *
     * @param src
     * @param pwd
     * @return
     * @throws Exception
     */
    public static String encryptStr2HexStr(String src, String pwd) throws Exception {
        //实例化工具
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");

        //使用该工具将基于密码的形式生成Key
        SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(pwd.toCharArray()));  //pwd
        PBEParameterSpec parameterspec = new PBEParameterSpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}, 2);  //salt

        //初始化加密操作，同时传递加密的算法
        cipher.init(Cipher.ENCRYPT_MODE, key, parameterspec);

        //将要加密的数据传递进去，返回加密后的数据
        byte[] results = cipher.doFinal(src.getBytes());

        return bytes2HexString(results);
    }

    /**
     * 使用密码将小写的16进制字符串解密成字符串
     * 基于口令的对称解密
     *
     * @param src
     * @param pwd
     * @return
     * @throws Exception
     */
    public static String decryptHexStr2Str(String src, String pwd) throws Exception {
        //实例化工具
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");

        //使用该工具将基于密码的形式生成Key
        SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(new PBEKeySpec(pwd.toCharArray()));  //pwd
        PBEParameterSpec parameterspec = new PBEParameterSpec(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}, 2);  //salt

        //初始化加密操作，同时传递加密的算法
        cipher.init(Cipher.DECRYPT_MODE, key, parameterspec);

        //将要加密的数据传递进去，返回加密后的数据
        byte[] results = cipher.doFinal(hexString2Bytes(src));

        return new String(results);
    }

    /**
     * 16进制字符串转换成字节数组
     *
     * @param str
     * @return
     */
    private static byte[] hexString2Bytes(String str) {

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
    private static String bytes2HexString(byte[] bytes) {
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
}
