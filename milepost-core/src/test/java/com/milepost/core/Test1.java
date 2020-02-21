package com.milepost.core;

import com.milepost.api.util.EncryptionUtil;
import com.milepost.core.lns.OrderedProperties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Properties;

/**
 * Created by Ruifu Hua on 2020/2/4.
 * 生成jks、提取公钥/私钥、生成license.dat
 *
 * https://blog.csdn.net/kexiuyi/article/details/52538383
 * JKS文件的结构：
 *  JKS文件是一个java中的密钥管理库，里面可以放很多的东西，这里只存放一类东西就是密钥，仓库当然会有一把锁，防范别人随便乱拿，这个就是JKS文件的密码。
 *  里面存放的密钥也各有不同，每个密钥都有一个名字（在下面叫别名），一类就密钥对，一类叫公钥，一类叫私钥。
 *  只要你能进入仓库你就可以随便查看拿走公钥，私钥则是有密码的，只允许有权限的人查看拿走。
 *  所以在下面读取密钥时也就有点细微的不同之处，对于读取公钥只需要知道JKS文件（仓库）的密码就可以了，
 *  但是在读取私钥时则必须有私钥的密码，也就是你必须要有权限，在下面你会发现，在读取私钥时多了一个参数，对应的就是私钥的密码。
 */
public class Test1 {

    @Test
    public void test1() throws Exception {
        //检测resources下是否存在jks文件，如果存在则不要重新创建
        if(jksInResources()){
            return;
        }

        //生成jks文件
        String keyStoreType = "JKS";
        String alias = "milepost-alias";
        String keyalg = "RSA";
        int validity = 365;
        String keystore = getRealPath("milepost.jks");
        String jksFilePath = keystore;
        String keypass = "milepost";
        String storepass = "milepost";
        String dname = "CN=花瑞富,OU=milepost公司,O=milepost公司,L=shenyan,S=liaoning,C=CH";

        generateJKS(alias, validity, keyalg, keypass, keystore, storepass, dname);

        //导出公钥，私钥
        String privateKeyFilePath = getRealPath("private.key");
        String publicKeyFilePath = getRealPath("public.key");
        KeyPair keyPair = exportKeyPair(jksFilePath, keyStoreType, alias, storepass, keypass);
        exportPrivateKey(keyPair, privateKeyFilePath);
        exportPublicKey(keyPair, publicKeyFilePath);

        //生成license文件
        String licenseFilePath = getRealPath("license.dat");
        String product = "all";
        String expirationDate = "2020/12/31";
        String companyName = "milepost";
        String emailAddress = "969729588@qq.com";
        generateLicense(licenseFilePath, privateKeyFilePath,
                product, expirationDate, companyName, emailAddress);

        //验签
        test2();
    }

    /**
     * 检测resources下是否存在jks文件，存在则返回true，否则返回false
     * @return
     */
    private boolean jksInResources() throws UnsupportedEncodingException {
        URL url = this.getClass().getResource("/");
        String urlStr = URLDecoder.decode(url.getPath(), "UTF-8");
        urlStr = urlStr.replace("target/test-classes/", "src/main/resources/");
        urlStr = urlStr + "milepost.jks";
        File file = new File(urlStr);
        if(file.exists() && file.isFile()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 验签
     * @throws Exception
     */
    public void test2() throws Exception {
        InputStream inputStream = null;
        try {
            String licenseFilePath = getRealPath("license.dat");
            String publicKeyFilePath = getRealPath("public.key");
            PublicKey publicKey = EncryptionUtil.loadPublicKey(publicKeyFilePath);
            inputStream = new FileInputStream(licenseFilePath);
            Properties properties = new OrderedProperties();
            properties.load(inputStream);
            String signature = (String)properties.remove("signature");
            String data = properties.toString();
            boolean verify = EncryptionUtil.verify(data.getBytes(), publicKey, signature);
            if(!verify){
                throw new Exception("验签失败！");
            }
        }finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     *
     * @param licenseFilePath
     * @param privateKeyFilePath
     * @param product
     * @param expirationDate
     * @param companyName
     * @param emailAddress
     */
    private void generateLicense(String licenseFilePath, String privateKeyFilePath, String product, String expirationDate, String companyName, String emailAddress) throws Exception {
        Properties properties = new OrderedProperties();
        properties.put("product", product);
        properties.put("expirationDate", expirationDate);
        properties.put("companyName", companyName);
        properties.put("emailAddress", emailAddress);
        String propertiesStr = properties.toString();
        PrivateKey privateKey = EncryptionUtil.loadPrivateKey(privateKeyFilePath);
        String signature = EncryptionUtil.sign(propertiesStr.getBytes(), privateKey);
        properties.put("signature", signature);
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(licenseFilePath);
            properties.store(outputStream, null);
            outputStream.flush();
        }finally {
            IOUtils.closeQuietly(outputStream);
        }
    }


    /**
     * 获取类路径下的文件的真实绝对路径
     * @param fileName
     * @return
     * @throws FileNotFoundException
     */
    private String getRealPath(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
        URL url = this.getClass().getResource("/");
        String urlStr = URLDecoder.decode(url.getPath(), "UTF-8");
        urlStr = urlStr.substring(1);
        return urlStr.replace("target/test-classes", "target/classes") + fileName;
    }

    /**
     * 生成jks文件
     * keytool -genkeypair -alias milepost-alias -validity 365 -keyalg RSA -keypass milepost -keystore milepost.jks -storepass milepost -dname "CN=花瑞富,OU=milepost公司,O=milepost公司,L=shenyan,S=liaoning,C=CH"
     *
     * @param alias 别名
     * @param validity 有效时长(天)
     * @param keyalg 加密算法
     * @param keypass keypass
     * @param keystore jks文件存储位置
     * @param storepass storepass
     * @param dname
     * @throws IOException
     * @throws InterruptedException
     */
    private static void generateJKS(String alias, int validity, String keyalg,
                                    String keypass, String keystore,
                                    String storepass, String dname) throws IOException, InterruptedException {
        StringBuffer cmd = new StringBuffer();
        cmd.append("keytool -genkeypair");
        cmd.append(" -alias "+ alias);//别名
        cmd.append(" -validity "+ validity);//有效时长(天)
        cmd.append(" -keyalg "+ keyalg);//加密算法
        cmd.append(" -keypass " + keypass);//keypass
        cmd.append(" -keystore "+ keystore);//jks文件存储位置
        cmd.append(" -storepass " + storepass);//storepass
        cmd.append(" -dname \""+ dname +"\"");
        Process process = Runtime.getRuntime().exec(cmd.toString());
        process.waitFor();//等待生成文件之后，在继续运行
    }

    /**
     * 获取jks文件中的密钥对
     * @param jksFilePath
     * @param keyStoreType
     * @param alias
     * @param storepass
     * @param keypass
     * @return
     * @throws Exception
     */
    public static KeyPair exportKeyPair(String jksFilePath, String keyStoreType, String alias, String storepass, String keypass) throws Exception {
        KeyPair keyPair = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(jksFilePath);
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(inputStream, storepass.toCharArray());
            Key key = keyStore.getKey(alias, keypass.toCharArray());
            if (key instanceof PrivateKey) {
                Certificate cert = keyStore.getCertificate(alias);
                PublicKey publicKey = cert.getPublicKey();
                keyPair = new KeyPair(publicKey, (PrivateKey) key);
            }
        }finally {
            IOUtils.closeQuietly(inputStream);
        }
        return keyPair;
    }

    public static void exportPrivateKey(KeyPair keyPair, String privateKeyFilePath) throws Exception {
        PrivateKey privateKey = keyPair.getPrivate();
        BASE64Encoder encoder = new BASE64Encoder();
        String encoded = encoder.encode(privateKey.getEncoded());
        FileUtils.writeStringToFile(new File(privateKeyFilePath), encoded);
    }

    public static void exportPublicKey(KeyPair keyPair, String publicKeyFilePath) throws Exception {
        PublicKey publicKey = keyPair.getPublic();
        BASE64Encoder encoder = new BASE64Encoder();
        String encoded = encoder.encode(publicKey.getEncoded());
        FileUtils.writeStringToFile(new File(publicKeyFilePath), encoded);
    }
}
