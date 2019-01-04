# -*- coding: gbk -*-

import sys
import time
import traceback
import os
import json

import MyLib.common.LocalTimeUtil as LocalTimeUtil
import MyLib.common.MyConfig as MyConfig
import MyLib.common.MySQLdbEx as MySQLdbEx
from __builtin__ import isinstance
from _bsddb import version


logfileHandle = None
def log(info):
    global logfileHandle
    if logfileHandle == None:
        if os.path.exists("./log") is False:
            os.mkdir("./log")
        logfileHandle = open("./log/%s.log"%LocalTimeUtil.current_datetime()[0].strftime("%Y%m%d_%H-%M-%S"),"a+")
        
    print info
    logfileHandle.write(str(info)+"\n")
    
def closelogfile():
    global logfileHandle
    if logfileHandle != None:
        logfileHandle.close()

def getTableColumnsStr(tableColumns):
    log(len(tableColumns))
    result = ""
    for oneTuple in tableColumns:
        nameColumn = oneTuple[0]
        if result != "":
            result +=","
        result = result + "`"+nameColumn+"`"
    return result
#sql template
creatOnlyCmdTemplate = "create table %s.%s like %s.%s"
#copyCmdTemplate = "create table %s.%s select * from %s.%s"      
insertCmdTemplate = "INSERT INTO %s.%s SELECT * FROM %s.%s" #"INSERT INTO merger_result.demo_achievement_item SELECT * FROM merger2.demo_achievement_item;"
    
insertValueCmdTemplate = "INSERT INTO %s.%s(%s) (SELECT %s FROM %s.%s)" #"INSERT INTO merger_result.demo_achievement_item SELECT * FROM merger2.demo_achievement_item;"
##
#合服主逻辑处理
#
#表类型配置    数据库配置
def merger(tableConf, dbConf):
    #数据库表配置
#     Unmerger_Table = ["demo_compensation","demo_compensation_log","demo_competition_log","demo_cost_log",
#                       "demo_rare_item_log","demo_red_bag_record","demo_sociaty","demo_union_battle_damage","demo_union_battle_status",
#                       "demo_union_log","demo_union_pray"]
#     
#     Special_Table = ["core_id_allot","demo_global_param"]
#     
#     Merger_Table = ["demo_achievement_item","demo_activatekey","demo_angel","demo_buff","demo_children",
#                     "demo_combat_rank","demo_competition_champion","demo_competition_human","demo_competition_mirror","demo_driver",
#                     "demo_fill_mail","demo_friend","demo_gene","demo_general","demo_general_rank",
#                     "demo_giftactivate","demo_hallows","demo_hallows_glyphs","demo_human","demo_human_info",
#                     "demo_instance","demo_instance_rank","demo_ios_pay_order","demo_item","demo_level_rank",
#                     "demo_mail","demo_monster","demo_newactivity","demo_newactivity_data","demo_newactivity_game",
#                     "demo_newactivity_humandata","demo_newfriend","demo_newfriendobject","demo_newserviceactivity","demo_operate_activity",
#                     "demo_operate_record","demo_part","demo_part_mirror","demo_pay_check_code","demo_pay_log",
#                     "demo_pocket_line","demo_pve_tower_rank","demo_quest_item","demo_setting","demo_shop",
#                     "demo_store","demo_title","demo_union","demo_union_member","demo_unit_propplus"]
    global creatOnlyCmdTemplate
    global insertCmdTemplate
    global insertValueCmdTemplate
    #仅创建不需保留数据的表
    Unmerger_Table = eval(tableConf.get("table_setting","unmerger_table","[]"))
    #仅保留主服数据的表
    Special_Table = eval(tableConf.get("table_setting","special_table","[]"))
    #需要全部数据合并的表
    Merger_Table = eval(tableConf.get("table_setting","merger_table","[]"))
    #特殊处理的表
    Other_Table = eval(tableConf.get("table_setting","other_table","[]"))
#     #合服创建的表
#     New_Table = eval(tableConf.get("table_setting","new_table","[]"))
    
    #print "num Unmerger_Table:%s,Special_Table:%s,Merger_Table:%s"%(len(Unmerger_Table),len(Special_Table),len(Merger_Table))

    #合服组数
    groupCnt = int(dbConf.get("db_config","groupCnt","1")) 
    #分组执行
    for groupIndex in xrange(groupCnt):
        groupId = groupIndex + 1
        #合服数据库配置
        srcDbList = dbConf.get("db_config","srcDB_%d"%groupId,"").strip().split(",")   #源数据库名列表
        destDb = dbConf.get("db_config","destDB_%d"%groupId,"").strip()                #目标数据库名
        
        #数据库连接配置
        host = dbConf.get("db_config","host_%d"%groupId,"127.0.0.1").strip()
        port= int(dbConf.get("db_config","port_%d"%groupId,"3306"))
        user=dbConf.get("db_config","user_%d"%groupId,"root").strip()
        passwd=dbConf.get("db_config","passwd_%d"%groupId,"root").strip()
        
        log('执行第%d组数据库合并：'%groupId)
        #connect db
        db = MySQLdbEx.MySQLdbEx(host=host, port=port, user=user, passwd=passwd)
        db.selectDB(destDb)
#         charst = db.select("SHOW VARIABLES LIKE 'character_set_%'")
        db.execute("SET NAMES utf8");
#         charst2 = db.select("SHOW VARIABLES LIKE 'character_set_%'")
        #检查合服目标库必须为空，否则可能重复执行或配置错误
        try:
            #检查目标库中表数量
            destDb_tableCnt = db.select("SELECT count(TABLE_NAME) FROM information_schema.TABLES WHERE TABLE_SCHEMA ='%s';"%destDb)[0][0]
        except Exception:
            log('\nFail #Exception:\n%s\n' % traceback.format_exc())
            db.disconnect()
            exit(0)
            return
        
        if destDb_tableCnt > 0:
            log("目标数据库[%s]不为空，请检查配置是否正确"%destDb)
            db.disconnect()
            time.sleep(1)
            exit(0)
            return
        #创建merge_cave,merge_serverIds,merge_version表
        createMergeTables(db, destDb)
        log("%s ----------创建合服表结构成功"%(LocalTimeUtil.current_strtime()))
        #create table 
        log("%s ----------以主服[%s]为样本创建新数据库结构"%(LocalTimeUtil.current_strtime(), srcDbList[0]))
        time.sleep(1)
        for tableName in Unmerger_Table + Special_Table + Merger_Table + Other_Table:#create only 
            try:
                creatOnlyCmd = creatOnlyCmdTemplate%(destDb,tableName,srcDbList[0],tableName)
                log("%s ----- 创建表  %s  执行  %s"%(LocalTimeUtil.current_strtime(),tableName,creatOnlyCmd))
                db.execute(creatOnlyCmd)
            except Exception:
                log('\nFail #Exception:\n%s\n' % traceback.format_exc())
             
        log("%s ----------创建新数据库结构 完毕 \n\n"%(LocalTimeUtil.current_strtime()))
        time.sleep(2)
         
        #insert special from first
        log("%s ----------从主服[%s]拷贝特殊公共表数据到新库"%(LocalTimeUtil.current_strtime(), srcDbList[0]))
        time.sleep(1)
        for tableName in Special_Table: #copy 
            try:
                insertCmd = insertCmdTemplate%(destDb,tableName,srcDbList[0],tableName)
                log("%s ----- 拷贝表  %s  执行  %s"%(LocalTimeUtil.current_strtime(),tableName,insertCmd))
                db.execute(insertCmd)
            except Exception:
                log('\nFail #Exception:\n%s\n' % traceback.format_exc())
        log("%s ---------- 拷贝特殊公共表数据 完毕\n\n"%(LocalTimeUtil.current_strtime()))
        time.sleep(2)
        
        #insert others
        for i in xrange(len(srcDbList)):#insert 
            if srcDbList[i].strip() == "":
                continue
            log("%s ----------从[%s]拷贝独立表数据到新库"%(LocalTimeUtil.current_strtime(), srcDbList[i]))
            time.sleep(1)
            for tableName in Merger_Table:
                 
                 
                tableColumns = db.select("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_name ='%s' and table_schema ='%s';"%(tableName,destDb))
                tableColumnStr = getTableColumnsStr(tableColumns)
                try:
                    insertCmd = insertValueCmdTemplate%(destDb,tableName,tableColumnStr,tableColumnStr,srcDbList[i],tableName)
                    #insertCmd = insertCmdTemplate%(destDb,tableName,srcDbList[i],tableName)
                    log("%s ----- 拷贝表  %s 执行   %s"%(LocalTimeUtil.current_strtime(),tableName,str(insertCmd)))
                    db.execute(insertCmd)
                except Exception:
                    log('\nFail #Exception:\n%s\n' % traceback.format_exc())
                 
                  
            log("%s ----------从[%s]拷贝独立表数据 完毕\n\n"%(LocalTimeUtil.current_strtime(), srcDbList[i]))
        try:
            mergeLogic(db, destDb, srcDbList)
        except BaseException:
            log("-----合服处理出错:\n%s\n"%(traceback.format_exc()))
            return
        finally:
            db.disconnect()

def createMergeTables(db,destDb):
    log("%s ----------目标服开始创建合服新建表\n\n"%(LocalTimeUtil.current_strtime()))
    createMergeCaveSql = "CREATE TABLE `merge_cave` (`id` bigint(20) NOT NULL,`CaveJsonStr` varchar(2000) NOT NULL COMMENT '仙域json字符串',`Flag` int(11) NOT NULL COMMENT '处理标记：0-未处理 1-已处理',PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;"
    createMergeServerIdsSql = "CREATE TABLE `merge_server_ids` (`id` bigint(20) NOT NULL,`serverIds` varchar(6000) NOT NULL COMMENT '已合并服务器id列表',PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;"    
    createMergeVersionSql = "CREATE TABLE `merge_version` (`id` bigint(20) NOT NULL,`version` varchar(255) NOT NULL COMMENT '版本日期',`updatedVersion` varchar(255) NOT NULL COMMENT '当前已经执行同步版本日期',PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;"
    db.execute(createMergeCaveSql)
    db.execute(createMergeServerIdsSql)
    db.execute(createMergeVersionSql)
    log("%s ----------目标服开始创建合服新建表完成\n\n"%(LocalTimeUtil.current_strtime()))

def mergeLogic(db,destDb,srcDbList):
    log("%s ----------开始执行特殊表处理\n\n"%(LocalTimeUtil.current_strtime()))
    timeArr = [time.time()]
    # 合并成长基金表
    mergeActivityGlobal(db, destDb, srcDbList)
    mergeLog("activity_global", timeArr)
    # 按玩家总战力进行竞技场排名
    mergeCompeteHuman(db, destDb, srcDbList)
    mergeLog("compete_human", timeArr)
    #根据主服eventKey去重后合并fillMail全局邮件表
    mergeDistinct(db, destDb, srcDbList, "fill_mail", "EventKey")
    mergeLog("fill_mail", timeArr)
    #根据主服eventKey去重后合并notice公告表
    mergeDistinct(db, destDb, srcDbList, "notice", "EventKey")
    mergeLog("notice", timeArr)
    #根据主服id去重后合并friend好友表
    mergeFriend(db, destDb, srcDbList)
    mergeLog("friend", timeArr)
    #合并human主玩家表
    mergeHuman(db, destDb, srcDbList)
    mergeLog("human", timeArr)
    #记录仙域玩家挑战记录，存入mergeCave表
    mergeCave(db, destDb, srcDbList)
    mergeLog("cave", timeArr)
    log("%s ----------执行特殊表处理完成\n\n开始合并服务器id与记录合服版本号到目标服"%(LocalTimeUtil.current_strtime()))
    #合并服务器id
    mergeServerIds(db, destDb, srcDbList)
    mergeLog("merge_server_ids", timeArr)
    #记录合服版本号
    saveVersion(db, destDb)
    mergeLog("merge_version", timeArr)
    log("%s ----------\n合并服务器id与记录合服版本号到目标服完成，开始移除未合并主玩家数据的关联表数据"%(LocalTimeUtil.current_strtime()))
    #删除已合并的但未合并human表的关联表数据
    deleteAssHuman(db,destDb)
    log("%s ----------\n移除未合并主玩家数据的关联表数据完成"%(LocalTimeUtil.current_strtime()))
    
def mergeLog(tableName, timeArr):
    nowTime = time.time()
    log("{%s}表操作成功,耗时:%sS"%(tableName,str(nowTime-timeArr[0])))
    timeArr[0] = nowTime

def mergeActivityGlobal(db,destDb,srcDbList):
    for srcDb in srcDbList:
        mergeRowByRow(db, destDb, "activity_global", "id", "FundBuyCount", srcDb)

def mergeRowByRow(db,destDb,tableName,keyColumn,optColumn,srcDb):
    global insertValueCmdTemplate
    #从原服查询所有数据
    ids = db.select("select %s,%s from %s.%s"%(keyColumn,optColumn,srcDb,tableName));
    for i in xrange(len(ids)):
        kid = ids[i][0]
        val = str(ids[i][1])
        rows = db.select("select %s from %s.%s where %s=%s"%(optColumn,destDb,tableName,keyColumn,kid))
        #目标服不存在该行，插入
        if (len(rows)==0):
            tableColumns = db.select("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_name ='%s' and table_schema ='%s';"%(tableName,destDb))
            tableColumnStr = getTableColumnsStr(tableColumns)
            insertCmd = "INSERT INTO %s.%s(%s) (SELECT %s FROM %s.%s where %s=%s)"%(destDb,tableName,tableColumnStr,tableColumnStr,srcDb,tableName,keyColumn,kid)
            db.execute(insertCmd)
        else:
            #目标服存在该行，合并值
            db.execute("update %s.%s set %s=%s where %s=%s"%(destDb,tableName,optColumn,optColumn+"+"+val,keyColumn,kid))

def getIndex3(arr):
    return arr[3]

def mergeCompeteHuman(db,destDb,srcDbList):
    tableName = "compete_human"
    competeHumanDic = {}
    db.selectDB(destDb)
    tableColumns = db.select("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_name ='%s' and table_schema ='%s';" % (tableName, destDb))
    tableColumnStr = getTableColumnsStr(tableColumns).strip()
    for i in xrange(len(srcDbList)):
        db.selectDB(srcDbList[i])
        #排除机器人
        rows = db.select(("SELECT a.id,b.Combat,c.id,c.Combat,a.%s FROM compete_human a LEFT JOIN human b on a.id=b.id LEFT JOIN compete_partner c on a.id=c.HumanId where a.IsRobot<>1")%(tableColumnStr.replace(",",",a.")));
        for row in rows:
            humanId = row[0]
            combat = row[3]
            if competeHumanDic.has_key(humanId):
                competeHumanDic[humanId][3]+=combat
            else:
                competeHumanDic[humanId] = list(row)
    columnStrs = tableColumnStr.split(",")
    colIndex = columnStrs.index("`Rank`")
    toIndex = colIndex + 4
    competeList = []
    for val in competeHumanDic.values():
        competeList.append(val)
    competeList.sort(key=getIndex3, reverse=True)
    db.selectDB(destDb)
    insertSql = ""
    for i in xrange(len(competeList)):
        sqlVal = ""
        compete = competeList[i]
        for j in xrange(4, len(compete)):
            if j == toIndex:
                sqlVal += str(i + 1) + ","
            else:
                val = compete[j]
                if isinstance(val, unicode):
                    sqlVal += "'" + val + "',"
                else:
                    sqlVal += str(val) + ","
        insertSql += "("+sqlVal[:-1]+"),"
    insertSql = "INSERT INTO `%s` VALUES %s" %(tableName,insertSql[:-1])
    db.execute(insertSql)

def mergeDistinct(db,destDb,srcDbList,tableName,distinctKey,whereLimit="1=1"):
    eventKeys = {}
    rows = []
    db.selectDB(destDb)
    tableColumns = db.select("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_name ='%s' and table_schema ='%s';"%(tableName,destDb))
    tableColumnStr = getTableColumnsStr(tableColumns).strip()
    columnStrs = tableColumnStr.split(",")
    colIndex = columnStrs.index("`"+distinctKey+"`")
    for i in xrange(0,len(srcDbList)):
        mergeDistinctStep2(db, srcDbList[i], eventKeys, colIndex, tableColumnStr, rows, tableName, whereLimit)
    db.selectDB(destDb)
    insertSql = "";
    for row in rows:
        insertSql += "("
        for i in xrange(len(row)):
            val = row[i]
            if isinstance(val, unicode):
                insertSql += "'"+val+"',"
            else:
                insertSql += str(val)+","
        insertSql = insertSql[:-1]
        insertSql += "),"
    if len(insertSql) > 0:
        insertSql = "INSERT INTO `%s` VALUES %s"%(tableName,insertSql[:-1])
        db.execute(insertSql)

def mergeDistinctStep2(db,srcDb,eventKeys,eventKeyColIndex,tableColumnStr,rows,tableName,whereLimit):
    db.selectDB(srcDb)
    rows2 = db.select("SELECT %s from %s where %s"%(tableColumnStr,tableName,whereLimit));
    for row in rows2:
        col = str(row[eventKeyColIndex])
        if eventKeys.has_key(col):
            continue
        eventKeys[col] = True
        rows.append(row)

def mergeFriend(db,destDb,srcDbList):
    db.selectDB(destDb)
    tableColumns = db.select("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_name ='%s' and table_schema ='%s';"%("friend",destDb))
    tableColumnStr = getTableColumnsStr(tableColumns).strip()
    nowTime = str(int(round(time.time()*1000)))
    tableName = "friend"
    col = "id"
    for srcDb in srcDbList:
        sql = "INSERT INTO %s.%s(%s) (SELECT %s FROM %s.%s where %s.%s.%s>%s)"%(destDb,tableName,tableColumnStr,tableColumnStr,srcDb,tableName,srcDb,tableName,col,nowTime)
        db.execute(sql)
    mergeDistinct(db, destDb, srcDbList, tableName, col, "%s<%s"%(col,nowTime))
    
def mergeHuman(db,destDb,srcDbList):
    tableName = "human"
    dupColName = "Name"
    guildTableName = "guild"
    tableColumns = db.select("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_name ='%s' and table_schema ='%s';"%(tableName,destDb))
    tableColumnStr = getTableColumnsStr(tableColumns).strip()
    nowTime = int(round(time.time()*1000))
    timeLimit = str(nowTime-86400*30*1000)
    for srcDb in srcDbList:
        db.selectDB(srcDb)
        #满足以下所有情况的角色将删除账号数据      #1.角色等级小于等于22级。#2.无充值记录。#3.最近30天以内未登陆。#4.非军团团长或为军团团长但此军团只有玩家一人（同时军团解散）。
        insertCmd = "INSERT INTO %s.%s(%s) (SELECT a.%s FROM `%s` a LEFT JOIN `%s` b on a.GuildId=b.id where a.Level>20 or a.ChargeGold>0 or a.TimeLogin>%s or (a.guildId<>0 and b.GuildOwnNum>1 and b.GuildLeaderId=a.id))"%(destDb,tableName,tableColumnStr,tableColumnStr.replace(",",",a."),tableName,guildTableName,timeLimit)
        db.execute(insertCmd)
    db.selectDB(destDb)
    sqlNameDuplicates = "SELECT %s,count(*) as ct FROM `%s` GROUP BY `%s` HAVING ct>1"%(dupColName,tableName,dupColName)
    rows = db.select(sqlNameDuplicates)
    for row in rows:
        dupName = row[0]
        dupIdServerSql = "SELECT %s,%s FROM `%s` WHERE %s='%s'"%("id","ServerId",tableName,dupColName,dupName)
        dupIdServers = db.select(dupIdServerSql)
        for i in xrange (1,len(dupIdServers)):
            uid = dupIdServers[i][0]
            serverId = dupIdServers[i][1]
            newName = dupName+".s"+str(serverId%1000)
            updateSql = "UPDATE `%s` set %s='%s' where %s=%s"%(tableName,dupColName,newName,"id",uid)
            db.execute(updateSql)

def mergeCave(db,destDb,srcDbList):
    tableName = "cave"
    destTableName = "merge_cave"
    tableColumns = db.select("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_name ='%s' and table_schema ='%s';"%(tableName,destDb))
    tableColumnStr = getTableColumnsStr(tableColumns).strip()
    insertSql = ""
    rowSum = 0
    for srcDb in srcDbList:
        db.selectDB(srcDb)
        sqlTakenCaves = "SELECT c.id as guildId,a.%s from cave a left join human b on a.id=b.id left join guild c on b.guildid=c.id where a.humanid>0"%(tableColumnStr.replace(",", ",a."))
        rows = db.select(sqlTakenCaves)
        for row in rows:
            rowSum += 1
            mergeCaveEntity = {}
            guildId = row[0]
            mergeCaveEntity['guildId'] = guildId
            for i in xrange(1,len(row)):
                colName = tableColumns[i-1][0]
                mergeCaveEntity[colName] = row[i]
            insertSql += "("+str(rowSum)+",'"+json.dumps(mergeCaveEntity)+"',"+str(0)+"),"
    db.selectDB(destDb)
    if len(insertSql) > 0:
        insertSql = "INSERT INTO `%s` VALUES %s"%(destTableName,insertSql[:-1])
        if db.execute(insertSql) is False:
            raise Exception("{"+tableName+"}表操作失败")
            
def mergeServerIds(db,destDb,srcDbList):
    serverIdsArr = []
    tableName = "merge_server_ids"
    keyCol = 1
    serverIdTarCol = "serverIds"
    for srcDb in srcDbList:
        db.selectDB(srcDb)
        tableExistSql = "select 1 from information_schema.tables where TABLE_NAME='%s' and TABLE_SCHEMA='%s'"%(tableName,srcDb);
        tables = db.select(tableExistSql)
        flag = False
        if (len(tables) > 0):
            sqlServerIds = "SELECT %s FROM `%s` where %s=%s"%(serverIdTarCol,tableName,"id",keyCol)
            serverIdsStr = db.select(sqlServerIds)
            if len(serverIdsStr) > 0:
                flag = True
                serverIds = json.loads(serverIdsStr[0][0])
                for serverId in serverIds:
                    serverIdsArr.append(serverId)
        if flag is False:
            sqlServerId = "SELECT %s FROM `%s` LIMIT 0,1"%("ServerId","human")
            serverIdRows = db.select(sqlServerId)
            if len(serverIdRows) == 1:
                serverIdsArr.append(serverIdRows[0][0])
    db.selectDB(destDb)
    sqlInsertServerIdsSql = "INSERT INTO `%s` VALUES (%s,'%s')"%(tableName,keyCol,json.dumps(serverIdsArr))
    if db.execute(sqlInsertServerIdsSql) is False:
        raise Exception("{"+tableName+"}表操作失败")

def deleteAssHuman(db,destDb):
    db.selectDB(destDb)
    selectExistHumanIdsSql = "SELECT %s FROM `%s`"%("id","human")
    rows = db.select(selectExistHumanIdsSql)
    selectExistHumanIds = ""
    for row in rows:
        selectExistHumanIds += str(row[0])+","
    if (len(selectExistHumanIds) > 0):
        selectExistHumanIds = selectExistHumanIds[:-1]
        assHumanIds = {"achievement":"HumanId","achieve_title":"HumanId","activity_humandata":"HumanId","activity_seven":"humanID","card":"HumanId","compete_partner":"HumanId","culture_times":"HumanId","dropinfo":"HumanId","fashion":"id","human_ext_info":"id","human_simple":"id","human_skill":"id","inst_resource":"id","instance":"HumanId","item_bag":"OwnerId","item_body":"OwnerId","mail":"Receiver","partner":"HumanId","quest":"id","rune":"humanId","shop":"HumanId"}
        for key,value in assHumanIds.items():
            try:
                sql = "DELETE FROM `%s` WHERE %s NOT IN (%s)"%(key,value,selectExistHumanIds)
                db.execute(sql)
            except Exception:
                log('\nFail #Exception:\n%s\n' % traceback.format_exc())
    sql = "DELETE FROM `%s` WHERE %s NOT IN (SELECT %s FROM `%s`)"%("cimelia","PartnerId","id","partner")
    db.execute(sql)
    sql = "DELETE FROM `%s` WHERE %s NOT IN (SELECT %s FROM `%s` UNION SELECT %s FROM `%s`)"%("unit_propplus","id","id","human","id","partner")
    db.execute(sql)
    
def saveVersion(db,destDb):
    tableName = "merge_version"
    keyCol = 1
    versionStr = LocalTimeUtil.current_datetime()[0].strftime("%Y%m%d")
    sqlInsertVersionSql = "INSERT INTO `%s` VALUES (%s,%s,%s)"%(tableName,keyCol,versionStr,"''")
    if db.execute(sqlInsertVersionSql) is False:
        raise Exception("{"+tableName+"}表操作失败")
    
        
if __name__ == "__main__":
    startTick = LocalTimeUtil.curTimeStamp()
    log("%s ----------合服工具开始执行...\n"%LocalTimeUtil.current_strtime())
    log("接收到参数列表:" + str(sys.argv))
    log("当前工作路径：" + str(os.path))
    if len(sys.argv) < 2:
        log("参数未传入合服配置路径,请调整后重试!!!")
        time.sleep(1)
        exit(0)
    else:
        log("加载合服配置: "+sys.argv[1])
    
    
    tableConf = MyConfig.MyConfig("./conf/table.ini")
    dbConf = None
    try:
        dbConf = MyConfig.MyConfig(sys.argv[1])
    except:
        log("合服配置读取失败,请检查配置文件!!!")
        time.sleep(1)
        exit(0)

    print "即将开始执行合服逻辑", 
    for i in xrange(6):
        time.sleep(0.3)
        print ".",
    print "\n"
    time.sleep(0.3)
    log('当前编码格式:'+sys.getdefaultencoding())
    reload(sys)
    sys.setdefaultencoding('utf-8')
    log('设置编码格式为：'+sys.getdefaultencoding())
    merger(tableConf, dbConf)
    log("%s ----------合服执行完毕   总用时 %s秒"%(LocalTimeUtil.current_strtime(),(LocalTimeUtil.curTimeStamp() - startTick)/1000))
    
    closelogfile()
    
    time.sleep(1)
    exit(0)
