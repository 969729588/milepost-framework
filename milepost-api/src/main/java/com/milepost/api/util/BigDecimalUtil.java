package com.milepost.api.util;

import java.math.BigDecimal;

/**
 * double 和 float 在计算时候会出现丢失精度的问题，要使用BigDecimal计算，
 * 并且实例化BigDecimal时要传入字符串类型参数，否则一样会出现丢失精度的问题。
 * @author  huarf
 */
public class BigDecimalUtil {

    /**
     * Double四舍五入，指定小数位
     * @param flo
     * @param scale 小数位数
     * @return
     */
    public static Float floatRound(Float flo, int scale){
        if(flo == null){
            return null;
        }
        BigDecimal bigDecimal = BigDecimalUtil.float2BigDecimal(flo);
        return bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * Double四舍五入，指定小数位
     * @param dou
     * @param scale 小数位数
     * @return
     */
    public static Double doubleRound(Double dou, int scale){
        if(dou == null){
            return null;
        }
        BigDecimal bigDecimal = BigDecimalUtil.double2BigDecimal(dou);
        return bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * bigDecimal四舍五入保留两位小数，转double
     * @param bigDecimal
     * @return
     */
    public static Double bigDecimal2Double(BigDecimal bigDecimal){
        if(bigDecimal == null){
            return null;
        }
        return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * bigDecimal四舍五入保留两位小数，转float
     * @param bigDecimal
     * @return
     */
    public static Float bigDecimal2Float(BigDecimal bigDecimal){
        if(bigDecimal == null){
            return null;
        }
        return bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * Double 转 bigDecimal
     * @param dou
     * @return
     */
    public static BigDecimal double2BigDecimal(Double dou){
        if(dou == null){
            return null;
        }
        return string2BigDecimal(Double.toString(dou));
    }

    /**
     * Float 转 bigDecimal
     * @param flo
     * @return
     */
    public static BigDecimal float2BigDecimal(Float flo){
        if(flo == null){
            return null;
        }
        return string2BigDecimal(Float.toString(flo));
    }

    /**
     * String 转 bigDecimal
     * @param str
     * @return
     */
    public static BigDecimal string2BigDecimal(String str){
        if(str == null){
            return null;
        }
        return new BigDecimal(str);
    }

}
