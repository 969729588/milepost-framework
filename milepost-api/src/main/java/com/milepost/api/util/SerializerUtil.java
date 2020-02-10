package com.milepost.api.util;

import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * 序列化
 * @author Huarf
 * 2017年11月5日
 */
public class SerializerUtil {

	/**
	 * 序列化
	 * 
	 * @param object
	 * @return
	 */
	public static byte[] serializeObj(Object object) throws IOException {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		byte[] bytes = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			bytes = baos.toByteArray();
		}finally {
			IOUtils.closeQuietly(oos);
			IOUtils.closeQuietly(baos);
		}
		return bytes;
	}

	/**
	 * 反序列化
	 * 
	 * @param bytes
	 * @return
	 */
	public static Object deserializeObj(byte[] bytes) throws IOException, ClassNotFoundException {
		if (bytes == null) {
			return null;
		}
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		Object object = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bais);
			object = ois.readObject();
		}finally {
			IOUtils.closeQuietly(ois);
			IOUtils.closeQuietly(bais);
		}
		
		return object;
	}
}