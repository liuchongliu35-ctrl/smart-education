//package com.bing.tpa.utils;
//
//import com.bing.tpa.excelResource;
//import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
//import org.apache.poi.ss.usermodel.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import java.beans.IntrospectionException;
//import java.beans.PropertyDescriptor;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.lang.reflect.Field;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.text.DecimalFormat;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//@Component
//public class ExcelUtil {
//    private static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);
//
//    public List<Object> readExcel(String fileName, Class excel) {
//        List<Object> result;
//        try {
//            if(excel==null){
//                logger.warn("实体类对象为空，请检查实体类传值是否正确！");
//                return null;
//            }
//            result= ReadExcelByPOJO(fileName, 2, -1, excel);
//        }catch (Exception e){
//            logger.warn("ReadExcelByPOJO方法调用出现错误");
//            return null;
//        }
//        return result;
//    }
//
//    //获取workBook对象
//    private static Workbook getWorkBook(InputStream inputStream) throws IOException{
////        获取一个工作簿
//        Workbook workbook= null;
//        workbook = WorkbookFactory.create(inputStream);
//        return workbook;
//    }
//
//    //将一个单元格里的数据转换为String类型数据的方法
//    private static String convertCellValueToString(Cell cell){
//        if(cell==null){
//            return null;
//        }
//        String returnValue=null;
//        switch (cell.getCellType()){
////    cell.getDateCellValue()获取单元格里的数据的方法
//
//            case NUMERIC:   //数字
////                先判断是否为时间或日期类型
//                if (DateUtil.isCellDateFormatted(cell)) {
////                    年月日类型
//                    if (DateUtil.isCellInternalDateFormatted(cell)) {
//                        Date dateValue = cell.getDateCellValue();
//                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                        returnValue = sdf.format(dateValue);
//                    } else {
////                    时分秒类型
//                        Date dateValue = cell.getDateCellValue();
//                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//                        returnValue = sdf.format(dateValue);
//                    }
////              其他非时间类型
//                } else {
//                    Double doubleValue = cell.getNumericCellValue();
//                    // 格式化科学计数法，取一位整数，如取小数，值如0.0,取小数点后几位就写几个0
//                    DecimalFormat df = new DecimalFormat("0.00");
//                    returnValue = df.format(doubleValue);
//                }
//                break;
//            case STRING:    //字符串
//                returnValue = cell.getStringCellValue();
//                break;
//            case BOOLEAN:   //布尔
//                Boolean booleanValue = cell.getBooleanCellValue();
//                returnValue = booleanValue.toString();
//                break;
//            case BLANK:     // 空值
//                break;
//            case FORMULA:   // 公式
//                returnValue = cell.getCellFormula();
//                break;
//            case ERROR:     // 故障
//                break;
//            default:
//                break;
//        }
//        return returnValue;
//    }
//
//    //将excel表格里的内容赋给相应的成员变量，核心功能，主要功能就是将实体类对象和sheet表格中的数据联系在一起
//    private static <t> List<Object> HandleDataPOJO(Workbook workbook, int staterRow, int endRow, Class<?> t) throws IntrospectionException, NoSuchFieldException, IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException {
////       准备返回的结果集
//        List<Object> result=new ArrayList<>();
//
////       解析sheet
//        for(int sheetNum=0;sheetNum<workbook.getNumberOfSheets();sheetNum++){
////           获取sheet对象
//            Sheet sheet = workbook.getSheetAt(sheetNum);
//            if(sheet==null){
////               如果该sheet没有内容，就继续解析下一个sheet
//                continue;
//            }
//
////           处理该sheet的头部，将头部的数据放入集合中
//            ArrayList<String> sheetTop=new ArrayList<>();
////           获取头部的数据的个数
//            int firstRowNum = sheet.getFirstRowNum();
////           获取头部行对象数据
//            Row sheetRow = sheet.getRow(firstRowNum);
//            if(sheetRow==null){
//                if(result!=null){
//                    return result;
//                }
//                logger.warn("解析Excel失败，头部数据位空");
//                return null;
//            }
//            for(int i=0;i<sheetRow.getLastCellNum();i++){
////               将第一行的数据放入集合中
//                sheetTop.add(convertCellValueToString(sheetRow.getCell(i)));
//            }
//
////           处理excel表对应的实体类的成员变量和excel表头部数据的关系
//            Map<String, Object> pojoFiles = getPOJOFieldAndValue(t);
////           准备一个联系成员变量和excel个列的关系的map数组
//            Map<String,Object> excelToEntity=new HashMap<>();
//            for(int k=0;k<sheetTop.size();k++){
//                if(pojoFiles.get(sheetTop.get(k))!=null&&!pojoFiles.get(sheetTop.get(k)).equals("")){
//                    excelToEntity.put(String.valueOf(k),pojoFiles.get(sheetTop.get(k)));
////                   ！！！！！该操作将列环和对应的成员变量对应起来，后面只要获取列数就知道是对哪一个成员变量赋值
////                   此时excelToEntity是一个以k为key，以成员变量的名称为value的map集合
//                }
//            }
//
////           处理其他数据
//            int endRowNum=0;
//            if(endRow==-1){
//                endRowNum=sheet.getPhysicalNumberOfRows();
//            }else {
//                endRowNum=endRow<sheet.getPhysicalNumberOfRows()?endRow:sheet.getPhysicalNumberOfRows();
//            }
////           对一行的数据将他们和相应的成员变量匹配
//            for(int j=staterRow-1;j<endRowNum;j++){
//                Row row = sheet.getRow(j);
//                if(row==null){
//                    continue;
//                }
////               将cell中的数据赋值给成员变量,使用反射获取成员变量的对象
//                t tPojo=(t) t.newInstance();
//                for(Map.Entry<String,Object> map:excelToEntity.entrySet()){
////                   获取第row行，第map.getKey()列的数据
//                    String cellData = convertCellValueToString(row.getCell(Integer.parseInt(map.getKey())));
////                   根据成员变量的名称获取他的对象
//                    PropertyDescriptor pd = new PropertyDescriptor((String) map.getValue(), tPojo.getClass());
////                   获取该成员变量额的set方法
//                    Method writeMethod = pd.getWriteMethod();
////                   根据该成员变量的类型进行赋值
//                    Field field = tPojo.getClass().getDeclaredField((String) map.getValue());
//                    String typeName = field.getGenericType().getTypeName();
//                    if(typeName.endsWith("String")) {
////                       进行赋值
//                        writeMethod.invoke(tPojo,cellData);
//                    }else if(typeName.endsWith("Double")){
//                        Double data = Double.valueOf(cellData);
//                        writeMethod.invoke(tPojo,data);
//                    }else if(typeName.endsWith("Integer")){
//                        Integer data = Integer.valueOf(cellData);
//
//                        writeMethod.invoke(tPojo,data);
//                    }
//                }
//                result.add(tPojo);
//            }
//        }
//        return result;
//    }
//
//    //生成workbook对象并打开excel文件
//    private static List<Object> ReadExcelByPOJO(String fileName, int staterRow, int endRow, Class t) throws InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException, NoSuchFieldException {
//        //            先判断starter和endRow是否符合条件
//        if(staterRow>endRow&&endRow!=-1){
//            logger.warn("开始的行数大于结束的行数，请重新输入");
//            return null;
//        }
////       准备返回的结果集List<<Object>
//        List<Object> result=new  ArrayList<Object>();
//        Workbook workbook=null;
//        FileInputStream inputStream=null;
//        try{
////            先用file打开文件
//            File file=new File(fileName);
//            if(!file.exists()){
//                logger.warn("文件不存在！");
//                return null;
//            }
//            inputStream=new FileInputStream(file);
//            Workbook workBook = getWorkBook(inputStream);
//            result= HandleDataPOJO(workBook,staterRow,endRow,t);
//        }catch (Exception e){
//            logger.warn("解析文件"+fileName+"出现错误，错误信息为："+e.getMessage());
//
//        }finally {
////        关闭数据流
//            try {
//                if(workbook!=null){
//                    workbook.close();
//                }
//                if(inputStream!=null){
//                    inputStream.close();
//                }
//            }catch (Exception e){
//                logger.warn("关闭数据流出现错误，错误信息为："+e.getMessage());
//            }
//        }
//        return result;
//    }
//
//    //获取实体类的成员，将他们放入map集合中，以excelResource的value值作为key，以成员变量名作为value
//    private static Map<String, Object> getPOJOFieldAndValue(Class t){
//        //返回一个Map集合
//        Map<String,Object> resultFiled=new HashMap<>();
//
//        Field[] declaredFields = t.getDeclaredFields();
//        if(declaredFields!=null){
//            for(Field field:declaredFields){
//                excelResource excelResource = field.getAnnotation(excelResource.class);
//                if(excelResource!=null&&!excelResource.value().equals("")){
//                    resultFiled.put(excelResource.value(),field.getName());
//                }
//            }
//        }else {
//            logger.warn("excel表格使用的实体类没有成员变量");
//            return null;
//        }
//        return resultFiled;
//    }
//
//    //!!!!一下为选择性的获取sheet表格
//    private List<List<Object>> readExcelToPojo(Workbook workbook,int start,int end,Class t,String sheetNum) throws InstantiationException, IllegalAccessException, IntrospectionException, NoSuchFieldException {
//        List<List<Object>> result=new ArrayList<>();
//        if(sheetNum=="-1"){
//            Integer[] sheetNums=new Integer[workbook.getNumberOfSheets()];
//            for(int i=0;i<workbook.getNumberOfSheets();i++){
//                sheetNums[i]=i;
//            }
//            result=readExcel(workbook,start,end,t,sheetNums);
//        }else {
//            String[] split = sheetNum.split(",");
//            Integer[] sheetNums=new Integer[split.length];
//            for (int i=0;i<split.length;i++){
//                sheetNums[i]=Integer.valueOf(split[i]);
//            }
//            result=readExcel(workbook,start,end,t,sheetNums);
//        }
//        return result;
//    }
//
//    private <t> List<List<Object>> readExcel(Workbook workbook,int start,int end,Class t,Integer[] sheetNums) throws InstantiationException, IllegalAccessException, IntrospectionException, NoSuchFieldException {
//        List<List<Object>> result=new ArrayList<>();
//        List<Object> mid=new ArrayList<>();
//        for(int i=0;i<sheetNums.length;i++){
//            Sheet sheet = workbook.getSheetAt(sheetNums[i]);
//            if(sheet==null){
//                continue;
//            }
//
//            ArrayList<String> top=new ArrayList<>();
//            int firstRowNum = sheet.getFirstRowNum();
//            Row sheetRow = sheet.getRow(firstRowNum);
//            if(sheetRow==null){
//                if(result!=null){
//                    return result;
//                }
//                logger.warn("解析Excel失败，头部数据位空");
//                return null;
//            }
//            for (int j=0;j<sheetRow.getLastCellNum();j++){
//                top.add(convertCellValueToString(sheetRow.getCell(j)));
//            }
//
//            Map<String, Object> pojoValue= getPOJOFieldAndValue(t);
//            Map<String,Object> entryMap=new HashMap<>();
//            for(int k=0;k<top.size();k++){
//                if(pojoValue.get(top.get(k))!=null&&!pojoValue.get(top.get(k)).equals("")){
//                    entryMap.put(String.valueOf(k),pojoValue.get(top.get(k)));
//                }
//            }
//
//            int endNum=0;
//            if(end==-1){
//                endNum=sheet.getPhysicalNumberOfRows();
//            }else {
//                endNum=end<sheet.getPhysicalNumberOfRows()?end:sheet.getPhysicalNumberOfRows();
//            }
//            for(int m=start-1;m<endNum;m++){
//                Row row = sheet.getRow(m);
//                if(row==null){
//                    continue;
//                }
//                t pojo=(t) t.newInstance();
//                for(Map.Entry<String,Object> map:entryMap.entrySet()){
//                    String data = convertCellValueToString(row.getCell(Integer.parseInt(map.getKey())));
//                    PropertyDescriptor pd = new PropertyDescriptor((String) map.getValue(), pojo.getClass());
//                    Method method = pd.getWriteMethod();
//                    Field declaredField = pojo.getClass().getDeclaredField((String) map.getValue());
//                    String typeName = declaredField.getGenericType().getTypeName();
//                    if(typeName.endsWith("String")){
//
//                    }else if(typeName.endsWith("Double")){
//
//                    }else if(typeName.endsWith("Integer")){
//
//                    }
//                }
//                mid.add(pojo);
//            }
//            result.add(mid);
//        }
//        return result;
//    }
//
//}
