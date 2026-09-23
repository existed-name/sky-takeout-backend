package com.sky.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Apache POI 学习读写 Excel
 *
 * @CreateTime 2026/9/21 20:14
 */
@Slf4j
public class POITest {
    // ==================== 常量 ====================
    /** 输出文件绝对路径 */
    public static final String OUTPUT_PATH = "D:\\itheima.xlsx";
    /** Excel Sheet 名称 */
    public static final String SHEET_NAME = "零基础，学 AI，就来黑马程序员";
    /**
     * 起始下标 0
     */
    public static final int FIRST_INDEX = 0;
    /** 第 2 个下标 */
    public static final int SECOND_INDEX = 1;
    /** tab 制表符, 暂时没用 4 个空格 */
    public static final String TAB = "\t";
    /** 换行符 */
    private static final String LINE_BREAK = "\r\n";


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================

    /**
     * 将 Excel 文件写到{@link #OUTPUT_PATH}
     */
    @Test
    public void write(){
        try (
                // 在内存中创建 1 个 Excel 文件对象
                XSSFWorkbook excel = new XSSFWorkbook( );
        ){
            // 创建 sheet 页
            XSSFSheet sheet = excel.createSheet( SHEET_NAME );

            // 在 sheet 页创建行, 行数从下标 0 开始
            XSSFRow row1 = sheet.createRow( FIRST_INDEX );
            // 创建单元格、设置值, 第 1 个单元格下标从 0 开始
            row1.createCell( FIRST_INDEX ).setCellValue( "姓名" );
            row1.createCell( SECOND_INDEX ).setCellValue( "城市" );

            XSSFRow row2 = sheet.createRow( SECOND_INDEX );
            // 省略了第 1 列( 下标 0 ), Excel 好像是
            row2.createCell( FIRST_INDEX ).setCellValue( "萧炎" );
            row2.createCell( SECOND_INDEX ).setCellValue( "乌坦城" );

            XSSFRow row3 = sheet.createRow( 2 );
            row3.createCell( FIRST_INDEX ).setCellValue( "唐三" );
            row3.createCell( SECOND_INDEX ).setCellValue( "圣魂村" );

            File file = new File( OUTPUT_PATH );
            try(
                FileOutputStream fileOutputStream = new FileOutputStream(
                        file
                        // , true // append
                );
            ){
                excel.write( fileOutputStream );
                fileOutputStream.flush(); // try-with-resources 应该会自动刷新
            } catch ( IOException e ){
                log.error( "创建文件输出流或者写出 Excel 文件时发生异常" );
                throw new UncheckedIOException( e );
            }
        } catch ( IOException e ){
            log.error( "创建 Excel 对象失败" );
            throw new UncheckedIOException( e );
        }
    }

    /**
     * 从{@link #OUTPUT_PATH}读取 Excel 文件内容
     */
    @Test
    public void read(){
        File file = new File( OUTPUT_PATH );
        try (
                FileInputStream fileInputStream = new FileInputStream( file )
        ) {
            try {
                XSSFWorkbook sheets = new XSSFWorkbook( fileInputStream );
                XSSFSheet sheetGottenByIndex = sheets.getSheetAt( FIRST_INDEX );
                XSSFSheet sheetGottenByName = sheets.getSheet( SHEET_NAME );

                boolean equalsFlag = sheetGottenByName.equals( sheetGottenByIndex );
                log.info( "用下标 {} 获取的 sheet 是否 = 用名字 \"{}\" 获取的 sheet ? 结果 = {}",
                        FIRST_INDEX, SHEET_NAME, equalsFlag
                );

                // 现在就直接遍历每一行, 输出每行数据
                // 于是就先拿到所有的行数( 最后 1 行位置 )然后下标遍历每一行
                int lastRowNum = sheetGottenByName.getLastRowNum();
                StringBuilder stringBuilder = new StringBuilder();
                /*
                    * 运行发现，i < lastRowNum 少输出了 1 行
                    突然反应过来 rowNum 是从 0 开始的, lastRowNum 是最后一行的下标
                    那么这里应该用 <=
                    看源码注释也确实是返回从 0 开始的最后一行

                    * 然而单元格不是这样?
                      - 看 getLastCellNum 示例是 < 而不是 <=
                      - 实测如果 <= 那会输出 null
                 */
                for ( int i = 0; i <= lastRowNum; i++ ){
                    XSSFRow rowi = sheetGottenByName.getRow( i );
                    // 拿到某一行, 应该从左到右遍历单元格
                    short lastCellNum = rowi.getLastCellNum();

                    for ( short j = 0; j < lastCellNum; j++ ){
                        XSSFCell cell = rowi.getCell( j );
                        stringBuilder.append( cell )
                                .append( TAB );
                        if ( cell != null ){
                            String toString = cell.toString();
                            String stringCellValue = cell.getStringCellValue();
                            // 发现是一样的
                            log.info( "toString = {}, stringCellValue = {}", toString, stringCellValue );
                        }
                    }
                    stringBuilder.append( LINE_BREAK );
                }

                log.info( "展示 Excel sheet 的内容: {}{}", LINE_BREAK, stringBuilder );
            }
            catch ( IOException e ){
                log.error( "创建 XSSFWorkbook 失败, fileInputStream = {}", fileInputStream );
                throw new RuntimeException( e );
            }
        }
        catch ( FileNotFoundException e ){
            log.error( "文件输入流创建失败, 可能是路径无效、文件不存在/被其他进程占用" );
            throw new RuntimeException( e );
        } catch ( IOException e ){
            log.error( "文件输入流创建失败" );
            throw new RuntimeException( e );
        }
    }


    // ==================== 私有辅助方法 ====================

}
