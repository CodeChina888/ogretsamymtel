::生成新的文件（生成前会判断是否能够生成，然后在清除旧文件重新生成） 参数：待生成源包路径  生成目标文件夹
java -cp ./crossSrv/bin;./core/bin;./platform/bin;./msg/bin;./config/;./libs/* core.gen.execute.Gen crosssrv /crossSrv/gen/ true
java -cp ./crossSrv/bin;./core/bin;./platform/bin;./msg/bin;./config/;./libs/* core.gen.execute.Gen turnbasedsrv /crossSrv/gen/ true
java -cp ./core/bin;./msg/bin;./config/;./libs/* core.gen.execute.Gen core /core/gen/ true
pause