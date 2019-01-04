# -*- coding: gbk -*-
# 注意：使用这个类的前提是正确安装 MySQLdb模块。
# 下载地址：http://www.codegood.com/archives/129

import MySQLdb


# 简单的MySQL应用类
class MySQLdbEx:
    # 关键参数
    _conn = None
    _cursor = None
    _paramDict = {}  # 存连接参数的字典
    _isConnected = False

    # 初始化(不主动连接)
    def __init__(self, host='localhost', port=3306, user='root', passwd='1qaz2wsx'):
        # 存连接参数
        self._paramDict["host"] = host
        self._paramDict["port"] = port
        self._paramDict["user"] = user
        self._paramDict["passwd"] = passwd
        
        self._isConnected = False

    # 建立连接
    def connect(self):
        if self._cursor or self._conn or self._isConnected:
            self.disconnect()  # 有连接，先断开
        # 连接数据库
        self._conn = MySQLdb.connect(host=self._paramDict["host"], port=self._paramDict["port"],
                                     user=self._paramDict["user"], passwd=self._paramDict["passwd"], charset='utf8')
        # 获取操作游标
        self._cursor = self._conn.cursor()
        self._isConnected = True
        return

    # 断开连接
    def disconnect(self):
        if self._cursor:
            self._cursor.close()
            self._cursor = None
        if self._conn:
            self._conn.close()
            self._conn = None
        self._isConnected = False
        return

    # 选择数据库
    def selectDB(self, dbName):
        if not self._isConnected:
            self.connect()
        self._conn.select_db(dbName)
        return

    # 执行语句
    def execute(self, sql_cmd):
        if not self._isConnected:
            return False
        self._cursor.execute(sql_cmd)
        self._conn.commit()  # 提交
        return True
    # 查询语句
    def select(self, sql_cmd):
        if not self._isConnected:
            return False
        self._cursor.execute(sql_cmd)
        rs = self._cursor.fetchall()
        return rs

        # ###################################
        # #MySQLdb 示例
        # #
        # ##################################
        # import MySQLdb
        #
        # #建立和数据库系统的连接
        # conn = MySQLdb.connect(host='localhost', user='root',passwd='1qaz2wsx')
        #
        # #获取操作游标
        # cursor = conn.cursor()
        # #执行SQL,创建一个数据库.
        # cursor.execute("""create database if not exists python""")
        #
        # #选择数据库
        # conn.select_db('python');
        # #执行SQL,创建一个数据表.
        # cursor.execute("""create table test(id int, info varchar(100)) """)
        #
        # value = [1,"inserted ?"];
        #
        # #插入一条记录
        # cursor.execute("insert into test values(%s,%s)",value);
        #
        # values=[]
        #
        #
        # #生成插入参数值
        # for i in range(20):
        # values.append((i,'Hello mysqldb, I am recoder ' + str(i)))
        # #插入多条记录
        #
        # cursor.executemany("""insert into test values(%s,%s) """,values);
        #
        # #关闭连接，释放资源
        # cursor.close();
