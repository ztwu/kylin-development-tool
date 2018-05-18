package com.iflytek.edu.kylin.tool;

import com.iflytek.edu.kylin.client.KylinBackups;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

/**
 * Created with Intellij IDEA.
 * User: ztwu2
 * Date: 2018/5/3
 * Time: 8:29
 * Description kylin restful apl 调用辅助类
 */

public class KylinApiUtil {

    private static final Logger logger = Logger.getLogger(KylinApiUtil.class);

    private static String encoding;
    private static String baseURL;
    private static String user;
    private static String passwd;

    static {
        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = KylinBackups.class.getResourceAsStream("/authentication.properties");
            properties.load(in);
            user = properties.getProperty("kylin.user");
            passwd = properties.getProperty("kylin.passwd");
            baseURL = properties.getProperty("kylin.url");
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String login() throws Exception{
        String method = "POST";
        String para = "/user/authentication";
        //Kylin的认证是basic authentication，加密算法是Base64，在POST的header进行用户认证
        //使用的用户和密码是（格式：username:password）使用Base64编码
        byte[] key = (user+":"+passwd).getBytes();
        encoding = new sun.misc.BASE64Encoder().encode(key);
        logger.info("Authorization : "+encoding);
        logger.info("用户认证");
        return excute(para,method,null);
    }

    /**
     * 获取项目下cube列表
     * @param offset
     * @param limit
     * @param cubeName
     * @param projectName
     * @return
     */
    public static String listCubes(int offset,
                                   int limit,
                                   String cubeName,
                                   String projectName ) throws Exception{
        String method = "GET";
        String para = "/cubes?"
                +"offset="+offset
                +"&limit="+limit
                +"&cubeName="+cubeName
                +"&projectName="+projectName;
        logger.info("获取cube列表");
        return excute(para,method,null);
    }

    /**
     * 获取cube的信息
     * @param cubeName
     * @return
     */
    public static String getCube(String cubeName) throws Exception{
        String method = "GET";
        String para = "/cubes/"+cubeName;
        logger.info("获取cube信息");
        return excute(para,method,null);
    }

    /**
     * 获取cube descriptor信息
     * @param cubeName
     * @return
     */
    public static String getCubeDes(String cubeName) throws Exception{
        String method = "GET";
        String para = "/cube_desc/"+cubeName;
        logger.info("获取cube描述");
        return excute(para,method,null);
    }

    /**
     * 获取model
     * @param modelName
     * @return
     */
    public static String getDataModel(String modelName) throws Exception{
        String method = "GET";
        String para = "/model/"+modelName;
        logger.info("获取model信息");
        return excute(para,method,null);
    }

    /**
     * 创建cube
     * @param body
     * @return
     */
    public static String createCube(String body) throws Exception{
        String method = "POST";
        String para = "/cubes";
        logger.info("创建cube");
        return excute(para,method,body);
    }

    /**
     * 创建model
     * @param body
     * @return
     */
    public static String createModel(String body) throws Exception{
        String method = "POST";
        String para = "/models";
        logger.info("创建model");
        return excute(para,method,body);
    }

    /**
     * 加载hive表
     * @param tables
     * @param projectName
     * @param body
     * @return
     * @throws Exception
     */
    public static String loadHiveTable(String tables,String projectName,String body) throws Exception {
        String method = "POST";
        String para = "/tables/"+tables+"/"+projectName;
        logger.info("加载hive表");
        return excute(para,method,body);
    }

    /**
     * 查询项目下的hive表
     * @param projectName
     * @param extOptional
     * @return
     * @throws Exception
     */
    public static String getHiveTables(String projectName,boolean extOptional) throws Exception {
        String method = "GET";
        String para = "/tables?project="+projectName+"&ext="+extOptional;
        return excute(para,method,null);
    }

    /**
     * 查询hive表
     * @param tableName
     * @return
     * @throws Exception
     */
    public static String getHiveTable(String tableName) throws Exception {
        String method = "GET";
        String para = "/tables/"+tableName;
        return excute(para,method,null);
    }

    /**
     * 查询hive表
     * @param tableName
     * @return
     * @throws Exception
     */
    public static String getHiveTableInfo(String tableName) throws Exception {
        String method = "GET";
        String para = "/tables/"+tableName+"/exd-map";
        return excute(para,method,null);
    }

    /**
     * 删除model
     * @param modelName
     * @return
     * @throws Exception
     */
    public static String deleteModel(String modelName) throws Exception {
        String method = "DELETE";
        String para = "/models/"+modelName;
        return excute(para,method,null);
    }

    /**
     * 删除cube
     * @param cubeName
     * @return
     * @throws Exception
     */
    public static String deleteCube(String cubeName) throws Exception {
        String method = "DELETE";
        String para = "/cubes/"+cubeName;
        return excute(para,method,null);
    }



    /**
     * http请求调用
     * @param para
     * @param method
     * @param body
     * @return
     */
    private static String excute(String para, String method, String body) throws Exception{
        StringBuilder out = new StringBuilder();
        try {
            URL url = new URL(baseURL+para);
            logger.info("url:"+url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setRequestProperty  ("Authorization", "Basic " + encoding);
            connection.setRequestProperty("Content-Type","application/json");
            if(body !=null){
                byte[] outputInBytes = body.getBytes("UTF-8");
                OutputStream os = connection.getOutputStream();
                os.write(outputInBytes);
                os.close();
            }
            InputStream content = connection.getInputStream();
            BufferedReader in  = new BufferedReader (new InputStreamReader (content,"UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                out.append(line);
            }
            in.close();
            connection.disconnect();
        } catch(Exception e) {
            logger.error(e.getMessage());
        }
        return out.toString();
    }
}