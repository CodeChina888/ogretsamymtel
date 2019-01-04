::生成新的文件（生成前会判断是否能够生成，然后在清除旧文件重新生成） 参数：待生成源包路径  生成目标文件夹
java -cp ./core/bin;./config/;./libs/* core.gen.execute.Gen core /core/gen/
pause