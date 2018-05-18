package com.iflytek.edu.kylin.tool;

import java.io.*;

/**
 * Created with Intellij IDEA.
 * User: ztwu2
 * Date: 2018/5/4
 * Time: 14:37
 * Description 文件读写辅助类
 */

public class FileUtil {

    public static String read(File file){
        String laststr="";
        BufferedReader reader=null;
        try{
            FileInputStream in = new FileInputStream(file);
            reader=new BufferedReader(new InputStreamReader(in,"UTF-8"));// 读取文件
            String tempString=null;
            while((tempString=reader.readLine())!=null){
                laststr=laststr+tempString;
            }
            reader.close();
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if(reader!=null){
                try{
                    reader.close();
                }catch(IOException el){
                }
            }
        }
        return laststr;
    }

    /**
     * 写入文件
     * @param file
     * @param data
     */
    public static  void write(File file, String data){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false), "UTF-8"));
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
