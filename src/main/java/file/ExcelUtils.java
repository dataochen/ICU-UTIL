package file;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dataochen
 * @Description excel数据工具类
 * @date: 2018/1/11 20:53
 */
public class ExcelUtils {

    /**
     * 导出文件格式07
     */
    public static final String EXPORT_TYPE_XLS = "xls";
    /**
     * 导出文件格式xlsx
     */
    public static final String EXPORT_TYPE_XLSX = "xlsx";
    /**
     * 批量导入文案国际化信息模板文件名
     */
    public static final String CODE_INTER_NAME = "codeInterTemplate.xls";
    /**
     * 业务单导出SHEET
     */
    public static final String BANK_BRANCH_SHEET_NAME = "sheet01";
    /**
     * 最大导出数据数量
     */
    public static final Integer EXPORT_MAX_COLUMNS = 1000;
    /**
     * 数据为空时 导入文件的字符
     */
    public static final String EXPORT_NULL_SYMBOL = "-";
    /**
     *批量导入info国际化信息模板文件第一列
     */
    public static final String[] MULTI_LANGUAGE_SHEET_COLUMN_NAME = {"tag","infoCode","applicationCode","simplifiedDescription","traditionalDescription","enDescription"};


    /**
     * 不包含单元格合并的写excel
     * excel导出到输出流
     * 谁调用谁负责关闭输出流
     *
     * @param os           输出流
     * @param excelExtName excel文件的扩展名，支持xls和xlsx，不带点号
     * @param data
     * @throws IOException
     */
    public static void writeExcel(OutputStream os, String excelExtName, Map<String, List<List<String>>> data) throws Exception {
        Workbook wb = null;
        try {
            if ("xls".equals(excelExtName)) {
                wb = new HSSFWorkbook();
            } else if ("xlsx".equals(excelExtName)) {
                wb = new XSSFWorkbook();
            } else {
                throw new Exception("文件类型只支持xls,xlsx");
            }
            for (String sheetName : data.keySet()) {
                Sheet sheet = wb.createSheet(sheetName);
                List<List<String>> rowList = data.get(sheetName);
                for (int i = 0; i < rowList.size(); i++) {
                    List<String> cellList = rowList.get(i);
                    Row row = sheet.createRow(i);
                    for (int j = 0; j < (cellList.size() > EXPORT_MAX_COLUMNS ? EXPORT_MAX_COLUMNS : cellList.size()); j++) {
                        Cell cell = row.createCell(j);
                        cell.setCellValue(cellList.get(j));
                    }
                }
            }
            wb.write(os);
        } catch (Exception e) {
            throw e;
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
    }


    /**
     * 读取excel工具类
     * Map<String, List<Map<String,String>>> key:sheetName,value的map是指 以第一行为key,后面对应列的值为value的集合，list代表的是所有行。
     * eg:
     * sheet01
     * 姓名  年龄
     * 张三  14
     * 李思 34
     * <p>
     * {
     * "sheet01": [
     * {
     * "张三": "14",
     * "李思": "34"
     * }
     * ]
     * }
     *
     * @param file
     * @return
     * @throws Exception
     */
    //读取excel
    public static Map<String, List<Map<String, String>>> readExcel(MultipartFile file) throws Exception {
        Workbook wb = null;
        if (file == null) {
            return null;
        }
        String extString = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        InputStream is = file.getInputStream();
        if (".xls".equals(extString)) {
            wb = new HSSFWorkbook(is);
        } else if (".xlsx".equals(extString)) {
            wb = new XSSFWorkbook(is);
        } else {
            throw new Exception("文件类型只支持xls,xlsx");
        }
        //用来存放表中数据
        HashMap<String, List<Map<String, String>>> result = new HashMap<>();
        int numberOfSheets = wb.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
//            每一个sheet
            List<Map<String, String>> maps = new ArrayList<>();
            String columns[] = new String[100];
            String sheetName = wb.getSheetName(i);
            Sheet sheetAt = wb.getSheetAt(i);
            //获取最大行数
            int rownum = sheetAt.getPhysicalNumberOfRows();
            //                j:行
            for (int j = 0; j < rownum; j++) {
                HashMap<String, String> map = new HashMap<>();
                //获取第j行
                Row row = sheetAt.getRow(j);
                //获取最大列数
                int colnum = row.getPhysicalNumberOfCells();
//                k:列
                for (int k = 0; k < colnum; k++) {
                    String cellData = (String) getCellFormatValue(row.getCell(k));
                    if (j == 0) {
//                        第一行作为map的key
                        columns[k] = cellData;
                    } else {
                    map.put(columns[k], cellData);
                    }
                }
                if (j != 0) {
                    maps.add(map);
                }
            }
            result.put(sheetName, maps);
        }
        return result;
    }

    /**
     * 获取元素值
     * @param cell
     * @return
     */
    private static Object getCellFormatValue(Cell cell) {
        Object cellValue = null;
        if (cell != null) {
            //判断cell类型
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_NUMERIC: {
                    cellValue = String.valueOf(cell.getNumericCellValue());
                    break;
                }
                case Cell.CELL_TYPE_FORMULA: {
                    //判断cell是否为日期格式
                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                        //转换为日期格式YYYY-mm-dd
                        cellValue = cell.getDateCellValue();
                    } else {
                        //数字
                        cellValue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                case Cell.CELL_TYPE_STRING: {
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                }
                default:
                    cellValue = "";
            }
        } else {
            cellValue = "";
        }
        return cellValue;
    }

}
