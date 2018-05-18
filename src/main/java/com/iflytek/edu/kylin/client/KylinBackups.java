package com.iflytek.edu.kylin.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.edu.kylin.tool.KylinApiUtil;
import com.iflytek.edu.kylin.tool.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

/**
 * Created with Intellij IDEA.
 * User: ztwu2
 * Date: 2018/5/4
 * Time: 12:50
 * Description kylin备份cube
 */

public class KylinBackups {

    private static final Logger logger = Logger.getLogger(KylinBackups.class);

    private String backupsDir;
    private String projectName;
    private String cubeName;

    public KylinBackups(String conf){
        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(conf);
            properties.load(in);
            backupsDir = properties.getProperty("backups.dir");
            projectName = properties.getProperty("project.name");
            cubeName = properties.getProperty("cube.name");
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 单个cube备份
     */
    public void singleBackups(){
        logger.info("cube.name : "+cubeName);
        backups(cubeName);
    }

    /**
     * 批量cube备份
     */
    public void batchBackups(){
        logger.info("batch backups");
        String listCubes = null;
        try {
            KylinApiUtil.login();
            listCubes = KylinApiUtil.listCubes(0,10,cubeName,projectName);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        JSONArray cubes = JSONArray.parseArray(listCubes);
        if(cubes!=null&&cubes.size()>0){
            for(int i=0;i<cubes.size();i++){
                JSONObject item = cubes.getJSONObject(i);
                String cubeName = item.getString("name");
                logger.info("batch backups 第"+(i+1)+"次开始备份......");
                logger.info("batch backups 第"+(i+1)+"次备份cube名称 : "+cubeName);
                backups(cubeName);
                logger.info("batch backups 第"+(i+1)+"次完成备份......");
            }
        }else {
            logger.warn("没有查询到cube");
        }
    }

    /**
     * cube备份
     * @param cubeName
     */
    private void backups(String cubeName){
        try{
            logger.info("读取备份配置文件......");
            logger.info("backups.dir : "+backupsDir);
            logger.info("project.name : "+projectName);

            Calendar calendar = Calendar.getInstance();
            String timeTemp = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());

            //生成备份文件目录，规则为自定义目录前缀+项目名+cube名+时间
            String filePath = backupsDir
                    +File.separator+projectName
                    +File.separator+timeTemp
                    +File.separator+cubeName;
            File file = new File(filePath);
            if(file.exists()){
                logger.info("清空已经存在的备份目录");
                file.delete();
            }
            file.mkdirs();
            logger.info("生成备份目录 : "+file.getPath());

            //生成cube备份文件
            File cubeFile = new File(file.getPath()+File.separator+"cube.json");
            if(cubeFile.exists()){
                cubeFile.delete();
            }
            try {
                cubeFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.info("生成cube备份文件 : "+cubeFile.getPath());

            //生成model备份文件
            File modelFile = new File(file.getPath()+File.separator+"model.json");
            if(modelFile.exists()){
                modelFile.delete();
            }
            try {
                modelFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            logger.info("生成model备份文件 : "+modelFile.getPath());

            logger.info("开始备份文件......");
            logger.info("获取cube segment属性的json文件");

            KylinApiUtil.login();

            String cubeData = KylinApiUtil.getCubeDes(cubeName);
            logger.info("cube json : "+cubeData);

            if(StringUtils.isNotBlank(cubeData)){
                //写入
                logger.info("开始写入cube备份文件......");
                FileUtil.write(cubeFile,cubeData);
                logger.info("完成写入cube备份文件......");

                JSONArray jsonArrayCube = JSONArray.parseArray(cubeData);
                JSONObject jsonCube = jsonArrayCube.getJSONObject(0);
                String modelName = jsonCube.getString("model_name");

                String modelData = KylinApiUtil.getDataModel(modelName);
                logger.info("model json : "+modelData);

                //写入
                logger.info("开始写入model备份文件......");
                FileUtil.write(modelFile,modelData);
                logger.info("完成写入model备份文件......");
            }else {
                logger.info("没有查询到该cube的相关信息，请仔细确认下cube名称是否正确");
                logger.info("清空备份目录");
                File[] list = file.listFiles();
                if(list!=null&&list.length>0){
                    for(File item:list){
                        item.delete();
                    }
                }
                file.delete();
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
    }

}
