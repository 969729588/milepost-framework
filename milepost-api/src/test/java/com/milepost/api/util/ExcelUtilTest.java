package com.milepost.api.util;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import java.io.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExcelUtilTest {

	@Test
	public void testRead(){
		try {
			InputStream inputStream = new FileInputStream(new File("F:/TestPicture/test1.xlsx"));
			List<LinkedHashMap<String, String>> list = ExcelUtil.read(inputStream);
			for(Map<String, String> map : list){
				for(Map.Entry<String, String> entry : map.entrySet()){
					System.out.print(entry.getKey() + "=" + entry.getValue() + " , ");
				}
				System.out.println("");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testWrite() throws IOException{
		OutputStream outputStream = null;
		Workbook workbook = null;
		try {
			InputStream inputStream = new FileInputStream(new File("F:/TestPicture/test1.xlsx"));
			List<LinkedHashMap<String, String>> list = ExcelUtil.read(inputStream);
			
			outputStream = new FileOutputStream(new File("F:/TestPicture/test1-.xlsx"));
			workbook = ExcelUtil.list2Workbook(list);
			workbook.write(outputStream);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			//注意关闭资源
			if(workbook != null){
				workbook.close();
			}
			if(outputStream != null){
				outputStream.close();
			}
		}
	}


	@Test
	public void testCreateExcel() throws IOException, InterruptedException {
		//工作薄
		Workbook[] wbs = new Workbook[] { new HSSFWorkbook(), new XSSFWorkbook() };//两个工作簿分别对应.xls和.xlsx类型的文件
		for (int i = 0; i < wbs.length; i++) {
			Workbook workbook = wbs[i];
			// 得到一个POI的工具类
			CreationHelper createHelper = workbook.getCreationHelper();

			// 在Excel工作簿中建一工作表，其名为缺省值, 也可以指定Sheet名称，一个EXCEL至少要有一个 sheet，否则打开的时候就会出错
			Sheet sheet = workbook.createSheet();
			// Sheet sheet = workbook.createSheet("SheetName");

			// 用于格式化单元格的数据
			DataFormat format = workbook.createDataFormat();

			// 设置字体
			Font font = workbook.createFont();
			font.setFontHeightInPoints((short) 20); // 字体高度
			font.setColor(Font.COLOR_RED); // 字体颜色
			font.setFontName("黑体"); // 字体
			font.setBoldweight(Font.BOLDWEIGHT_BOLD); // 宽度
			font.setItalic(true); // 是否使用斜体
			// font.setStrikeout(true); //是否使用划线

			// 设置单元格类型
			CellStyle cellStyle = workbook.createCellStyle();
			cellStyle.setFont(font);
			cellStyle.setAlignment(CellStyle.ALIGN_CENTER); // 水平布局：居中
			cellStyle.setWrapText(true);

			CellStyle cellStyle2 = workbook.createCellStyle();
			cellStyle2.setDataFormat(format.getFormat("＃, ## 0.0"));

			CellStyle cellStyle3 = workbook.createCellStyle();
			cellStyle3.setDataFormat(format.getFormat("yyyy-MM-dd HH:mm:ss"));

			// 添加单元格注释
			// 创建Drawing对象,Drawing是所有注释的容器.
			Drawing drawing = sheet.createDrawingPatriarch();
			// ClientAnchor是附属在WorkSheet上的一个对象， 其固定在一个单元格的左上角和右下角.
			ClientAnchor anchor = createHelper.createClientAnchor();
			// 设置注释位子
			anchor.setRow1(0);
			anchor.setRow2(2);
			anchor.setCol1(0);
			anchor.setCol2(2);
			// 定义注释的大小和位置,详见文档
			Comment comment = drawing.createCellComment(anchor);
			// 设置注释内容
			RichTextString str = createHelper.createRichTextString("Hello, World!");
			comment.setString(str);
			// 设置注释作者. 当鼠标移动到单元格上是可以在状态栏中看到该内容.
			comment.setAuthor("H__D");

			// 定义几行
			for (int rownum = 0; rownum < 30; rownum++) {
				// 创建行
				Row row = sheet.createRow(rownum);
				// 创建单元格
				Cell cell = row.createCell((short) 1);
				cell.setCellValue(createHelper.createRichTextString("Hello！" + rownum));// 设置单元格内容
				cell.setCellStyle(cellStyle);// 设置单元格样式
				cell.setCellType(Cell.CELL_TYPE_STRING);// 指定单元格格式：数值、公式或字符串
				cell.setCellComment(comment);// 添加注释

				// 格式化数据
				Cell cell2 = row.createCell((short) 2);
				cell2.setCellValue(11111.25);
				cell2.setCellStyle(cellStyle2);

				Cell cell3 = row.createCell((short) 3);
				cell3.setCellValue(new Date());
				cell3.setCellStyle(cellStyle3);

				sheet.autoSizeColumn((short) 0); // 调整第一列宽度
				sheet.autoSizeColumn((short) 1); // 调整第二列宽度
				sheet.autoSizeColumn((short) 2); // 调整第三列宽度
				sheet.autoSizeColumn((short) 3); // 调整第四列宽度

			}

			// 合并单元格
			sheet.addMergedRegion(new CellRangeAddress(1, // 第一行（0）
					2, // last row（0-based）
					1, // 第一列（基于0）
					2 // 最后一列（基于0）
			));

			// 保存
			String filename = "F:/TestPicture/workbook.xls";
			if (workbook instanceof XSSFWorkbook) {
				filename = filename + "x";
			}

			FileOutputStream out = new FileOutputStream(filename);
			workbook.write(out);
			out.close();
		}
	}

	@Test
	public void testUpdateExcel() throws EncryptedDocumentException, InvalidFormatException, IOException {

		InputStream inputStream = new FileInputStream("F:/TestPicture/workbook.xls");

		Workbook workbook = WorkbookFactory.create(inputStream);
		Sheet sheet = workbook.getSheetAt(0);
		Row row = sheet.getRow(2);
		Cell cell = row.getCell(3);
		if (cell == null)
			cell = row.createCell(3);
		cell.setCellType(Cell.CELL_TYPE_STRING);
		cell.setCellValue("a test");

		// Write the output to a file
		FileOutputStream fileOut = new FileOutputStream("F:/TestPicture/workbook.xls");
		workbook.write(fileOut);
		fileOut.close();

	}

	@Test
	public void testReadExcel() throws EncryptedDocumentException, InvalidFormatException, IOException {

		InputStream inputStream = new FileInputStream("F:/TestPicture/workbook.xls");

		Workbook workbook = WorkbookFactory.create(inputStream);
		Sheet sheet = workbook.getSheetAt(0);

		DataFormatter formatter = new DataFormatter();

		for (Row row : sheet) {
			for (Cell cell : row) {
				CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
				//单元格名称
				System.out.print(cellRef.formatAsString());
				System.out.print(" - ");

				//通过获取单元格值并应用任何数据格式（Date，0.00，1.23e9，$ 1.23等），获取单元格中显示的文本
				String text = formatter.formatCellValue(cell);
				System.out.println(text);

				//获取值并自己格式化
				switch (cell.getCellType()) {
					case Cell.CELL_TYPE_STRING:// 字符串型
						System.out.println(cell.getRichStringCellValue().getString());
						break;
					case Cell.CELL_TYPE_NUMERIC:// 数值型
						if (DateUtil.isCellDateFormatted(cell)) { // 如果是date类型则 ，获取该cell的date值
							System.out.println(cell.getDateCellValue());
						} else {// 纯数字
							System.out.println(cell.getNumericCellValue());
						}
						break;
					case Cell.CELL_TYPE_BOOLEAN:// 布尔
						System.out.println(cell.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_FORMULA:// 公式型
						System.out.println(cell.getCellFormula());
						break;
					case Cell.CELL_TYPE_BLANK:// 空值
						System.out.println();
						break;
					case Cell.CELL_TYPE_ERROR: // 故障
						System.out.println();
						break;
					default:
						System.out.println();
				}
			}
		}

	}
}
