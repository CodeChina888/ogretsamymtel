::生成新的文件（生成前会判断是否能够生成，然后在清除旧文件重新生成） 参数：待生成源包路径  生成目标文件夹
java -cp ./out/production/world;./out/production/msg;./config/;./libs/* core.gen.execute.Gen game /world/gen/
pause