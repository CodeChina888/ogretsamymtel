# coding:utf8
import os
import os.path
import shutil
def copytree(src, dst, symlinks=False):
    names = os.listdir(src)
    if not os.path.isdir(dst):
        os.makedirs(dst)
          
    errors = []
    for name in names:
        srcname = os.path.join(src, name)
        dstname = os.path.join(dst, name)
        try:
            if symlinks and os.path.islink(srcname):
                linkto = os.readlink(srcname)
                os.symlink(linkto, dstname)
            elif os.path.isdir(srcname):
                copytree(srcname, dstname, symlinks)
            else:
                if os.path.isdir(dstname):
                    os.rmdir(dstname)
                elif os.path.isfile(dstname):
                    os.remove(dstname)
                shutil.copy2(srcname, dstname)
            # XXX What about devices, sockets etc.?
        except (IOError, os.error) as why:
            errors.append((srcname, dstname, str(why)))
        # catch the Error from the recursive copytree so that we can
        # continue with other files
        except OSError as err:
            errors.extend(err.args[0])
    try:
        shutil.copystat(src, dst)
    except WindowsError:
        # can't copy file access times on Windows
        pass
    except OSError as why:
        errors.extend((src, dst, str(why)))
    if errors:
        raise Error(errors)

#把指定文件的内容替换的函数,多个环境参数并行替换，这样就可以一个脚本，多个通用。
def repip_func(file_path, odlStr,newStr):

  f = open(file_path,'r+')
  all_the_lines = f.readlines()
  f.seek(0)
  f.truncate()
  for line in all_the_lines:
    line = line.replace(odlStr,newStr)
    f.write(line)
  f.close()

  
def gci(filepath):
#遍历filepath下所有文件，包括子目录
  files = os.listdir(filepath)
  for fi in files:
    fi_d = os.path.join(filepath,fi)            
    if os.path.isdir(fi_d):
      gci(fi_d)                  
    else:
        list = os.listdir(filepath) #列出文件夹下所有的目录与文件

        for i in range(0,len(list)):
            path = os.path.join(filepath,list[i])
            if os.path.isfile(path) and os.path.splitext(path)[1] == ".java":
                f = open(path,'r')
                rc = f.read()
                rc = rc.replace('game.platform','org.gof.platform')
                rc = rc.replace('import core','import org.gof.core')
                rc = rc.replace('import game.','import org.gof.demo.')
                rc = rc.replace('game.turnbasedsrv','org.gof.demo.turnbasedsrv')
                rc = rc.replace('package game.','package org.gof.demo.')
                rc = rc.replace('import org.gof.demo.msg.','import org.gof.demo.worldsrv.msg.')
                
                f.close()
                t =open(path,'w')
                t.write(rc)
                t.close()
                print "replace file ",path
def syncFile(oldDir,newDir):
    #复制文件
    copytree(oldDir, newDir)
    # 批量替换
    gci(newDir)
if __name__ == '__main__':
    syncFile('platform/src/game/platform/','../branches/20171215_ServerResetCore/serverNew/platform/src/org/gof/platform/')
    syncFile('world/src/game/','../branches/20171215_ServerResetCore/serverNew/world/src/org/gof/demo/')
    syncFile('world/seam/game/seam/','../branches/20171215_ServerResetCore/serverNew/world/seam/org/gof/demo/seam/')