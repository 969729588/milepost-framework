package com.milepost.core.lns;

import com.milepost.api.util.EncryptionUtil;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by Ruifu Hua on 2020/2/3.
 */
public class Lice {
    public static final String EXPIRATION_DATE = "expirationDate";
    DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private Properties properties;

    public Lice(Properties properties) throws LiceE {
        this.properties = properties;
    }

    /**
     * 获取过期日期，yyyy/MM/dd
     * @return
     */
    public Date getExpirationDate() {
        LocalDate localDate = LocalDate.parse(this.properties.getProperty(EXPIRATION_DATE), this.DATE_FORMAT);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * 获取过期时间，
     * @return
     */
    public String getExpirationDateAsString() {
        return this.properties.getProperty(EXPIRATION_DATE);
    }

    /**
     * 验证此license是否适用于指定产品，
     * license中的产品可以使用“,”分割，“all”表示适用于所有产品，不配置product则忽略此项
     * @param name
     * @throws LiceE
     */
    public void isProduct(String name) throws Exception {
        if(this.properties.containsKey("product")) {
            //product支持用逗号分隔的，"all"表示适用于所有产品
            String[] products = this.properties.get("product").toString().split(",");
            boolean match = Boolean.FALSE.booleanValue();

            for(String product : products) {
                if(product.equalsIgnoreCase("all") || product.equalsIgnoreCase(name)) {
                    match = Boolean.TRUE.booleanValue();
                    break;
                }
            }

            if(!match) {
                String msg = EncryptionUtil.pbeDecrypt(Constant.ex4.getBytes());//授权产品不符
                throw new LiceE(msg + "[" + this.properties.get("product") + "]");
            }
        }

    }

    /**
     * 验证此license是否过期
     * @throws Exception
     */
    public void isExpired() throws Exception {
        if(System.currentTimeMillis() > (this.getExpirationDate().getTime())) {
            String msg = EncryptionUtil.pbeDecrypt(EncryptionUtil.hexString2Bytes(Constant.infokey));//licenseinfo
            //String infotitle = EncryptionUtil.pbeDecrypt(EncryptionUtil.hexString2Bytes(Constant.infotitle));//授权信息: %s 有效期 %s
            //String cn = EncryptionUtil.pbeDecrypt(EncryptionUtil.hexString2Bytes(Constant.cn));//companyName
            //System.setProperty(msg, String.format(infotitle, new Object[]{this.properties.getProperty(cn), this.getExpirationDateAsString()}));

            msg = EncryptionUtil.pbeDecrypt(EncryptionUtil.hexString2Bytes(Constant.ex2));//授权过期
            throw new LiceE(msg + "[" + this.getExpirationDateAsString() + "]");
        }
    }

    /**
     * 获取此license还有多少天过期
     * @return
     */
    public int getDaysTillExpire() {
        int result = 0;
        long milliseconds = this.getExpirationDate().getTime() - new Date().getTime();
        if(milliseconds <= 0){
            result = 0;
        }else{
            result =  (int)(milliseconds / (24*60*60*1000)) + 1;
        }
        return result;
    }

    public List<String> getInfo() throws Exception {
        List<String> lic = new ArrayList();
        String line = EncryptionUtil.pbeDecrypt(EncryptionUtil.hexString2Bytes(Constant.line));
        String exp = EncryptionUtil.pbeDecrypt(EncryptionUtil.hexString2Bytes(Constant.exp));

        lic.add(line);
        lic.add(this.properties.getProperty("product") == null?"all":this.properties.getProperty("product"));
        lic.add(exp + this.getExpirationDateAsString());
        lic.add(line);
        return lic;
    }

    public String toString() {
        return this.properties.toString();
    }
}
