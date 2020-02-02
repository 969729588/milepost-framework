package com.milepost.api.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * MD5加密
 * @author Huarf
 * 2017年9月27日
 */
public class MD5Util {
	
	protected static MessageDigest messagedigest = null;
    static {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsaex) {
        	//根本就不会抛出异常，因为这里的加密方式已经写死成“MD5”了
        }
    }
	
    /**
     * 生成文件的md5校验值，部分内容参加校验，适合与大文件(>=512MB)
     * 数列2的n次幂
     * 
     * @param file
     * @return
     * @throws IOException
     */
    private static String file2MD5Fast(File file) throws IOException {       
        InputStream fis = null;
        String result = null;
        try {
        	//依据2的n次幂数列获取要取出的次数
        	int byteLength = 2048;//缓冲数组的长度
        	List<Double> pointList = getPointList(file.length(), byteLength);
        	fis = new FileInputStream(file);
            byte[] buffer = new byte[byteLength];
            double readCount = 0;//读取次数
            int numRead = 0;//每次读取的长度
            while ((numRead = fis.read(buffer)) > 0) {
            	readCount = readCount + 1;
            	if(pointList.contains(readCount)){
            		//多次update的机制累计，而不会覆盖
            		messagedigest.update(buffer, 0, numRead);
            	}
            }
            byte[] targ = messagedigest.digest();
            result = bintoascii(targ);
		} catch (Exception e) {
			throw e;
		}finally {
			if(fis != null){
				fis.close();
			}
		}
        return result;
    }
    
    /**
     * 依据2的n次幂数列获取要取出的点，[1,2,3,4,16,32,64,128,,,]
     * @param length
     * @param byteLength
     * @return
     */
    private static List<Double> getPointList(long length, int byteLength) {
    	
    	//每次读取byteLength，读取count次
    	int count = (int)length/byteLength;
    	if((length%byteLength)!=0){
    		count = count + 1;
    	}
    	
    	List<Double> pointList = new ArrayList<Double>();
    	for(int i=0; (Math.pow(2,i))<count; i++){
    		pointList.add(Math.pow(2,i));
    	}
		return pointList;
	}

	/**
     * 生成文件的md5校验值，全内容参加校验，适合与小文件(<=512MB)
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static String file2MD5(File file) throws IOException {      
    	//先判断文件的大小，根据大小选择不同的方法
    	long fileLength = 512 * 1024 * 1024;//512MB
    	if(file!=null && file.length()>fileLength){
    		return file2MD5Fast(file);
    	}
        InputStream fis = null;
        String result = null;
        try {
        	fis = new FileInputStream(file);
            byte[] buffer = new byte[2048];
            int numRead = 0;
            while ((numRead = fis.read(buffer)) > 0) {
            	//多次update的机制累计，而不会覆盖
                messagedigest.update(buffer, 0, numRead);
            }
            byte[] targ = messagedigest.digest();
            result = bintoascii(targ);
		} catch (Exception e) {
			throw e;
		}finally {
			if(fis != null){
				fis.close();
			}
		}
        return result;
    }
    
	/**
	 * 将md5加密后的128位（bit）的结果转换成16进制字符表示的字符串
	 * @param bySourceByte
	 * @return
	 */
	private static String bintoascii(byte[] bySourceByte){
		int len,i;
		byte tb;
		char high,tmp,low;
		String result=new String();
		len=bySourceByte.length;
		for(i=0;i<len;i++)
		{
			tb=bySourceByte[i];
			
			tmp=(char)((tb>>>4)&0x000f);
			if(tmp>=10)
				high=(char)('a'+tmp-10);
			else
				high=(char)('0'+tmp);
			result+=high;
			tmp=(char)(tb&0x000f);
			if(tmp>=10)
				low=(char)('a'+tmp-10);
			else
				low=(char)('0'+tmp);
			
			result+=low;
		}
		return result;
	}

	/**
	 * MD5加密
	 * 
	 * 加密结果：
	 *  string2MD5 ("") = d41d8cd98f00b204e9800998ecf8427e
		string2MD5 ("a") 	= 0cc175b9c0f1b6a831c399e269772661
		string2MD5 ("abc") = 900150983cd24fb0d6963f7d28e17f72
		string2MD5 ("message digest") = 	f96b697d7cb7938d525a2f31aaf161d0
		string2MD5 ("abcdefghijklmnopqrstuvwxyz") 	= 	c3fcd3d76192e4007dfb496cca67e13b
		string2MD5 ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789") = d174ab98d277d9f5a5611c2c9f419d9f
		string2MD5 ("12345678901234567890123456789012345678901234567890123456789012345678901234567890") = 	57edf4a22be3c955ac49da2e2107b67a
	 */
	public static String string2MD5(String inSrc){
		String result = null;
		byte[] bytes = inSrc.getBytes();
		byte[] targ = messagedigest.digest(bytes);
		result = bintoascii(targ);
		return result;
	}

	public static void main(String[] args) {
		System.out.println(string2MD5("a"));
	}
}
