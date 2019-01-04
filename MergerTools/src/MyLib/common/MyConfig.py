# -*- coding: gbk -*-

import ConfigParser
import os
import sys

#ini���ö�д��
class MyConfig:
    "configParser wrapper class"
    __iniFile = ""
    __confInstance = None
    __outFp = None

    #��ʼ��
    def __init__(self,filePath):
        self.__iniFile = filePath.strip()
        self.loadFormFile()

    #�Ƿ�װ�سɹ�
    def isLoadOK(self):
        return self.__confInstance != None

    #װ������
    def loadFormFile(self):
        sourcePath = sys.path[0]+"/../"+self.__iniFile
        print "sourcePath:"+sourcePath
        if not os.path.isfile(sourcePath):
            self.__confInstance = None
            return
        self.__confInstance = ConfigParser.ConfigParser()
        self.__confInstance.read(sourcePath)

    #��ȡ����
    def get(self, section, option, defValue=None):
        if self.__confInstance == None:
            return defValue
        if not self.__confInstance.has_option(section, option):
            return defValue
        return self.__confInstance.get(section, option, False, None)

    #д������
    def set(self, section, option, value=None):
        if self.__confInstance == None:
            return False
        if not self.__confInstance.has_section(section):
            self.__confInstance.add_section(section)
        self.__outFp = open(self.__iniFile, "w")
        self.__confInstance.set(section, option, value)

        self.__confInstance.write(self.__outFp)
        self.__outFp.close()

        return True


if __name__ == "__main__":
    "test code"
    scriptPath = os.path.split(os.path.realpath(__file__))[0]
    iniFilePath = os.path.join(scriptPath, "example.ini")
    print iniFilePath
    iniExample = MyConfig(iniFilePath)
    print iniExample.get("Example", "MyConfigTest", "Example.ini No Found !")

