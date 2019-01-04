# -*- coding: gbk -*-
# ע�⣺ʹ��������ǰ������ȷ��װ MySQLdbģ�顣
# ���ص�ַ��http://www.codegood.com/archives/129

import MySQLdb


# �򵥵�MySQLӦ����
class MySQLdbEx:
    # �ؼ�����
    _conn = None
    _cursor = None
    _paramDict = {}  # �����Ӳ������ֵ�
    _isConnected = False

    # ��ʼ��(����������)
    def __init__(self, host='localhost', port=3306, user='root', passwd='1qaz2wsx'):
        # �����Ӳ���
        self._paramDict["host"] = host
        self._paramDict["port"] = port
        self._paramDict["user"] = user
        self._paramDict["passwd"] = passwd
        
        self._isConnected = False

    # ��������
    def connect(self):
        if self._cursor or self._conn or self._isConnected:
            self.disconnect()  # �����ӣ��ȶϿ�
        # �������ݿ�
        self._conn = MySQLdb.connect(host=self._paramDict["host"], port=self._paramDict["port"],
                                     user=self._paramDict["user"], passwd=self._paramDict["passwd"], charset='utf8')
        # ��ȡ�����α�
        self._cursor = self._conn.cursor()
        self._isConnected = True
        return

    # �Ͽ�����
    def disconnect(self):
        if self._cursor:
            self._cursor.close()
            self._cursor = None
        if self._conn:
            self._conn.close()
            self._conn = None
        self._isConnected = False
        return

    # ѡ�����ݿ�
    def selectDB(self, dbName):
        if not self._isConnected:
            self.connect()
        self._conn.select_db(dbName)
        return

    # ִ�����
    def execute(self, sql_cmd):
        if not self._isConnected:
            return False
        self._cursor.execute(sql_cmd)
        self._conn.commit()  # �ύ
        return True
    # ��ѯ���
    def select(self, sql_cmd):
        if not self._isConnected:
            return False
        self._cursor.execute(sql_cmd)
        rs = self._cursor.fetchall()
        return rs

        # ###################################
        # #MySQLdb ʾ��
        # #
        # ##################################
        # import MySQLdb
        #
        # #���������ݿ�ϵͳ������
        # conn = MySQLdb.connect(host='localhost', user='root',passwd='1qaz2wsx')
        #
        # #��ȡ�����α�
        # cursor = conn.cursor()
        # #ִ��SQL,����һ�����ݿ�.
        # cursor.execute("""create database if not exists python""")
        #
        # #ѡ�����ݿ�
        # conn.select_db('python');
        # #ִ��SQL,����һ�����ݱ�.
        # cursor.execute("""create table test(id int, info varchar(100)) """)
        #
        # value = [1,"inserted ?"];
        #
        # #����һ����¼
        # cursor.execute("insert into test values(%s,%s)",value);
        #
        # values=[]
        #
        #
        # #���ɲ������ֵ
        # for i in range(20):
        # values.append((i,'Hello mysqldb, I am recoder ' + str(i)))
        # #���������¼
        #
        # cursor.executemany("""insert into test values(%s,%s) """,values);
        #
        # #�ر����ӣ��ͷ���Դ
        # cursor.close();
