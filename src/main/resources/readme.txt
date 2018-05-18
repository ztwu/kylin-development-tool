## bin ----执行脚本
## conf ----配置文件
## data ----备份文件路径
## lib ----jar包
## log ----日志


## bin -------------------------执行脚本
修改，配置自己jdk路径，如果已经设置jdk环境变量，可以直接删除配置jdk

##单个cube备份
run.bat 1

##批量cube备份
run.bat 1b

##单个cube导入
run.bat 2

##批量cube导入
run.bat 2b


## conf ------------------------配置文件
kylin-backups.properties备份配置文件
kylin-import.properties复制配置文件


（注意：备份路径为 项目名称+"/"+备份时间+"/"+cube名称）
进行备份之前，修改conf下的配置文件，主要修改备份的项目名称，此处目前可以默认不修改，为edu_edmp_base_dev，即开发环境项目；
单个cube备份时，修改对应的cube名称，
批量cube备份是，支持cube名称模糊匹配，例如考试模块肯定包含exam，可以使用exam去模糊匹配之后批量备份

进行复制之前，修改conf下的配置文件，主要修改备份的项目名称，此处目前可以默认不修改，为edu_edmp_base_dev，即开发环境项目；
筛选备份的时间版本;
单个cube复制时，修改cube名称
批量复制，会复制备份路径下（项目名称+"/"+备份时间）所有的文件


