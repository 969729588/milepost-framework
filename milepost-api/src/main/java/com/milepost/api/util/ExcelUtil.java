package com.milepost.api.util;

import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Huarf
 * 2018年3月7日
 */
public class ExcelUtil {

	/**
	 * 支持单个sheet的excel，
	 * 必须把所有的excel中所有有内容的单元格的格式设置为“文本”，否则会出现各样的异常，
	 * excel中无表头和内容之分，
	 * @param inputStream 读取数据之后会关闭输入流，
	 * @return 返回的结果中，map的key是单元格名称，即单元格坐标，如A1、A2、B3等，这里的map使用的是LinkedHashMap实现类，可以保持k-v对的顺序与excel中相同，
	 * @throws Exception
	 */
	public static List<LinkedHashMap<String, String>> read(InputStream inputStream ) throws Exception {
		
		List<LinkedHashMap<String, String>> list = null;
		Workbook workbook = null;
		
		try {
			
			workbook = WorkbookFactory.create(inputStream);
			//只支出单个sheet的excel
			Sheet sheet = workbook.getSheetAt(0);
	        DataFormatter formatter = new DataFormatter();
	        
	        list = new ArrayList<LinkedHashMap<String, String>>();
	        
	        for (Row row : sheet) {
	        	LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
	            for (Cell cell : row) {
	                CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
	                //单元格名称，即单元格坐标，使用该值作为map的key
	                String cellName = cellRef.formatAsString();

	                //通过获取单元格值并应用任何数据格式（Date，0.00，1.23e9，$ 1.23等），获取单元格中显示的文本
	                String cellText = formatter.formatCellValue(cell);
	                map.put(cellName, cellText);
	            }
	            list.add(map);
	        }
		} finally {
			IOUtils.closeQuietly(workbook);
			IOUtils.closeQuietly(inputStream);
		}
		
		return list;
	}
	
	/**
	 * 
	 * 用给定的数据生成一个.xlsx类型的Workbook对象，可以使用此对象的workbook.write(outputStream);方法将内容写到输出流中，
	 * list中的数据无表头和内容之分，
	 * @param list 这里不在关注LinkedHashMap的key，只是按照顺序依次铺在excel中，因此请保证所有LinkedHashMap中k-v对个数相同，否则会出现单元格错位的情况，
	 * @return
	 * @throws Exception
	 */
	public static Workbook list2Workbook(List<LinkedHashMap<String, String>> list) throws Exception {
		
		//工作薄
		Workbook workbook = new XSSFWorkbook();//.xlsx类型的工作簿
		
		// 得到一个POI的工具类
		CreationHelper createHelper = workbook.getCreationHelper();

		// 在Excel工作簿中建一工作表，其名为缺省值, 也可以指定Sheet名称，一个EXCEL至少要有一个 sheet，否则打开的时候就会出错
		Sheet sheet = workbook.createSheet();

		//设置CELL格式为文本格式  
		CellStyle cellStyle = workbook.createCellStyle();  
		DataFormat format = workbook.createDataFormat();  
        cellStyle.setDataFormat(format.getFormat("@"));  
		
		for (int rownum = 0; rownum < list.size(); rownum++) {
			// 创建行
			Row row = sheet.createRow(rownum);
			LinkedHashMap<String, String> map = list.get(rownum);
			int cellnum = 0;
			for(Map.Entry<String, String> entry : map.entrySet()){
				String value = entry.getValue();
				// 创建单元格
				Cell cell = row.createCell(cellnum);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(createHelper.createRichTextString(value));// 设置单元格内容
				cellnum ++;
			}
		}
		
		//依据第一个map的k-v对个数调整列宽
		if(list.get(0) != null){
			int cellnum = list.get(0).size();
			for(int i=0; i<cellnum; i++){
				sheet.autoSizeColumn(i);
			}
		}

		//workbook.write(outputStream);
		
		return workbook;
	}
}
