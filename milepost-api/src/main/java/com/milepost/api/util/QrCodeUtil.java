package com.milepost.api.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

/**
 * 生成二维码工具类
 */
public class QrCodeUtil {

	/**
	 * 生成二维码，可指定图片尺寸
	 * @param outputStream
	 * @param content
	 * @param imageSize 图片尺寸
	 * @throws Exception
	 */
	public static void generateQRCode(OutputStream outputStream, String content, Integer imageSize)
			throws Exception {
		generateQRCode(outputStream, content, imageSize, null, null, null, null, null, null);
	}
	
	/**
	 * 生成二维码，可指定图片尺寸和logo
	 * @param outputStream
	 * @param content
	 * @param imageSize 图片尺寸
	 * @param logoFile logo
	 * @throws Exception
	 */
	public static void generateQRCode(OutputStream outputStream, String content, Integer imageSize, File logoFile)
			throws Exception {
		generateQRCode(outputStream, content, imageSize, logoFile, null, null, null, null, null);
	}
	
	/**
	 * 生成二维码，最基础的类
	 * @param outputStream
	 *            将图片写入到输出流，注意，该方法中并没有关闭该输出流,
	 * @param content
	 *            二维码内容，
	 * @param level
	 *            纠错级别，默认为ErrorCorrectionLevel.L，
	 * @param imageSize
	 *            图片尺寸，默认为200px，
	 * @param imageFormat
	 *            图片格式，默认为JPEG，
	 * @param onColor
	 *            前景颜色，默认0xFF000000黑色，
	 * @param offColor
	 *            背景颜色，默认0xFFFFFFFF白色，
	 * @param margin
	 *            边框，取值-1~4，-1表示一点边框都没有，内部调用了以方法去除了边框
	 * @param logoFile
	 *            二维码的logo
	 * @throws Exception
	 */
	public static void generateQRCode(OutputStream outputStream, String content, 
			Integer imageSize, 
			File logoFile, 
			String imageFormat, 
			Integer margin, 
			Integer onColor, 
			Integer offColor, 
			ErrorCorrectionLevel level)
			throws Exception {
		// 图片格式
		if (level == null) {
			level = ErrorCorrectionLevel.L;
		}
		if (imageSize == null) {
			imageSize = 200;
		}
		if (imageFormat == null) {
			imageFormat = "JPEG";
		}
		if (onColor == null) {
			onColor = 0xFF000000;
		}
		if (offColor == null) {
			offColor = 0xFFFFFFFF;
		}
		if (margin == null) {
			margin = 1;
		}

		Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
		// 指定纠错等级
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		// 指定编码格式
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		// 设置白边 0--4
		hints.put(EncodeHintType.MARGIN, margin);
		BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, imageSize, imageSize,
				hints);
		// 如果margin==-1，则删除白色边框，
		if (margin != null && margin.intValue() == -1) {
			bitMatrix = deleteWhite(bitMatrix);
		}

		if (logoFile == null) {
			// 不带有logo的
			MatrixToImageConfig config = new MatrixToImageConfig(onColor, offColor);
			MatrixToImageWriter.writeToStream(bitMatrix, imageFormat, outputStream, config);
		} else {
			// 带有logo的
			BufferedImage image = matrix2BufferedImage(bitMatrix, onColor, offColor);
			Graphics2D gs = image.createGraphics();
			int ratioWidth = image.getWidth() * 2 / 10;
			int ratioHeight = image.getHeight() * 2 / 10;
			// 载入logo
			Image img = ImageIO.read(logoFile);
			int logoWidth = img.getWidth(null) > ratioWidth ? ratioWidth : img.getWidth(null);
			int logoHeight = img.getHeight(null) > ratioHeight ? ratioHeight : img.getHeight(null);

			int x = (image.getWidth() - logoWidth) / 2;
			int y = (image.getHeight() - logoHeight) / 2;

			gs.drawImage(img, x, y, logoWidth, logoHeight, null);
			gs.setColor(Color.black);
			gs.setBackground(Color.WHITE);
			gs.dispose();
			img.flush();
			ImageIO.write(image, imageFormat, outputStream);
		}
	}

	/**
	 * 删除二维码的白色边框
	 * 
	 * @param matrix
	 * @return
	 */
	private static BitMatrix deleteWhite(BitMatrix matrix) {
		int[] rec = matrix.getEnclosingRectangle();
		int resWidth = rec[2] + 1;
		int resHeight = rec[3] + 1;

		BitMatrix resMatrix = new BitMatrix(resWidth, resHeight);
		resMatrix.clear();
		for (int i = 0; i < resWidth; i++) {
			for (int j = 0; j < resHeight; j++) {
				if (matrix.get(i + rec[0], j + rec[1]))
					resMatrix.set(i, j);
			}
		}
		return resMatrix;
	}

	/**
	 * Matrix转化成BufferedImage，用于生成带有logo的二维码
	 * 
	 * @param matrix
	 * @param onColor
	 *            前景颜色
	 * @param offColor
	 *            背景颜色
	 * @return
	 */
	private static BufferedImage matrix2BufferedImage(BitMatrix matrix, Integer onColor, Integer offColor) {
		int width = matrix.getWidth();
		int height = matrix.getHeight();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, matrix.get(x, y) ? onColor : offColor);
			}
		}
		return image;
	}

	/**
	 * 读二维码并输出携带的信息
	 */
	public static void readQrCode(InputStream inputStream) throws IOException {
		// 从输入流中获取字符串信息
		BufferedImage image = ImageIO.read(inputStream);
		// 将图像转换为二进制位图源
		LuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader = new QRCodeReader();
		Result result = null;
		try {
			result = reader.decode(bitmap);
		} catch (ReaderException e) {
			e.printStackTrace();
		}
		System.out.println(result.getText());
	}

}
