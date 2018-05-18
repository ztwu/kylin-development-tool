package com.iflytek.edu.kylin.tool;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with Intellij IDEA.
 * User: ztwu2
 * Date: 2018/5/4
 * Time: 14:45
 * Description model，cube创建json模板辅助类
 */

public class TempletUtil {

    /**
     * model模板
     * @param model
     * @param projectSourceName
     * @param projectDestName
     * @return
     */
    public static JSONObject modelTemplet(JSONObject model,
                                          String projectSourceName,
                                          String projectDestName,
                                          String modelNameSuffix){
        //项目名称
        //projectName
        //model全称
        String name = model.getString("name");
        //名称前缀
        String namePrefix = projectDestName;
        //定义名称
        String nameDefine = name.substring(projectSourceName.length()+1);
        if(StringUtils.isNotBlank(modelNameSuffix)){
            nameDefine = nameDefine+"_"+modelNameSuffix;
        }

        //model描述
        String description = model.getString("description");
        //生产新的名称
        name = namePrefix+"_"+nameDefine;
        //事实表
        String factTable = model.getString("fact_table");
        //维度表
        JSONArray lookups = model.getJSONArray("lookups");
        //维度列
        JSONArray dimensions = model.getJSONArray("dimensions");
        //度量列
        JSONArray metrics = model.getJSONArray("metrics");
        //分区描述
        JSONObject partitionDesc = model.getJSONObject("partition_desc");
        //过滤条件
        String filterCondition = model.getString("filter_condition");

        JSONObject data = new JSONObject();

        data.put("project",projectDestName);

        Map<String,Object> modelDescDataMap = new HashMap<String,Object>();
        modelDescDataMap.put("name",name);
        modelDescDataMap.put("name_prefix",namePrefix);
        modelDescDataMap.put("name_define",nameDefine);
        modelDescDataMap.put("description",description);
        modelDescDataMap.put("fact_table",factTable);
        modelDescDataMap.put("lookups",lookups);
        modelDescDataMap.put("filter_condition",filterCondition);
        modelDescDataMap.put("dimensions",dimensions);
        modelDescDataMap.put("metrics",metrics);
        modelDescDataMap.put("partition_desc",partitionDesc);
        modelDescDataMap.put("last_modified",0);
        String modelDescData = JSONObject.toJSONString(modelDescDataMap, SerializerFeature.WriteMapNullValue);
        data.put("modelDescData",modelDescData);

        //替换kylin中hive表对应的库名
        data = JSONObject.parseObject(data.toString().replaceAll(projectSourceName.toUpperCase(),projectDestName.toUpperCase()));

//        String modelTemp = data.toString();
//        modelTemp = "{\"modelDescData\":\"{  \\\"name\\\": \\\"edu_edcc_dev_ceshi12345678t91113111\\\",  \\\"name_prefix\\\": \\\"edu_edcc_dev_\\\",\\n  \\\"name_define\\\": \\\"ceshi123456789\\\",\\n  \\\"description\\\": \\\"\\\",\\n  \\\"fact_table\\\": \\\"EDU_BG_HOUSE.DW_UC_ORG_SCHOOL\\\",\\n  \\\"lookups\\\": [],\\n  \\\"filter_condition\\\": \\\"\\\",\\n  \\\"dimensions\\\": [\\n    {\\n      \\\"table\\\": \\\"EDU_BG_HOUSE.DW_UC_ORG_SCHOOL\\\",\\n      \\\"columns\\\": [\\n        \\\"SCHOOL_ID\\\"\\n      ]\\n    }\\n  ],\\n  \\\"metrics\\\": [\\n    \\\"SCHOOL_ID\\\"\\n  ],\\n  \\\"partition_desc\\\": {\\n    \\\"partition_date_column\\\": null,\\n    \\\"partition_date_start\\\": null,\\n    \\\"partition_type\\\": \\\"APPEND\\\",\\n    \\\"partition_date_format\\\": \\\"yyyy-MM-dd\\\",\\n    \\\"partition_time_column\\\": null,\\n    \\\"partition_time_start\\\": null\\n  },\\n  \\\"last_modified\\\": 0\\n}\",\"project\":\"edu_edcc_dev\"}";

        return data;
    }

    /**
     * cube模板
     * @param cube
     * @param projectSourceName
     * @param projectDestName
     * @return
     */
    public static JSONObject cubeTemplet(JSONObject cube,
                                     String projectSourceName,
                                     String projectDestName,
                                     String modelName,
                                     String modelDefine,
                                         String cubelNameSuffix){

        //项目名称
        //projectName
        //cube全称
        String name = cube.getString("name");
        //名称前缀
        String namePrefix = projectDestName;
        //定义名称
        String nameDefine = name.substring(projectSourceName.length()+1);
        if(StringUtils.isNotBlank(cubelNameSuffix)){
            nameDefine = nameDefine+"_"+cubelNameSuffix;
        }

        //生产新的名称
        name = namePrefix+"_"+nameDefine;
        //cube描述
        String description = cube.getString("description");
        //model全名称
        //model自定义名称
        //维度列
        JSONArray dimensions = cube.getJSONArray("dimensions");
        //度量列
        JSONArray measures = cube.getJSONArray("measures");
        //字典
        JSONArray dictionaries = cube.getJSONArray("dictionaries");
        //rowkey
        JSONObject rowkey = cube.getJSONObject("rowkey");
        //聚合组
        JSONArray aggregationGroups = cube.getJSONArray("aggregation_groups");
        //分区起始日期
        long partitionDateStart = cube.getLongValue("partition_date_start");
        //分区结束日期
        long partitionDateEnd = cube.getLongValue("partition_date_end");
        //通知列表
        JSONArray notifyList = cube.getJSONArray("notify_list");
        //通知信息级别
        JSONArray statusNeedNotify = cube.getJSONArray("status_need_notify");
        //hbase表segment，列簇
        JSONObject hbaseMapping = cube.getJSONObject("hbase_mapping");
        int retentionRange = cube.getIntValue("retention_range");
        //自动合并时间
        JSONArray autoMergeTimeRanges = cube.getJSONArray("auto_merge_time_ranges");
        //计算引擎
        int engineType = cube.getIntValue("engine_type");
        //存储级别
        int storageType = cube.getIntValue("storage_type");
        //kylin自定义服务配置
        JSONObject overrideKylinProperties = cube.getJSONObject("override_kylin_properties");

        JSONObject data = new JSONObject();

        data.put("project",projectDestName);

        Map<String,Object> cubeDataMap = new HashMap<String,Object>();
        cubeDataMap.put("name",name);
        cubeDataMap.put("name_prefix",namePrefix);
        cubeDataMap.put("name_define",nameDefine);
        cubeDataMap.put("description",description);
        cubeDataMap.put("model_name",modelName);
        cubeDataMap.put("model_define",modelDefine);
        cubeDataMap.put("dimensions",dimensions);
        cubeDataMap.put("measures",measures);
        cubeDataMap.put("dictionaries",dictionaries);
        cubeDataMap.put("rowkey",rowkey);
        cubeDataMap.put("aggregation_groups",aggregationGroups);
        cubeDataMap.put("partition_date_start",partitionDateStart);
        cubeDataMap.put("notify_list",notifyList);
        cubeDataMap.put("hbase_mapping",hbaseMapping);
        cubeDataMap.put("retention_range",retentionRange);
        cubeDataMap.put("status_need_notify",statusNeedNotify);
        cubeDataMap.put("auto_merge_time_ranges",autoMergeTimeRanges);
        cubeDataMap.put("engine_type",engineType);
        cubeDataMap.put("storage_type",storageType);
        cubeDataMap.put("override_kylin_properties",overrideKylinProperties);
        String cubeDescData = JSONObject.toJSONString(cubeDataMap, SerializerFeature.WriteMapNullValue);
        data.put("cubeDescData",cubeDescData);

        //替换kylin中hive表对应的库名
        data = JSONObject.parseObject(data.toString().replaceAll(projectSourceName.toUpperCase(),projectDestName.toUpperCase()));
        //替换掉项目队列
        data = JSONObject.parseObject(data.toString().replaceAll(projectSourceName,projectDestName));

//        String cubeTemp = data.toString();
//        cubeTemp = "{\"cubeDescData\":\"{\\n  \\\"name\\\": \\\"edu_edcc_dev_ceshi123\\\",\\n  \\\"name_prefix\\\": \\\"edu_edcc_dev_\\\",\\n  \\\"model_name\\\": \\\"edu_edcc_dev_ceshi123\\\",\\n  \\\"model_define\\\": \\\"\\\",\\n  \\\"description\\\": \\\"\\\",\\n  \\\"dimensions\\\": [\\n    {\\n      \\\"name\\\": \\\"SCHOOL_ID\\\",\\n      \\\"table\\\": \\\"EDU_BG_HOUSE.DW_UC_ORG_SCHOOL\\\",\\n      \\\"derived\\\": null,\\n      \\\"column\\\": \\\"SCHOOL_ID\\\"\\n    }\\n  ],\\n  \\\"measures\\\": [\\n    {\\n      \\\"name\\\": \\\"_COUNT_\\\",\\n      \\\"function\\\": {\\n        \\\"expression\\\": \\\"COUNT\\\",\\n        \\\"returntype\\\": \\\"bigint\\\",\\n        \\\"parameter\\\": {\\n          \\\"type\\\": \\\"constant\\\",\\n          \\\"value\\\": \\\"1\\\",\\n          \\\"next_parameter\\\": null\\n        },\\n        \\\"configuration\\\": {}\\n      }\\n    }\\n  ],\\n  \\\"dictionaries\\\": [],\\n  \\\"rowkey\\\": {\\n    \\\"rowkey_columns\\\": [\\n      {\\n        \\\"column\\\": \\\"SCHOOL_ID\\\",\\n        \\\"encoding\\\": \\\"dict\\\",\\n        \\\"isShardBy\\\": \\\"false\\\"\\n      }\\n    ]\\n  },\\n  \\\"aggregation_groups\\\": [\\n    {\\n      \\\"includes\\\": [\\n        \\\"SCHOOL_ID\\\"\\n      ],\\n      \\\"select_rule\\\": {\\n        \\\"hierarchy_dims\\\": [],\\n        \\\"mandatory_dims\\\": [],\\n        \\\"joint_dims\\\": []\\n      }\\n    }\\n  ],\\n  \\\"partition_date_start\\\": 0,\\n  \\\"notify_list\\\": [],\\n  \\\"hbase_mapping\\\": {\\n    \\\"column_family\\\": [\\n      {\\n        \\\"name\\\": \\\"f1\\\",\\n        \\\"columns\\\": [\\n          {\\n            \\\"qualifier\\\": \\\"m\\\",\\n            \\\"measure_refs\\\": [\\n              \\\"_COUNT_\\\"\\n            ]\\n          }\\n        ]\\n      }\\n    ]\\n  },\\n  \\\"retention_range\\\": \\\"0\\\",\\n  \\\"status_need_notify\\\": [\\n    \\\"ERROR\\\",\\n    \\\"DISCARDED\\\",\\n    \\\"SUCCEED\\\"\\n  ],\\n  \\\"auto_merge_time_ranges\\\": [\\n    604800000,\\n    2419200000\\n  ],\\n  \\\"engine_type\\\": 2,\\n  \\\"storage_type\\\": 2,\\n  \\\"override_kylin_properties\\\": {\\n    \\\"kylin.job.mr.config.override.mapreduce.job.queuename\\\": \\\"edu_edcc_dev\\\",\\n    \\\"kylin.hive.config.override.mapreduce.job.queuename\\\": \\\"edu_edcc_dev\\\"\\n  },\\n  \\\"name_define\\\": \\\"ceshi\\\"\\n}\",\"project\":\"edu_edcc_dev\"}";

        return data;
    }

}
