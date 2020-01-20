package com.milepost.api.util;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.util.ArrayList;

/**
 * 操作文件工具类
 * 
 * @author HRF
 */
public class ZipUtil {

	/**
	 * 压缩文件夹，文件夹中可以包含文件，子文件夹等
	 * @param folderPath 文件夹路径
	 * @param saveZipFileName zip的保存路径
	 * @return
	 */
	public static boolean zipFolder(String folderPath, String saveZipFileName) throws ZipException {
		boolean result = false;
		// Initiate ZipFile object with the path/name of the zip file.
		ZipFile zipFile = new ZipFile(saveZipFileName);

		// Initiate Zip Parameters which define various properties such
		// as compression method, etc.
		ZipParameters parameters = new ZipParameters();

		// set compression method to store compression
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

		// Set the compression level. This value has to be in between 0 to 9
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

		// Create a split file by setting splitArchive parameter to true
		// and specifying the splitLength. SplitLenth has to be greater than
		// 65536 bytes(字节)，即65536/1024=64KB
		// Please note: If the zip file already exists, then this method throws an
		// exception
		zipFile.createZipFileFromFolder(folderPath, parameters, false, 65536);//10485760B = 10240KB = 10MB
		result = true;
		return result;
	}
	
	/**
	 * 压缩文件
	 * @param fileName 文件路径
	 * @param saveZipFileName zip的保存路径
	 * @return
	 */
	public static boolean zipFile(String fileName, String saveZipFileName) throws ZipException {
		boolean result = false;
		// Initiate ZipFile object with the path/name of the zip file.
		ZipFile zipFile = new ZipFile(saveZipFileName);

		// Build the list of files to be added in the array list
		// Objects of type File have to be added to the ArrayList
		ArrayList<File> filesToAdd = new ArrayList<File>();
		filesToAdd.add(new File(fileName));

		// Initiate Zip Parameters which define various properties such
		// as compression method, etc.
		ZipParameters parameters = new ZipParameters();

		// set compression method to store compression
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

		// Set the compression level. This value has to be in between 0 to 9
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

		// Create a split file by setting splitArchive parameter to true
		// and specifying the splitLength. SplitLenth has to be greater than
		// 65536 bytes
		// Please note: If the zip file already exists, then this method throws an
		// exception
		zipFile.createZipFile(filesToAdd, parameters, false, 10485760);
		result = true;
		return result;
	}
}