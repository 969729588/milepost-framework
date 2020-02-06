package com.milepost.core.lns;

import com.milepost.api.util.EncryptionUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;

/**
 * Created by Ruifu Hua on 2020/2/3.
 */
public class LiceM {

    private static final Logger logger = LoggerFactory.getLogger(LiceM.class);

    public static String LICE_FILE_NAME;//license.dat
    public static String PK;//public.key
    public static String SIGN;//signature
    private long lastModified = 0L;
    private Lice lice;
    private static LiceM liceM = new LiceM();

    static {
        try {
            LICE_FILE_NAME = EncryptionUtil.pbeDecrypt(EncryptionUtil.hexString2Bytes(Constant.ld));
            PK = EncryptionUtil.pbeDecrypt(EncryptionUtil.hexString2Bytes(Constant.pk));
            SIGN = EncryptionUtil.pbeDecrypt(EncryptionUtil.hexString2Bytes(Constant.sign));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public LiceM() {
    }

    /**
     * 获取单例的LiceM
     * @return
     */
    public static LiceM getInstance() {
        return liceM;
    }

    /**
     * 验证license是否可用
     * @param lice
     * @param product
     * @throws LiceE
     */
    public void isValidLice(Lice lice, String product) throws Exception {
        //产品是否匹配
        lice.isProduct(product);
        //授权是否过期
        lice.isExpired();
    }

    /**
     * 获取Lice对象
     * @return
     * @throws Exception
     */
    public Lice getLice() throws Exception {
        File lns = new File(LICE_FILE_NAME);//license.dat
        if(!lns.exists() || !lns.isFile()) {
            String msg = EncryptionUtil.pbeDecrypt(EncryptionUtil.hexString2Bytes(Constant.ex3));//授权文件没找到
            throw new LiceE(msg);
        } else {
            //不是每次都loadLice
            if(this.lastModified == 0L || this.lastModified != lns.lastModified()) {
                this.lice = this.loadLice();
                this.lastModified = lns.lastModified();
            }
            return this.lice;
        }
    }

    /**
     * 加载license文件
     * @return
     * @throws Exception
     */
    private Lice loadLice() throws Exception {
        Properties properties = this.loadProperties(LICE_FILE_NAME);
        if(!properties.containsKey(SIGN)) {
            String msg = EncryptionUtil.pbeDecrypt(EncryptionUtil.hexString2Bytes(Constant.missign));//授权文件中没有签名
            throw new LiceE(msg);
        } else {
            String signature = (String)properties.remove(SIGN);//signature
            String encoded = properties.toString();
            PublicKey publicKey = this.readPublicKey(PK);//public.key
            if(!this.verify(encoded.getBytes(), signature, publicKey)) {
                String msg = EncryptionUtil.pbeDecrypt(EncryptionUtil.hexString2Bytes(Constant.ex1));//无效的授权文件
                throw new LiceE(msg);
            } else {
                return new Lice(properties);
            }
        }
    }

    /**
     * 读取license文件内容
     * @param liceFileName
     * @return
     * @throws Exception
     */
    private Properties loadProperties(String liceFileName) throws Exception {
        InputStream inputStream = null;
        Properties properties = null;
        try {
            File liceFile = new File(liceFileName);
            if(liceFile.exists() && liceFile.isFile()){
                //如果存在并且是文件
                inputStream = new FileInputStream(liceFile);
            }else{
                //否则从类路径下读取
//                Resource resource = new ClassPathResource(liceFileName);
//                inputStream = resource.getInputStream();
                inputStream = ResourceUtils.getURL(ResourceUtils.CLASSPATH_URL_PREFIX + liceFileName).openStream();
            }
            properties = new OrderedProperties();
            properties.load(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return properties;
    }

    /**
     * 读取公钥
     * @param publicKeyFileName
     * @return
     * @throws Exception
     */
    private PublicKey readPublicKey(String publicKeyFileName) throws Exception {
        File publicKeyFile = new File(publicKeyFileName);
        byte[] bytes;
        if(publicKeyFile.exists() && publicKeyFile.isFile()) {
            //如果存在并且是文件
            bytes = FileUtils.readFileToByteArray(publicKeyFile);
        } else {
            //否则从类路径下读取
            Resource resource = new ClassPathResource(publicKeyFileName);
            bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
        }
        String keyEncodedBase64 = new String(bytes);
        byte[] keyEncodedBytes = EncryptionUtil.base64Str2ByteArr(keyEncodedBase64);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyEncodedBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(EncryptionUtil.RSA_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 验证签名
     * @param message
     * @param signature
     * @param publicKey
     * @return
     * @throws Exception
     */
    private boolean verify(byte[] message, String signature, PublicKey publicKey) throws Exception {
        return EncryptionUtil.verify(message, publicKey, signature);
    }
}
