Android调用so库（c语言编写） Codeblocks+adt-win-x86+ndk-r9c

CSDN:  http://blog.csdn.net/lanmo555/article/details/18698391


Android调用so库, so库是c语言编写


1. 所需软件环境：
1）so库开发环境
操作系统： Ubuntu 10.04  x86

编译软件：Code::Blocks

开发库：android-ndk-r9c-linux-x86.tar.bz2 

2） Android客户端开发
操作系统：Windows 7 x86

测试环境： Android手机（系统4.0及以上）

开发工具和SDK包： adt-bundle-windows-x86-20131030.zip


可以在 http://developer.android.com/intl/zh-cn/develop/index.html 下载  

android-ndk-r9c-linux-x86.tar.bz2   http://developer.android.com/intl/zh-cn/tools/sdk/ndk/index.html
adt-bundle-windows-x86-20131030.zip   http://developer.android.com/intl/zh-cn/sdk/index.html
2. 环境搭建
1）Code::Blocks环境搭建

      首先安装Code::Blocks, 然后解压 android-ndk-r9c-linux-x86.tar.bz2 , 如解压到  /home/ndk-r9c/
      然后启动Code::Blocks,  进行系统环境配置
      

2）Android 开发环境搭建

     直接解压 adt-bundle-windows-x86-20131030.zip ， 如解压到  E:\Program Files\adt\adt-bundle-windows-x86-20131030 