#!/bin/bash


case $1 in
'CheckFileApp')
nohup java -classpath ./jts_download-1.0-SNAPSHOT-jar-with-dependencies.jar com.fancydsp.data.CheckFileApp > ./CheckFileApp.log  2>&1 &
;;
'FixFileApp')
nohup java -classpath ./jts_download-1.0-SNAPSHOT-jar-with-dependencies.jar com.fancydsp.data.FixFileApp > ./FixFileApp.log  2>&1 &
;;
'DownloadApp')
nohup java -classpath ./jts_download-1.0-SNAPSHOT-jar-with-dependencies.jar com.fancydsp.data.DownloadApp > ./DownloadApp.log  2>&1 &
;;
* )
echo -e "error"
;;
esac
