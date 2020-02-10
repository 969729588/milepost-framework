package com.milepost.api.util;

import org.junit.Test;

import java.io.IOException;

public class SerializerUtilTest {

	@Test
	public void test1() throws IOException, ClassNotFoundException {
		Student student = new Student();
		//student.setId("123456");
		student.setName("张三");
		byte[] studentByte = SerializerUtil.serializeObj(student);
		for(byte b : studentByte){
			System.out.println(b);
		}
		Student student2 = (Student)SerializerUtil.deserializeObj(studentByte);
		System.out.println(student2);
	}
}
