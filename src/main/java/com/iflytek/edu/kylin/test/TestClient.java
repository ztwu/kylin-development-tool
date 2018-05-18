package com.iflytek.edu.kylin.test;

import com.iflytek.edu.kylin.client.KylinBackups;
import com.iflytek.edu.kylin.client.KylinImport;

/**
 * Created with Intellij IDEA.
 * User: ztwu2
 * Date: 2018/5/4
 * Time: 12:52
 * Description
 */

public class TestClient {

    public static void main(String[] args){
        KylinBackups kylinBackups = new KylinBackups(".\\src\\main\\resources\\kylin-backups.properties");
        kylinBackups.singleBackups();
//        kylinBackups.batchBackups();

        KylinImport kylinImport = new KylinImport(".\\src\\main\\resources\\kylin-import.properties");
//        kylinImport.singleImport();
//        kylinImport.batchImport();
    }
}
