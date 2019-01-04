# -*- coding: gbk -*-

import ConfigParser
import os
import sys

#ini配置读写器
class MyConfig:
    "configParser wrapper class"
    __iniFile = ""
    __confInstance = None
    __outFp = None

    #初始化
    def __init__(self,filePath):
        self.__iniFile = filePath.strip()
        self.loadFormFile()

    #是否装载成功
    def isLoadOK(self):
        return self.__confInstance != None

    #装载配置
    def loadFormFile(self):
        sourcePath = sys.path[0]+"/../"+self.__iniFile
        print "sourcePath:"+sourcePath
        if not os.path.isfile(sourcePath):
            self.__confInstance = None
            return
        self.__confInstance = ConfigParser.ConfigParser()
        self.__confInstance.read(sourcePath)

    #读取配置
    def get(self, section, option, defValue=None):
        if self.__confInstance == None:
            return defValue
        if not self.__confInstance.has_option(section, option):
            return defValue
        return self.__confInstance.get(section, option, False, None)

    #写入配置
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

