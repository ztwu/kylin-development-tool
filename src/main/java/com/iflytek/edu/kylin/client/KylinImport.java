package com.iflytek.edu.kylin.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.edu.kylin.tool.FileUtil;
import com.iflytek.edu.kylin.tool.KylinApiUtil;
import com.iflytek.edu.kylin.tool.TempletUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Created with Intellij IDEA.
 * User: ztwu2
 * Date: 2018/5/4
 * Time: 14:55
 * Description kylin cube导入
 */

public class KylinImport {

    private static final Logger logger = Logger.getLogger(KylinImport.class);

    private String backupsDir;
    private String projectSourceName;
    private String projectDestName;
    private String cubeName;
    private String cubeTime;

    private String modelNameSuffix;
    private String cubelNameSuffix;

    public KylinImport(String conf) {
        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(conf);
            properties.load(in);
            backupsDir = properties.getProperty("backups.dir");
            projectSourceName = properties.getProperty("project.source.name");
            projectDestName = properties.getProperty("project.dest.name");
            cubeName = properties.getProperty("cube.name");
            cubeTime = properties.getProperty("cube.time");

            modelNameSuffix = properties.getProperty("model.name.suffix");
            cubelNameSuffix = properties.getProperty("cube.name.suffix");

            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 单个cube导入
     */
    public void singleImport(){
        logger.info("读取备份配置文件......");
        logger.info("backups.dir : "+backupsDir);
        logger.info("project.source.name : "+projectSourceName);
        logger.info("project.dest.name : "+projectDestName);
        logger.info("cube.name : "+cubeName);
        logger.info("cube.time : "+cubeTime);

        String path = backupsDir
                +File.separator+projectSourceName
                +File.separator+cubeTime
                +File.separator+cubeName;
        //读取model json备份数据
        String modelPath = path+File.separator+"model.json";
        logger.info("读取备份配置文件路径 : "+modelPath);
        File modelFile = new File(modelPath);

        //读取cube json备份数据
        String cubePath = path+File.separator+"cube.json";
        logger.info("读取备份配置文件路径 : "+cubePath);
        File cubeFile = new File(cubePath);

        if(modelFile.exists()&&cubeFile.exists()){
            importCube(modelFile,cubeFile);
        }else {
            logger.info("没有找到对应的备份文件，请仔细检查备份cube是否存在");
        }
    }

    /**
     * 批量cube导入
     */
    public void batchImport(){
        logger.info("batch import");
        logger.info("读取备份配置文件......");
        logger.info("backups.dir : "+backupsDir);
        logger.info("project.source.name : "+projectSourceName);
        logger.info("project.dest.name : "+projectDestName);
        logger.info("cube.time : "+cubeTime);

        String paths = backupsDir
                +File.separator+projectSourceName
                +File.separator+cubeTime;
        File files = new File(paths);
        File[] tempList = files.listFiles();
        for(int i=0;i<tempList.length;i++){
            String path = tempList[i].getPath();
            logger.info("batch import 第"+(i+1)+"次开始导入");

            //读取model json备份数据
            String modelPath = path+File.separator+"model.json";
            logger.info("读取备份配置文件路径 : "+modelPath);
            File modelFile = new File(modelPath);

            //读取cube json备份数据
            String cubePath = path+File.separator+"cube.json";
            logger.info("读取备份配置文件路径 : "+cubePath);
            File cubeFile = new File(cubePath);

            if(modelFile.exists()&&cubeFile.exists()){
                importCube(modelFile,cubeFile);
            }else {
                logger.info("没有找到对应的备份文件，请仔细检查备份cube是否存在");
            }

            logger.info("batch import 第"+(i+1)+"次完成导入");

        }
    }

    /**
     * cube导入
     * @param modelFile
     * @param cubeFile
     */
    private void importCube(File modelFile, File cubeFile){
        try{
            //判断model是否存在，若存在，执行创建cube,不存在执行创建model
            JSONObject isModelRsData = isModel(modelFile);
            boolean isModel = isModelRsData.getBooleanValue("isModel");
            logger.info("model是否存在："+isModel);
            JSONObject modelTemplet = isModelRsData.getJSONObject("modelTemplet");

            boolean isSuccessModel = false;
            if(!isModel){
                logger.info("校验hive表加载情况......");
                if(validateHiveTable(modelTemplet)){

                    //load hive成功后暂停30秒之后执行创建model
                    logger.info("等待hive表数据同步，30秒后开始执行model创建过程......");
                    Thread.sleep(30000);

                    logger.info("开始执行model创建过程......");
                    JSONObject modelRsData = createModel(modelTemplet);
                    isSuccessModel = modelRsData.getBooleanValue("isSuccess");
                }else {
                    logger.error("hive表加载出现问题");
                }
            }else {
                logger.info("model已经存在");
                isSuccessModel = true;
            }
            if(isSuccessModel){
                boolean isCube = false;
                JSONObject cubeTemplet = null;
                JSONObject cubeRsData = isCube(cubeFile,modelTemplet);
                isCube = cubeRsData.getBooleanValue("isCube");
                cubeTemplet = cubeRsData.getJSONObject("cubeTemplet");
                if(!isCube){
                    //创建model成功后暂停30秒之后执行创建cube
                    logger.info("等待model数据同步，30秒后开始执行cube创建过程......");
                    Thread.sleep(30000);
                    createCube(cubeTemplet);
                }else {
                    logger.info("cube已经存在");
                }
            }else {
                logger.info("model创建失败，请检查原因");
            }
        } catch (Exception e){
            logger.error(e.getMessage());
        }
    }

    /**
     * 新建model
     * @param modelTemplet
     * @return
     * @throws Exception
     */
    private JSONObject createModel(JSONObject modelTemplet) throws Exception{
        JSONObject rsData = new JSONObject();

        logger.info("开始导入model...... : "+modelTemplet.toString());

        KylinApiUtil.login();
        String modelResult = KylinApiUtil.createModel(modelTemplet.toString());
        logger.info("执行创建model，返回数据 : "+modelResult);

        boolean isSuccess = false;
        if(StringUtils.isNotBlank(modelResult)){
            JSONObject result = JSONObject.parseObject(modelResult);
            if(result!=null){
                isSuccess = result.getBooleanValue("successful");
                if(isSuccess){
                    logger.info("创建model成功");
                }else {
                    logger.info("创建model报错");
                }
            }else {
                isSuccess = false;
                logger.info("创建model报错");
            }
        }else {
            isSuccess = false;
            logger.info("创建model报错");
        }
        rsData.put("modelTemplet",modelTemplet);
        rsData.put("isSuccess",isSuccess);
        return rsData;
    }

    /**
     * 新建cube
     * @param cubeTemplet
     * @throws Exception
     */
    private boolean createCube(JSONObject cubeTemplet) throws Exception{

        logger.info("开始导入cube...... : "+cubeTemplet.toString());
        KylinApiUtil.login();
        String cubeResult = KylinApiUtil.createCube(cubeTemplet.toString());
        logger.info("执行创建cube，返回数据 : "+cubeResult);

        boolean isSuccess = false;
        if(StringUtils.isNotBlank(cubeResult)){
            JSONObject result = JSONObject.parseObject(cubeResult);
            if(result!=null){
                isSuccess = result.getBooleanValue("successful");
                if(isSuccess){
                    logger.info("创建cube成功");
                }else {
                    logger.info("创建cube报错");
                }
            }else {
                isSuccess = false;
                logger.info("创建cube报错");
            }
        }else {
            isSuccess = false;
            logger.info("创建cube报错");
        }
        return isSuccess;
    }

    /**
     * 判断model是否存在
     * @param modelFile
     * @return
     * @throws Exception
     */
    private JSONObject isModel(File modelFile) throws Exception {

        JSONObject rsData = new JSONObject();

        String modelJson = FileUtil.read(modelFile);
        logger.info("读取本地的model备份文件 : "+modelJson);

        //创建model
        JSONObject modelJsonObject = JSONObject.parseObject(modelJson);
        JSONObject modelTemplet = TempletUtil.modelTemplet(modelJsonObject,projectSourceName,projectDestName,modelNameSuffix);

        //判断model是否已经存在
        boolean isSuccess = false;
        KylinApiUtil.login();
        String isModel = KylinApiUtil.getDataModel(modelTemplet.getJSONObject("modelDescData").getString("name"));
        if(StringUtils.isNotBlank(isModel)&&JSONObject.parseObject(isModel).getString("uuid")!=null){
            isSuccess = true;
        }else {
            isSuccess = false;
        }

        rsData.put("modelTemplet",modelTemplet);
        rsData.put("isModel",isSuccess);

        return  rsData;

    }

    /**
     * 判断cube是否存在
     * @param cubeFile
     * @param modelTemplet
     * @return
     * @throws Exception
     */
    private JSONObject isCube(File cubeFile, JSONObject modelTemplet) throws Exception {

        JSONObject rsData = new JSONObject();

        String cubeJson = FileUtil.read(cubeFile);
        logger.info("读取本地的cube备份文件 : "+cubeJson);

        logger.info("获取cube对应的model信息......");
        JSONObject modelDescData = modelTemplet.getJSONObject("modelDescData");
        String modelName = modelDescData.getString("name");
        logger.info("全名称modelName : "+modelName);
        String modelDefine = modelDescData.getString("name_define");
        logger.info("自定义名称modelDefine : "+modelDefine);

        ////创建cube
        JSONObject cubeJsonObject = JSONArray.parseArray(cubeJson).getJSONObject(0);
        JSONObject cubeTemplet = TempletUtil.cubeTemplet(cubeJsonObject,projectSourceName,projectDestName,modelName,modelDefine,cubelNameSuffix);

        //判断cube是否已经存在
        boolean isSuccess = false;
        KylinApiUtil.login();
        String isCube = KylinApiUtil.getCube(cubeTemplet.getJSONObject("cubeDescData").getString("name"));
        if(StringUtils.isNotBlank(isCube)&&JSONObject.parseObject(isCube).getString("uuid")!=null) {
             isSuccess = true;
        }else {
            isSuccess = false;
        }

        rsData.put("cubeTemplet",cubeTemplet);
        rsData.put("isCube",isSuccess);

        return  rsData;
    }

    /**
     * 校验判断对应hive表是否加载，如果未加载则自动针对model中使用的hive表进行加载
     * @param modelTemplet
     * @return
     */
    private boolean validateHiveTable(JSONObject modelTemplet) throws Exception{

        boolean isSuccess = false;

        //查询需要加载的hive表
        Set<String> tables = new HashSet<String>();
        JSONObject modelDescDataJson = modelTemplet.getJSONObject("modelDescData");
        if(modelDescDataJson!=null){
            String factTable = modelDescDataJson.getString("fact_table");
            if(StringUtils.isNotBlank(factTable)){
                tables.add(factTable);
                logger.info("事实表 : "+factTable);
            }
            JSONArray lookups = modelDescDataJson.getJSONArray("lookups");
            if(lookups!=null&&lookups.size()>0){
                for(int i=0;i<lookups.size();i++){
                    JSONObject item = lookups.getJSONObject(i);
                    if(item!=null){
                        String tableTemp = item.getString("table");
                        if(StringUtils.isNotBlank(tableTemp)){
                            tables.add(tableTemp);
                            logger.info("维度表 : "+tableTemp);
                        }
                    }
                }
            }
        }

        logger.info("判断hive是否已经加载"+tables.size());
        Set<String> needTables = new HashSet<String>();
        Iterator<String> iterable =  tables.iterator();
        while(iterable.hasNext()){
            String table = iterable.next();
            String rsData = KylinApiUtil.getHiveTable(table);
            if(StringUtils.isNotBlank(rsData)&&StringUtils.isNotBlank(JSONObject.parseObject(rsData).getString("uuid"))){
                iterable.remove();
                logger.info(table+" --- hive表已经存在");
            }else {
                logger.info(table+" --- hive表需要加载");
                needTables.add(table);
            }
        }

        if(needTables!=null&&needTables.size()>0){
            logger.info("需要load的hive表 : "+tables.size());
            logger.info("开始load hive表");

            String loadTables = "";
            for(String tableItem:tables){
                loadTables+=(tableItem+",");
            }
            KylinApiUtil.login();
            String loadResult = KylinApiUtil.loadHiveTable(loadTables,projectDestName,"{\"calculate\":true}");
            if(StringUtils.isNotBlank(loadResult)){
                JSONObject loadResultJson = JSONObject.parseObject(loadResult);
                if(loadResultJson!=null){
                    JSONArray loadedArray = loadResultJson.getJSONArray("result.loaded");
                    JSONArray unloadedArray = loadResultJson.getJSONArray("result.unloaded");
                    logger.info("load hive表共有"+loadedArray.size()+"张");
                    if(loadedArray.size()==tables.size()){
                        isSuccess = true;
                        logger.info("hive全部加载");
                    }else {
                        isSuccess = false;
                        logger.info("hive加载失败"+unloadedArray.size());
                    }
                } else {
                    isSuccess = false;
                    logger.info("load hive表失败，请仔细检查改项目下是否存在相应的hive");
                }
            }else {
                isSuccess = false;
                logger.info("load hive表失败，请仔细检查改项目下是否存在相应的hive");
            }
        } else {
            logger.info("该cube下hive表都已经存在");
            isSuccess = true;
        }
        return isSuccess;
    }

}
