package com.iflytek.edu.kylin.command;

import com.iflytek.edu.kylin.client.KylinBackups;
import com.iflytek.edu.kylin.client.KylinImport;
import org.apache.log4j.Logger;

/**
 * Created with Intellij IDEA.
 * User: ztwu2
 * Date: 2018/5/5
 * Time: 16:06
 * Description 命令行运行
 */

public class RunCommand {

    private static final Logger logger = Logger.getLogger(RunCommand.class);

    public static void main(String[] args) {

        if (args != null && args.length > 0) {
            String command = "";
            String conf = "";
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-config=")) {
                    conf = args[i].substring("-config=".length());
                } else if (args[i].startsWith("-command=")) {
                    command = args[i].substring("-command=".length());
                }
            }

            logger.info("config:" + conf);
            logger.info("command:" + command);
            
            KylinBackups kylinBackups = new KylinBackups(conf);
            if ("backups:single".equals(command)) {
                kylinBackups.singleBackups();
            } else if ("backups:batch".equals(command)) {
                kylinBackups.batchBackups();
            }

            KylinImport kylinImport = new KylinImport(conf);
            if ("import:single".equals(command)) {
                kylinImport.singleImport();
            } else if ("import:batch".equals(command)) {
                kylinImport.batchImport();
            }
        } else {
            logger.info("传入参数");
        }
    }
}
